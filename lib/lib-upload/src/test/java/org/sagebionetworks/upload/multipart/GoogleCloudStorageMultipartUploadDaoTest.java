package org.sagebionetworks.upload.multipart;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sagebionetworks.gcp.SynapseGoogleCloudStorageClient;
import org.sagebionetworks.repo.model.file.CompleteMultipartRequest;
import org.sagebionetworks.repo.model.file.PartMD5;

@ExtendWith(MockitoExtension.class)
public class GoogleCloudStorageMultipartUploadDaoTest {


	@Mock
	SynapseGoogleCloudStorageClient mockStorageClient;

	@InjectMocks
	GoogleCloudStorageMultipartUploadDAO googleCloudStorageMultipartUploadDAO;

	private static final String BUCKET_NAME = "test-bucket-name";


	@Test
	public void testGCP() {
		GoogleCloudStorageMultipartUploadDAO googleCloudStorageMultipartUploadDAO = new GoogleCloudStorageMultipartUploadDAOImpl();

		URL url = googleCloudStorageMultipartUploadDAO.createPreSignedPutUrl("synapase-test-storage-201906", "newuploadedobject2", null);


		System.out.println(url.toString());
	}

	@Test
	public void testCompleteMultipartUpload() throws Exception {

		GoogleCloudStorageMultipartUploadDAO googleCloudStorageMultipartUploadDAO = new GoogleCloudStorageMultipartUploadDAOImpl();

		List<PartMD5> parts = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			String fname = "file" + i;
			String body = RandomStringUtils.randomAlphanumeric(1024 * 1024 * 5); // 5 MB
			String md5Hash = DigestUtils.md5Hex(body);
//			parts.add(new PartMD5(i, md5Hash));
		}

		CompleteMultipartRequest cmr = new CompleteMultipartRequest();
		cmr.setBucket("synapase-test-storage-201906");
		cmr.setKey("a bigger object");
//		long length = googleCloudStorageMultipartUploadDAO.completeMultipartUpload(cmr);
		for (PartMD5 part : parts) {
//			googleCloudStorageMultipartUploadDAO.deleteObject(BUCKET_NAME, part.getPartMD5Hex());
		}
	}
}
