package org.sagebionetworks;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.sagebionetworks.aws.SynapseS3Client;
import org.sagebionetworks.repo.manager.EntityManager;
import org.sagebionetworks.repo.manager.asynch.AsynchJobStatusManager;
import org.sagebionetworks.repo.manager.asynch.AsynchJobUtils;
import org.sagebionetworks.repo.manager.table.TableEntityManager;
import org.sagebionetworks.repo.manager.table.TableManagerSupport;
import org.sagebionetworks.repo.manager.table.TableViewManager;
import org.sagebionetworks.repo.manager.table.metadata.MetadataIndexProvider;
import org.sagebionetworks.repo.manager.table.metadata.MetadataIndexProviderFactory;
import org.sagebionetworks.repo.manager.table.metadata.ViewScopeFilterBuilder;
import org.sagebionetworks.repo.model.AsynchJobFailedException;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.dao.FileHandleDao;
import org.sagebionetworks.repo.model.entity.IdAndVersion;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.ObjectDataDTO;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryOptions;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableFailedException;
import org.sagebionetworks.repo.model.table.TableState;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.repo.model.table.ViewObjectType;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.table.ViewScopeFilter;
import org.sagebionetworks.repo.model.table.ViewScopeType;
import org.sagebionetworks.repo.web.TemporarilyUnavailableException;
import org.sagebionetworks.table.cluster.ConnectionFactory;
import org.sagebionetworks.table.cluster.TableIndexDAO;
import org.sagebionetworks.table.cluster.utils.TableModelUtils;
import org.sagebionetworks.table.query.ParseException;
import org.sagebionetworks.table.query.TableQueryParser;
import org.springframework.beans.factory.annotation.Autowired;

public class AsynchronousJobWorkerHelperImpl implements AsynchronousJobWorkerHelper {

	@Autowired
	AsynchJobStatusManager asynchJobStatusManager;
	@Autowired
	EntityManager entityManager;
	@Autowired
	ConnectionFactory tableConnectionFactory;
	@Autowired
	TableManagerSupport tableMangerSupport;
	@Autowired
	TableViewManager tableViewManager;
	@Autowired
	TableEntityManager tableEntityManager;
	@Autowired
	FileHandleDao fileHandleDao;
	@Autowired
	SynapseS3Client s3Client;
	@Autowired
	MetadataIndexProviderFactory metadataProviderFactory;

	@Override
	public <R extends AsynchronousRequestBody, T extends AsynchronousResponseBody> T startAndWaitForJob(UserInfo user,
			R request, long maxWaitMS, Class<? extends T> responseClass) throws InterruptedException {
		AsynchronousJobStatus status = asynchJobStatusManager.startJob(user, request);
		long start = System.currentTimeMillis();
		while (!AsynchJobState.COMPLETE.equals(status.getJobState())) {
			try {
				AsynchJobUtils.throwExceptionIfFailed(status);
			} catch (Throwable e) {
				if(e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}else {
					throw new RuntimeException(e);
				}
			}
			System.out.println("Waiting for job to complete: Message: " + status.getProgressMessage() + " progress: "
					+ status.getProgressCurrent() + "/" + status.getProgressTotal());
			assertTrue((System.currentTimeMillis() - start) < maxWaitMS, "Timed out waiting for job with request type: " + request.getClass().getName());
			Thread.sleep(1000);
			// Get the status again
			status = this.asynchJobStatusManager.getJobStatus(user, status.getJobId());
		}
		return (T) status.getResponseBody();
	}
	
