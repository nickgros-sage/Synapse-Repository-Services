package org.sagebionetworks.repo.manager.doi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.doi.*;
import org.sagebionetworks.repo.manager.AuthorizationManager;
import org.sagebionetworks.repo.manager.AuthorizationManagerUtil;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.model.*;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

public class EntityDoiManagerDataciteImpl implements EntityDoiManager {

	private final Logger logger = LogManager.getLogger(EntityDoiManagerDataciteImpl.class);

	@Autowired private UserManager userManager;
	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private DoiDao doiDao;
	@Autowired private NodeDAO nodeDao;;
	@Autowired private DoiAsyncClient doiAsyncClient;
	@Autowired private DoiHandler doiHandler;

	private DataciteEzMetadata metadata = new DataciteEzMetadata();
	private final DxAsyncClient dxAsyncClient;

	public EntityDoiManagerDataciteImpl() {
		dxAsyncClient = new DxAsyncClient();
	}

	/**
	 * Limits the transaction boundary to within the DOI DAO and runs with a new transaction.
	 * DOI client creating the DOI is an asynchronous call and must happen outside the transaction to
	 * avoid race conditions.
	 */
	@Override
	public Doi createDoi(final Long userId, final String entityId, final Long versionNumber)
			throws NotFoundException, UnauthorizedException, DatastoreException {

		if (userId == null) {
			throw new IllegalArgumentException("User name cannot be null or empty.");
		}
		if (entityId == null) {
			throw new IllegalArgumentException("Entity ID cannot be null");
		}

		// Authorize
		UserInfo currentUser = userManager.getUserInfo(userId);
		UserInfo.validateUserInfo(currentUser);
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(currentUser, entityId, ObjectType.ENTITY, ACCESS_TYPE.UPDATE));

		// If it already exists with no error, no need to create again.
		Doi doiDto = null;
		try {
			doiDto = doiDao.getDoi(entityId, ObjectType.ENTITY, versionNumber);
		} catch (NotFoundException e) {
			doiDto = null;
		}
		if (doiDto != null && !DoiStatus.ERROR.equals(doiDto.getDoiStatus())) {
			return doiDto;
		}

		// Find the node. Make sure the node exists. Node info will be used in DOI metadata.
		final Node node = getNode(entityId, versionNumber);

		// Record the attempt. This is where we draw the transaction boundary.
		if (doiDto == null) {
			String userGroupId = currentUser.getId().toString();
			doiDto = doiDao.createDoi(userGroupId, entityId, ObjectType.ENTITY, versionNumber, DoiStatus.IN_PROCESS);
		} else {
			doiDto = doiDao.updateDoiStatus(entityId, ObjectType.ENTITY, versionNumber, DoiStatus.IN_PROCESS, doiDto.getEtag());
		}

		// Create DOI string
		doiHandler.setDto(doiDto);
		String doi = DataciteConstants.DOI_PREFIX + entityId;
		if (versionNumber != null) {
			doi = doi + "." + versionNumber;
		}
		doiHandler.setDoi(doi);

		// Create DOI metadata.
		String creatorName = DataciteConstants.DEFAULT_CREATOR;
		metadata.setCreator(creatorName);
		final int year = Calendar.getInstance().get(Calendar.YEAR);
		metadata.setPublicationYear(year);
		metadata.setPublisher(DataciteConstants.PUBLISHER);
		String target = DataciteConstants.TARGET_URL_PREFIX + entityId;
		if (versionNumber != null) {
			target = target + "/version/" + versionNumber;
		}
		metadata.setTarget(target);
		metadata.setTitle(node.getName());
		metadata.setDoi(doi);
		doiHandler.setMetadata(metadata);

