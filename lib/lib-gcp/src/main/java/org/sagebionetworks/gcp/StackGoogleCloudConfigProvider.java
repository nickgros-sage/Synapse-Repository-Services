package org.sagebionetworks.gcp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.web.TemporarilyUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

/*
 * Use to configure parameters in the DataCite client
 */
public class StackGoogleCloudConfigProvider implements GoogleCloudConfig {

	@Autowired
	StackConfiguration stackConfiguration;

	public Credentials getCredentials() {
		try {
			return ServiceAccountCredentials.fromStream(new ByteArrayInputStream(stackConfiguration.getGoogleCloudCredentials().getBytes(StandardCharsets.UTF_8)));
		} catch (IOException e) {
			throw new TemporarilyUnavailableException("Error getting credentials to connect to Google Cloud");
		}
	}

}