	/**
	 * Wait for EntityReplication to show the given etag for the given entityId.
	 * 
	 * @param tableId
	 * @param entityId
	 * @param etag
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public ObjectDataDTO waitForEntityReplication(UserInfo user, String tableId, String entityId, long maxWaitMS) throws InterruptedException{
		Entity entity = entityManager.getEntity(user, entityId);
		return waitForObjectReplication(ViewObjectType.ENTITY, KeyFactory.stringToKey(entity.getId()), entity.getEtag(), maxWaitMS);
	}
	
	@Override
	public ObjectDataDTO waitForObjectReplication(ViewObjectType objectType, Long objectId, String etag, long maxWaitMS) throws InterruptedException {
		TableIndexDAO indexDao = tableConnectionFactory.getFirstConnection();
		long start = System.currentTimeMillis();
		while(true){
			ObjectDataDTO dto = indexDao.getObjectData(objectType, objectId);
			if(dto == null || !dto.getEtag().equals(etag)){
				assertTrue((System.currentTimeMillis()-start) <  maxWaitMS, "Timed out waiting for object replication.");
				System.out.println("Waiting for object replication. id: "+objectId+" etag: "+etag);
				Thread.sleep(1000);
			}else{
				return dto;
			}
		}
	}
	
	/**
	 * Create a View with the default schema for its type.
	 * @param user
	 * @param name
	 * @param parentId
	 * @param schema
	 * @param scope
	 * @param viewTypeMask
	 * @return
	 */
	@Override
	public EntityView createView(UserInfo user, String name, String parentId, List<String> scope, long viewTypeMask) {
		EntityType entityType = EntityType.entityview;
		List<ColumnModel> defaultColumns = tableMangerSupport.getDefaultTableViewColumns(entityType, viewTypeMask);
		EntityView view = new EntityView();
		view.setName(name);
		view.setViewTypeMask(viewTypeMask);
		view.setParentId(parentId);
		view.setColumnIds(TableModelUtils.getIds(defaultColumns));
		view.setScopeIds(scope);
		String viewId = entityManager.createEntity(user, view, null);
		view = entityManager.getEntity(user, viewId, EntityView.class);
		ViewScope viewScope = new ViewScope();
		viewScope.setViewEntityType(entityType);
		viewScope.setScope(view.getScopeIds());
		viewScope.setViewTypeMask(viewTypeMask);
		tableViewManager.setViewSchemaAndScope(user, view.getColumnIds(), viewScope, viewId);
		return view;
	}
	
	@Override
	public SubmissionView createSubmissionView(UserInfo user, String name, String parentId, List<String> scope) {
		EntityType entityType = EntityType.submissionview;
		Long typeMask = 0L;
		List<ColumnModel> defaultColumns = tableMangerSupport.getDefaultTableViewColumns(entityType, typeMask);
		SubmissionView view = new SubmissionView();
		view.setName(name);
		view.setParentId(parentId);
		view.setColumnIds(TableModelUtils.getIds(defaultColumns));
		view.setScopeIds(scope);
		String viewId = entityManager.createEntity(user, view, null);
		view = entityManager.getEntity(user, viewId, SubmissionView.class);
		
		ViewScope viewScope = new ViewScope();
		viewScope.setViewEntityType(entityType);
		viewScope.setScope(view.getScopeIds());
		viewScope.setViewTypeMask(typeMask);
		
		tableViewManager.setViewSchemaAndScope(user, view.getColumnIds(), viewScope, viewId);
		
		return view;
	}
	
	/**
	 * An expensive call to determine if a view is up-to-date with the entity replication data.
	 * 
	 * @param tableId
	 * @return Optional<Boolean> A non-empty result is only returned if the ID belongs view
	 * with a status of available.
	 * @throws TableFailedException 
	 */
	@Override
	public Optional<Boolean> isViewAvailableAndUpToDate(IdAndVersion tableId) throws TableFailedException {
		EntityType type = tableMangerSupport.getTableEntityType(tableId);
		if(!EntityTypeUtils.isViewType(type)) {
			// not a view
			return Optional.empty();
		}
		TableStatus status = tableMangerSupport.getTableStatusOrCreateIfNotExists(tableId);
		if(!TableState.AVAILABLE.equals(status.getState())) {
			return Optional.empty();
		}
		TableIndexDAO indexDao = tableConnectionFactory.getConnection(tableId);
		ViewScopeType scopeType = tableMangerSupport.getViewScopeType(tableId);
		Set<Long> allContainersInScope = tableMangerSupport.getAllContainerIdsForViewScope(tableId, scopeType);
		
		MetadataIndexProvider provider = metadataProviderFactory.getMetadataIndexProvider(scopeType.getObjectType());
		
		ViewScopeFilter scopeFilter = new ViewScopeFilterBuilder(provider, scopeType.getTypeMask())
				.withContainerIds(allContainersInScope).build();
		
		long limit = 1L;
		Set<Long> changes = indexDao.getOutOfDateRowsForView(tableId, scopeFilter, limit);
		return Optional.of(changes.isEmpty());
	}
	
