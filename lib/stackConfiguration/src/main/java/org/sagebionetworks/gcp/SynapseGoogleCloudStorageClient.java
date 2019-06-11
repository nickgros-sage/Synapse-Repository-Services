package org.sagebionetworks.gcp;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.HttpMethod;

public interface SynapseGoogleCloudStorageClient {

	Blob get(String bucket, String key);

	URL createSignedUrl(String bucket, String key, int expirationInMinutes, HttpMethod requestMethod);

	Blob put(String bucket, String key, String body);

	Blob put(String bucket, String key, File body) throws IOException;

	Blob put(String bucket, String key, File body, ObjectMetadata metadata) throws IOException;

	Blob composeObjects(String bucket, List<String> key, String blobName);

	void rename(String bucket, String oldKey, String newKey);
}
