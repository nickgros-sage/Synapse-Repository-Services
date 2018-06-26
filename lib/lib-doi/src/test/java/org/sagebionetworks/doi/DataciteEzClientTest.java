package org.sagebionetworks.doi;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.junit.Test;
import org.sagebionetworks.StackConfigurationSingleton;
import org.sagebionetworks.repo.model.doi.Doi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataciteEzClientTest {

	@Autowired DoiClient doiClient;

	@Test
	public void testIsStatusOk() throws Exception {

		// Mock status OK
		HttpResponse mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("response body"));
		StatusLine mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		RetryableHttpClient mockWriteClient = mock(RetryableHttpClient.class);
		when(mockWriteClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		DataciteEzClient client = new DataciteEzClient();
		ReflectionTestUtils.setField(client, "readClient", mockWriteClient);
		assertTrue(client.isStatusOk());

		// Mock ISE
		mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("response body"));
		mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		mockWriteClient = mock(RetryableHttpClient.class);
		when(mockWriteClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		client = new DataciteEzClient();
		ReflectionTestUtils.setField(client, "readClient", mockWriteClient);
		assertFalse(client.isStatusOk());
	}

	@Test
	public void testCreateGet() throws Exception {

		// Mock success
		HttpResponse mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("response body"));
		StatusLine mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		RetryableHttpClient mockWriteClient = mock(RetryableHttpClient.class);
		when(mockWriteClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		DataciteEzClient client = new DataciteEzClient();
		ReflectionTestUtils.setField(client, "writeClient", mockWriteClient);
		DoiHandler doiHandler = new DoiHandler();
		doiHandler.setDoi("doi:1093.3/sth");
		doiHandler.setDto(new Doi());
		DataciteEzMetadata mockMetadata = mock(DataciteEzMetadata.class);
		when(mockMetadata.getMetadataAsString()).thenReturn("metadata");
		doiHandler.setMetadata(mockMetadata);
		client.create(doiHandler);

		// Mock 400 BAD_REQUEST "identifier already exists"
		mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("bad request -- identifier already exists"));
		mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		mockWriteClient = mock(RetryableHttpClient.class);
		when(mockWriteClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		client = new DataciteEzClient();
		ReflectionTestUtils.setField(client, "writeClient", mockWriteClient);
		client.create(doiHandler);

		// Mock 400 BAD_REQUEST and get()
		// Mock write client
		mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("bad request"));
		mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		mockWriteClient = mock(RetryableHttpClient.class);
		when(mockWriteClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		// Mock read client
		mockResponse = mock(HttpResponse.class);
		when(mockResponse.getEntity()).thenReturn(new StringEntity("OK"));
		mockStatusLine = mock(StatusLine.class);
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		RetryableHttpClient mockReadClient = mock(RetryableHttpClient.class);
		when(mockReadClient.executeWithRetry(any(HttpUriRequest.class))).thenReturn(mockResponse);
		client = new DataciteEzClient();
		ReflectionTestUtils.setField(client, "writeClient", mockWriteClient);
		ReflectionTestUtils.setField(client, "readClient", mockReadClient);
		client.create(doiHandler);
	}
}
