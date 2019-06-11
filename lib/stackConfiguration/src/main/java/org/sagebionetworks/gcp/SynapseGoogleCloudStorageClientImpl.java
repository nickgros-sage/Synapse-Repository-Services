package org.sagebionetworks.gcp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.IOUtils;
import com.google.auth.Credentials;
import com.google.cloud.MetadataConfig;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class SynapseGoogleCloudStorageClientImpl implements SynapseGoogleCloudStorageClient {

	private Storage storage;

	public SynapseGoogleCloudStorageClientImpl(Credentials credentials) {
		this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
	}


	public Blob getBlob(String bucketName, String key)
			throws SdkClientException {
		return storage.get(BlobId.of(bucketName, key));
	}


	public BlobInfo getBlobWithMetadata(String bucketName, String key)
			throws SdkClientException {
		return storage.get(bucketName, key, Storage.BlobGetOption.fields(Storage.BlobField.values()));
	}

	public void deleteBlob(String bucketName, String key) {
		BlobId blobId = BlobId.of(bucketName, key);
		if (!storage.delete(blobId)) {
			throw new RuntimeException(); // TODO: More specific
		}
	}

	public List<Boolean> deleteBlobs(Iterable<BlobId> blobs)
			throws SdkClientException {
		return storage.delete(blobs);
	}

	@Override
	public Blob get(String bucket, String key) {
		return storage.get(BlobId.of(bucket, key));
	}

	@Override
	public URL createSignedUrl(String bucket, String key, int expirationInMinutes, HttpMethod requestMethod) {
		return storage.signUrl(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(),
				expirationInMinutes, TimeUnit.MILLISECONDS, Storage.SignUrlOption.withV4Signature(),
				Storage.SignUrlOption.httpMethod(requestMethod));
	}

	@Override
	public Blob put(String bucket, String key, String body) {
		return storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(), body.getBytes(UTF_8));
	}

	@Override
	public Blob put(String bucket, String key, File body) throws IOException {
		return storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(), Files.readAllBytes(body.toPath()));
	}

	@Override
	public Blob put(String bucket, String key, File body, ObjectMetadata metadata) throws IOException {
		return storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key))
				.setContentType(metadata.getContentType())
				.setContentDisposition(metadata.getContentDisposition())
				.setContentEncoding(metadata.getContentEncoding())
				.setMd5(metadata.getContentMD5())
				.build(), Files.readAllBytes(body.toPath()));
	}


	@Override
	public Blob composeObjects(String bucket, List<String> partKey, String blobName) {
		BlobId blobId = BlobId.of(bucket, blobName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
		storage.create(blobInfo);

		// TODO: Come up with a less ugly algorithm to compose a very large object
		Blob blob = null;
		int i = 0;
		while (i < partKey.size()) { // get each part
			Storage.ComposeRequest.Builder builder =
					Storage.ComposeRequest.newBuilder()
							.setTarget(blobInfo)
							.addSource(blobInfo.getName());
			for (int j = 0; j < 31 && i < partKey.size();) { // Cannot compose more than 32 chunks at a time
				builder.addSource(partKey.get(i));
				i++;
				j++;
			}
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
