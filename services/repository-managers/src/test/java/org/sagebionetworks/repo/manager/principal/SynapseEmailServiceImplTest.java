package org.sagebionetworks.repo.manager.principal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.manager.S3TestUtils;
import org.sagebionetworks.util.RetryException;
import org.sagebionetworks.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class SynapseEmailServiceImplTest {
	
	private static String BUCKET = null;
	
	private String s3KeyToDelete;

	@Autowired
	private SynapseEmailService sesClient;
	
	@Autowired
	private AmazonS3Client s3Client;
	
	@BeforeClass
	public static void before() throws Exception {
		BUCKET = StackConfiguration.getS3Bucket();
	}
	
	@After
	public void after() throws Exception {
		if (s3KeyToDelete!=null && S3TestUtils.doesFileExist(BUCKET, s3KeyToDelete, s3Client)) {
			S3TestUtils.deleteFile(BUCKET, s3KeyToDelete, s3Client);
		}
		s3KeyToDelete = null;
	}

	@Test
	public void testWriteToFile() throws Exception {
		String to = UUID.randomUUID().toString()+"@foo.bar";
		s3KeyToDelete = to+".json";
		assertFalse(S3TestUtils.doesFileExist(BUCKET, s3KeyToDelete, s3Client));
		SendEmailRequest emailRequest = new SendEmailRequest();
		Destination destination = new Destination();
		destination.setToAddresses(Collections.singletonList(to));
		emailRequest.setDestination(destination);
		Message message = new Message();
		Body body = new Body();
		Content content = new Content();
		content.setData("my dog has fleas");
		body.setText(content);
		message.setBody(body);
		emailRequest.setMessage(message);
		emailRequest.setSource("me@foo.bar");
		sesClient.sendEmail(emailRequest);
		boolean result = TimeUtils.waitForExponentialMaxRetry(10, 1000L, 
			new Callable<Boolean>(){

				@Override
				public Boolean call() throws Exception {
					boolean result = S3TestUtils.doesFileExist(BUCKET, s3KeyToDelete, s3Client);
					if (!result) throw new RetryException("file does not exist");
					return true;
				}
				
			});
		
		assertTrue(result);
	}

}
