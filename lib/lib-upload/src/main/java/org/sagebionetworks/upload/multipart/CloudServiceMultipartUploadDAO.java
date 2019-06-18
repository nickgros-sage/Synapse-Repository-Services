package org.sagebionetworks.upload.multipart;

import java.net.URL;
import java.util.List;

import org.sagebionetworks.repo.model.file.AddPartRequest;
import org.sagebionetworks.repo.model.file.CompleteMultipartRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;

import com.google.cloud.storage.Blob;

/**
 * Abstraction for S3 multi-part upload.
 *
 */
public interface CloudServiceMultipartUploadDAO {

	/**
	 * Start a multi-part upload.
	 * @param bucket
	 * @param key
	 * @param request
	 * @return
	 */
	public String initiateMultipartUpload(String bucket, String key, MultipartUploadRequest request);


	/**
	 * Create a pre-signed URL to A pre-signed URL to upload a part of multi-part file upload.
	 * @param bucket
	 * @param partKey
	 * @param contentType Optional parameter.  Sets the expected content-type of the request. The content-type is included in
     * the signature.
	 * @return
	 */
	public URL createPreSignedPutUrl(String bucket, String partKey, String contentType);

	/**
	 * Add a part to a multi-part upload. This call may delete the temporary part file, depending on implementation.
	 * @param request
	 */
	public void addPart(AddPartRequest request);

	/**
	 * Complete a multi-part upload.
	 * @param request
	 * @return The size of the resulting file.
	 */
	public long completeMultipartUpload(CompleteMultipartRequest request);

}