	@Override
	public QueryResultBundle waitForConsistentQuery(UserInfo user, String sql, Predicate<QueryResultBundle> resultMatcher, int maxWaitTime) throws Exception {
		Query query = new Query();
		query.setSql(sql);
		query.setIncludeEntityEtag(true);
		QueryOptions options = new QueryOptions()
				.withRunQuery(true)
				.withRunCount(true)
				.withReturnFacets(false)
				.withReturnColumnModels(true);
		return waitForConsistentQuery(user, query, options, resultMatcher, maxWaitTime);
	}
	
	@Override
	public QueryResultBundle waitForConsistentQuery(UserInfo user, Query query, QueryOptions options, Predicate<QueryResultBundle> resultMatcher, int maxWaitTime) throws Exception {
		System.out.println("Submitting query: " + query.getSql());
		long start = System.currentTimeMillis();
		while(true) {
			QueryResultBundle results = waitForConsistentQuery(user, query, options, maxWaitTime);
			
			if (resultMatcher.test(results)) {
				return results;
			}
			
			System.out.println("Waiting for matching result...");
			assertTrue((System.currentTimeMillis()-start) <  maxWaitTime, "Timed out waiting for matching table results");
			Thread.sleep(1000);
		}
	}
	
	private QueryResultBundle waitForConsistentQuery(UserInfo user, Query query, QueryOptions options, int maxWaitTime) throws Exception {
		// Wait for the view to be up-to-date before running the query
		IdAndVersion viewId = extractTableIdFromQuery(query.getSql());
		waitForViewToBeUpToDate(viewId, maxWaitTime);
		// The view is up-to-date so run the caller's query.
		QueryBundleRequest request = new QueryBundleRequest();
		request.setQuery(query);
		request.setPartMask(options.getPartMask());
		QueryResultBundle results =  startAndWaitForJob(user, request, maxWaitTime, QueryResultBundle.class);
		// Keep running queries as long as a view out-of-date

		return results;
	}
	
	private IdAndVersion extractTableIdFromQuery(String sqlQuery) throws ParseException {
		return IdAndVersion.parse(TableQueryParser.parserQuery(sqlQuery).getTableName());
	}
	
	/**
	 * Helper to wait for a view to be up-to-date
	 * @param user
	 * @param viewId
	 * @throws InterruptedException
	 * @throws AsynchJobFailedException
	 * @throws TableFailedException 
	 */
	@Override
	public void waitForViewToBeUpToDate(IdAndVersion viewId, long maxWaitMS) throws InterruptedException, AsynchJobFailedException, TableFailedException {
		long start = System.currentTimeMillis();
		// only wait if the view is available but out-of-date.
		while(!isViewAvailableAndUpToDate(viewId).orElse(true)) {
			assertTrue((System.currentTimeMillis()-start) <  maxWaitMS, "Timed out waiting for table view to be up-to-date.");
			System.out.println("Waiting for view "+viewId+" to be up-to-date");
			Thread.sleep(1000);
		}
	}

	@Override
	public void setTableSchema(UserInfo userInfo, List<String> newSchema, String tableId, long maxWaitMS) throws InterruptedException {
		long start = System.currentTimeMillis();
		while(true) {
			try {
				tableEntityManager.setTableSchema(userInfo, newSchema, tableId);
				return;
			} catch (TemporarilyUnavailableException e) {
				System.out.println("Waiting for excluisve lock on "+tableId+"...");
				Thread.sleep(1000);
			}
			assertTrue((System.currentTimeMillis()-start) <  maxWaitMS, "Timed out Waiting for excluisve lock on "+tableId);
		}
	}
	

	/**
	 * Helper to download the contents of the given FileHandle ID to a string.
	 * @param fileHandleId
	 * @return
	 * @throws IOException
	 */
	@Override
	public String downloadFileHandleFromS3(String fileHandleId) throws IOException {
		FileHandle fh = fileHandleDao.get(fileHandleId);
		if (!(fh instanceof S3FileHandle)) {
			throw new IllegalArgumentException("Not a S3 file handle: " + fh.getClass().getName());
		}
		S3FileHandle s3Handle = (S3FileHandle) fh;
		try (Reader reader = new InputStreamReader(
				s3Client.getObject(s3Handle.getBucketName(), s3Handle.getKey()).getObjectContent(),
				StandardCharsets.UTF_8)) {
			return IOUtils.toString(reader);
		}
	}

}
