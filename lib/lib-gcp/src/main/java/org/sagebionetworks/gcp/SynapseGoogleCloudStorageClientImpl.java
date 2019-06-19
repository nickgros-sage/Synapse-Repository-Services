package org.sagebionetworks.gcp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sagebionetworks.repo.web.TemporarilyUnavailableException;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class SynapseGoogleCloudStorageClientImpl implements SynapseGoogleCloudStorageClient {

	private static final int MAX_OBJECTS_IN_COMPOSE = 32;

	private Storage storage;

	public SynapseGoogleCloudStorageClientImpl(GoogleCloudConfig config) {
		this.storage = StorageOptions.newBuilder().setCredentials(config.getCredentials()).build().getService();
	}

	@Override
	public void deleteObject(String bucketName, String key) {
		BlobId blobId = BlobId.of(bucketName, key);
		if (!storage.delete(blobId)) {
			throw new TemporarilyUnavailableException("Error encountered when deleting the object in Google Cloud Storage. The item has not been deleted.");
		}
	}

	@Override
	public Blob getObject(String bucket, String key) {
		return storage.get(BlobId.of(bucket, key));
	}

	@Override
	public URL createSignedUrl(String bucket, String key, int expirationInMilliseconds, HttpMethod requestMethod) {
		return storage.signUrl(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(),
				expirationInMilliseconds, TimeUnit.MILLISECONDS, Storage.SignUrlOption.withV4Signature(),
				Storage.SignUrlOption.httpMethod(requestMethod));
	}

	@Override
	public Blob putObject(String bucket, String key, File file) throws IOException {
		return storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(), Files.readAllBytes(file.toPath()));
	}

	@Deprecated
	@Override
	public Blob putObject(String bucket, String key, File file, ObjectMetadata metadata) throws IOException {
		return storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key))
				.setContentType(metadata.getContentType())
				.setContentDisposition(metadata.getContentDisposition())
				.setContentEncoding(metadata.getContentEncoding())
				.setMd5(metadata.getContentMD5())
				.setContentLanguage(metadata.getContentLanguage())
				.build(), Files.readAllBytes(file.toPath()));
	}


	@Override
	public Blob composeObjects(String bucket, String newKey, List<String> partKeys) {
		if (partKeys.size() > 32) throw new IllegalArgumentException("Cannot compose more than 32 objects in one request");
		BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, newKey)).build();
		Storage.ComposeRequest.Builder builder = Storage.ComposeRequest.newBuilder().setTarget(blobInfo);
		for (String part : partKeys) {
				builder.addSource(part);
		}
		return storage.compose(builder.build());
	}


	public Blob composeObjectsNoLimit(String bucket, String newKey, List<String> partKeys) {
		BlobId blobId = BlobId.of(bucket, newKey);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		storage.create(blobInfo);

//		// TODO: Come up with a less ugly algorithm to compose a very large object
		Blob blob = null;
		// I know this algo works
//		int i = 0;
//		while (i < partKeys.size()) { // get each part
//			Storage.ComposeRequest.Builder builder =
//					Storage.ComposeRequest.newBuilder()
//							.setTarget(blobInfo)
//							.addSource(blobInfo.getName());
//			for (int j = 0; j < (MAX_OBJECTS_IN_COMPOSE - 1) && i < partKeys.size();) { // Cannot compose more than 32 chunks at a time
//				builder.addSource(partKeys.get(i));
//				i++;
//				j++;
//			}
//			blob = storage.compose(builder.build());
//		}

		// I don't know if this algo works, but it's cleaner
		Storage.ComposeRequest.Builder builder = Storage.ComposeRequest.newBuilder()
				.setTarget(blobInfo)    // Save to the new location
				.addSource(blobInfo.getName()); // Use the content we have already composed
		int currentCompositionSize = 1;
		for (String part : partKeys) {
			if (currentCompositionSize == MAX_OBJECTS_IN_COMPOSE) {
				blob = storage.compose(builder.build());
				builder = Storage.ComposeRequest.newBuilder()
						.setTarget(blobInfo)    // Save to the new location
						.addSource(blobInfo.getName()); // Use the content we have already composed
				currentCompositionSize = 1;
			}
			builder.addSource(part);
			currentCompositionSize++;
		}
		if (currentCompositionSize > 1) { // We reached the last set of parts but haven't composed them yet
			blob = storage.compose(builder.build());
		}
		return blob;
	}

	@Override
	public void rename(String bucket, String oldKey, String newKey) {
		storage.copy(Storage.CopyRequest.newBuilder().setSource(BlobId.of(bucket,oldKey)).setTarget(BlobId.of(bucket,newKey)).build());
		storage.delete(BlobId.of(bucket,oldKey));
	}
}