		// Call Datacite to create the DOI
		doiAsyncClient.create(doiHandler, new DoiAsyncCallback() {

			@Override
			public void onSuccess(DoiHandler doi) {
				assert doi != null;
				try {
					Doi dto = doi.getDto();
					doiDao.updateDoiStatus(dto.getObjectId(), dto.getObjectType(),
							dto.getObjectVersion(), DoiStatus.CREATED, dto.getEtag());
				} catch (DatastoreException e) {
					logger.error(e.getMessage(), e);
				} catch (NotFoundException e) {
					logger.error(e.getMessage(), e);
				}
			}

			@Override
			public void onError(DoiHandler doi, Exception e) {
				assert doi != null;
				try {
					logger.error(e.getMessage(), e);
					Doi dto = doi.getDto();
					doiDao.updateDoiStatus(dto.getObjectId(), dto.getObjectType(),
							dto.getObjectVersion(), DoiStatus.ERROR, dto.getEtag());
				} catch (DatastoreException x) {
					logger.error(x.getMessage(), x);
				} catch (NotFoundException x) {
					logger.error(x.getMessage(), x);
				}
			}
		});

		// Now calls the DOI resolution service to check if the DOI is ready for use
		dxAsyncClient.resolve(doiHandler, new DxAsyncCallback() {

			@Override
			public void onSuccess(DoiHandler doiHandler) {
				try {
					Doi doiDto = doiHandler.getDto();
					doiDto = doiDao.getDoi(doiDto.getObjectId(), doiDto.getObjectType(),
							doiDto.getObjectVersion());
					doiDao.updateDoiStatus(doiDto.getObjectId(),
							doiDto.getObjectType(), doiDto.getObjectVersion(),
							DoiStatus.READY, doiDto.getEtag());
				} catch (DatastoreException e) {
					logger.error(e.getMessage(), e);
				} catch (NotFoundException e) {
					logger.error(e.getMessage(), e);
				}
			}

			@Override
			public void onError(DoiHandler doiHandler, Exception e) {
				logger.error(e.getMessage(), e);
			}
		});

		return doiDto;
	}

	@Override
	public Doi getDoiForVersion(Long userId, String entityId, Long versionNumber)
			throws NotFoundException, UnauthorizedException, DatastoreException {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null or empty.");
		}
		if (entityId == null) {
			throw new IllegalArgumentException("Entity ID cannot be null");
		}

		UserInfo currentUser = userManager.getUserInfo(userId);
		UserInfo.validateUserInfo(currentUser);
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(currentUser, entityId, ObjectType.ENTITY, ACCESS_TYPE.READ));
		return doiDao.getDoi(entityId, ObjectType.ENTITY, versionNumber);
	}

	/** Gets the node whose information will be used in DOI metadata. */
	private Node getNode(String entityId, Long versionNumber) throws NotFoundException {
		Node node = null;
		if (versionNumber == null) {
			node = nodeDao.getNode(entityId);
		} else {
			node = nodeDao.getNodeForVersion(entityId, versionNumber);
		}
		if (node == null) {
			String error = "Cannot find entity " + entityId;
			if (versionNumber != null) {
				error = error + " for version " + versionNumber;
			}
			throw new NotFoundException(error);
		}
		return node;
	}

	@Override
	public Doi getDoiForCurrentVersion(Long userId, String entityId)
			throws NotFoundException, UnauthorizedException, DatastoreException {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null or empty.");
		}
		if (entityId == null) {
			throw new IllegalArgumentException("Entity ID cannot be null");
		}

		UserInfo currentUser = userManager.getUserInfo(userId);
		UserInfo.validateUserInfo(currentUser);
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(currentUser, entityId, ObjectType.ENTITY, ACCESS_TYPE.READ));
		Node node = getNode(entityId, null);
		Long versionNumber = null;
		// Versionables such as files should have the null versionNumber converted into non-null versionNumber
		if (node.getNodeType() == EntityType.file || node.getNodeType() == EntityType.table) {
			versionNumber = getNode(entityId, null).getVersionNumber();
		}
		return doiDao.getDoi(entityId, ObjectType.ENTITY, versionNumber);
	}
}
