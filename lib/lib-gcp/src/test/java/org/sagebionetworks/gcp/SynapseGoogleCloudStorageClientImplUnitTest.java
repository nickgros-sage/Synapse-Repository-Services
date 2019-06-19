package org.sagebionetworks.gcp;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.auth.Credentials;
import com.google.cloud.storage.Storage;

@ExtendWith(MockitoExtension.class)
public class SynapseGoogleCloudStorageClientImplUnitTest {
	
	@Mock
	private Storage mockStorage;

	@Mock
	private GoogleCloudConfig config;

	@Mock
	private Credentials mockCredentials;

	private SynapseGoogleCloudStorageClient client;

	@BeforeAll
	public static void beforeAll() {
		GoogleCloudConfig config = new StackGoogleCloudConfigProvider();
		when(config.getCredentials()).thenReturn(mockCredentials);
		client = new SynapseGoogleCloudStorageClientImpl(config);
	}

}
