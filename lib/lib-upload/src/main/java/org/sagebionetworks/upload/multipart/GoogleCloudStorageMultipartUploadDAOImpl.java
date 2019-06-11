package org.sagebionetworks.upload.multipart;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.sagebionetworks.gcp.GoogleCloudClientFactory;
import org.sagebionetworks.repo.model.file.AddPartRequest;
import org.sagebionetworks.repo.model.file.CompleteMultipartRequest;
import org.sagebionetworks.repo.model.file.PartMD5;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.HttpMethod;

public class GoogleCloudStorageMultipartUploadDAOImpl implements GoogleCloudStorageMultipartUploadDAO {

	// 15 minutes
	private static final int PRE_SIGNED_URL_EXPIRATION_MS = 15 * 1000 * 60;


	@Override
	public URL createSignedPutUrl(String bucket, String partKey, String contentType) {
		return GoogleCloudClientFactory.createGoogleCloudStorageClient().createSignedUrl(bucket, partKey, PRE_SIGNED_URL_EXPIRATION_MS, HttpMethod.PUT);
	}

	@Override
	public void addPart(AddPartRequest request) {

	}

	@Override
	public void deleteObject(String bucket, String key) {
		GoogleCloudClientFactory.createGoogleCloudStorageClient().deleteBlob(bucket, key);
	}

	@Override
	public long completeMultipartUpload(CompleteMultipartRequest request, List<PartMD5> parts) {
		Blob blob = GoogleCloudClientFactory.createGoogleCloudStorageClient().composeObjects(request.getBucket(), parts.stream().map(PartMD5::getPartMD5Hex).collect(Collectors.toList()), request.getKey());
		GoogleCloudClientFactory.createGoogleCloudStorageClient()
				.deleteBlobs(parts.stream().map(p -> BlobId.of(request.getBucket(), String.valueOf(p))).collect(Collectors.toList()));
		return blob.getSize();
	}

	@Override
	public void renameObject(String bucket, String oldKey, String newKey) {
		GoogleCloudClientFactory.createGoogleCloudStorageClient().rename(bucket, oldKey, newKey);
	}
}