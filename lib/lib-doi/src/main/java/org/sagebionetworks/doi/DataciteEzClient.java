package org.sagebionetworks.doi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.sagebionetworks.repo.model.doi.Doi;
import org.springframework.stereotype.Component;

/**
 * EZID DOI client.
 */
@Component
public class DataciteEzClient implements DoiClient {

	private static final String REALM = "ez.datacite.org";
	private static final Integer TIME_OUT = Integer.valueOf(9000); // 9 seconds
	private static final String USER_AGENT = "Synapse";
	private final RetryableHttpClient writeClient;
	private final RetryableHttpClient readClient;

	public DataciteEzClient() {
		// Write client needs to set up authentication
		AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, REALM, AuthSchemes.BASIC);
		final String username = DataciteConstants.DATACITE_USERNAME;
		final String password = DataciteConstants.DATACITE_PASSWORD;
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(authScope, new UsernamePasswordCredentials(username, password));
		// Create a configuration to use the desired socket timeout
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).build();
		// Apply these parameters to the HTTP client
		HttpClient httpClientW = HttpClients.custom()
				.setUserAgent(USER_AGENT)
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCredentialsProvider(credentialsProvider).build();

		writeClient = new RetryableHttpClient(httpClientW);

		// Read client does not need authentication
		final HttpClient httpClientR = HttpClients.custom()
				.setUserAgent(USER_AGENT)
				.setDefaultRequestConfig(requestConfig)
				.build();
		readClient = new RetryableHttpClient(httpClientR);
	}

	public boolean isStatusOk() {
		URI uri = URI.create(DataciteConstants.DATACITE_EZ_URL + "status");
		HttpGet get = new HttpGet(uri);
		get.setHeader("User-Agent", USER_AGENT);
		HttpResponse response = readClient.executeWithRetry(get);
		try {
			// Must consume the response to close the connection
			EntityUtils.toString(response.getEntity());
			final int status = response.getStatusLine().getStatusCode();
			return status == HttpStatus.SC_OK;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public DoiHandler get(final DoiHandler doiHandler) {

		if (doiHandler == null) {
			throw new IllegalArgumentException("DOI handler cannot be null.");
		}
		final String doi = doiHandler.getDoi();
		if (doi == null) {
			throw new IllegalArgumentException("DOI string cannot be null.");
		}
		final Doi doiDto = doiHandler.getDto();
		if (doiDto == null) {
			throw new IllegalArgumentException("DOI DTO cannot be null.");
		}

		URI uri = URI.create(DataciteConstants.DATACITE_EZ_URL + doi);
		HttpGet get = new HttpGet(uri);
		get.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
		HttpResponse response = readClient.executeWithRetry(get);

		String responseString = "";
		try {
			// Must consume the response to close the connection
			responseString = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		final int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			// If the doi does not exist, EZID does not return
			// HttpStatus.SC_NOT_FOUND as of now. Instead it returns
			// HttpStatus.SC_BAD_REQUEST "no such identifier".
			String error = status + " ";
			error = error + " " + responseString;
			throw new RuntimeException(error);
		}

		DoiHandler result = new DoiHandler();
		result.setDoi(doi);
		result.setDto(doiDto);
		DataciteEzMetadata metadata = new DataciteEzMetadata();
		metadata.initFromString(responseString);
		result.setMetadata(metadata);
		return result;
	}

	public void create(final DoiHandler doiHandler) {

		if (doiHandler == null) {
			throw new IllegalArgumentException("DOI cannot be null.");
		}

		URI uri = URI.create(DataciteConstants.DATACITE_EZ_URL + doiHandler.getDoi());
		HttpPut put = new HttpPut(uri);
		// For some reason these headers aren't being appended at the HTTP Client level
//		put.setHeader(HttpHeaders.USER_AGENT, USER_AGENT);
		put.setHeader(HttpHeaders.CONTENT_TYPE,"text/plain;charset=UTF-8");
		StringEntity requestEntity = new StringEntity(doiHandler.getMetadata().getMetadataAsString(), "UTF-8");
		put.setEntity(requestEntity);

		System.out.println(doiHandler.getMetadata().getMetadataAsString());

		HttpResponse response = writeClient.executeWithRetry(put);
		String responseString = "";
		try {
			// Must consume the response to close the connection
			responseString = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		final int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) { // For some reason DataciteEz throws 200 OK upon creation
			if (status == HttpStatus.SC_BAD_REQUEST) {
				if (responseString.toLowerCase().contains("identifier already exists")) {
					return;
				}
				try {
					get(doiHandler);
					return; // Already exists
				} catch (RuntimeException e) {
					String error = "DOI " + doiHandler.getDoi();
					error += " got 400 BAD_REQUEST but does not already exist.";
					throw new RuntimeException(error);
				}
			}
			String error = status + " ";
			error = error + " " + responseString;
			throw new RuntimeException(error);
		}
	}

	public void update(DoiHandler doiHandler) {

		if (doiHandler == null) {
			throw new IllegalArgumentException("DOI cannot be null.");
		}

		URI uri = URI.create(DataciteConstants.DATACITE_EZ_URL + doiHandler.getDoi());
		HttpPost post = new HttpPost(uri);
		post.setHeader("User-Agent", USER_AGENT);

		try {
			StringEntity requestEntity = new StringEntity(
					doiHandler.getMetadata().getMetadataAsString(),
					HTTP.PLAIN_TEXT_TYPE, "UTF-8");
			post.setEntity(requestEntity);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		HttpResponse response = writeClient.executeWithRetry(post);

		try {
			// Must consume the response to close the connection
			final String responseStr = EntityUtils.toString(response.getEntity());
			final int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				String error = status + " ";
				error = error + " " + responseStr;
				throw new RuntimeException(error);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
