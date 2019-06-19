package org.sagebionetworks.gcp;

import com.google.auth.Credentials;

public interface GoogleCloudConfig {

	Credentials getCredentials();

}
