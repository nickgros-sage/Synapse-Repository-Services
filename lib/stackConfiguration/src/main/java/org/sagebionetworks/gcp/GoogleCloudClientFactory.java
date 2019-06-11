package org.sagebionetworks.gcp;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * A factory for creating AWS clients using credential chains.
 *
 */
public class GoogleCloudClientFactory {


	public static SynapseGoogleCloudStorageClientImpl createGoogleCloudStorageClient() {
		Credentials credentials;
		try {
			credentials = GoogleCredentials.fromStream(new FileInputStream("/Users/gros47/Sage/imperial-glyph-243017-515298f49795.json"));
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
		return new SynapseGoogleCloudStorageClientImpl(credentials);
	}

}
