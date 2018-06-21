package org.sagebionetworks.doi;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DoiAsyncClientTest {

	@Test
	public void testCreateSuccess() throws Exception {

		DoiAsyncClient asyncClient = new DoiAsyncClientImpl();
		DoiClient doiClient = mock(DoiClient.class);
		ReflectionTestUtils.setField(asyncClient, "doiClient", doiClient);

		DoiHandler doi = mock(DoiHandler.class);
		DoiAsyncCallback callback = mock(DoiAsyncCallback.class);
		asyncClient.create(doi, callback);

		// Wait 100 ms before verifying the call
		// the execution is on a separate thread
		Thread.sleep(100L);
		verify(doiClient, times(1)).create(doi);
		verify(callback, times(1)).onSuccess(doi);
	}

	@Test
	public void testCreateError() throws Exception {

		DoiClient doiClient = mock(DoiClient.class);
		DoiHandler doiWithError = mock(DoiHandler.class);
		Exception e = new RuntimeException("Mocked exception");
		doThrow(e).when(doiClient).create(doiWithError);
		DoiAsyncClient asyncClient = new DoiAsyncClientImpl();
		ReflectionTestUtils.setField(asyncClient, "doiClient", doiClient);

		DoiAsyncCallback callback = mock(DoiAsyncCallback.class);
		asyncClient.create(doiWithError, callback);

		// Wait 100 ms before verifying the call
		// the execution is on a separate thread
		Thread.sleep(100L);
		verify(doiClient, times(1)).create(doiWithError);
		verify(callback, times(1)).onError(doiWithError, e);
	}

	@Test
	public void testUpdateSuccess() throws Exception {

		DoiClient doiClient = mock(DoiClient.class);
		DoiAsyncClient asyncClient = new DoiAsyncClientImpl();
		ReflectionTestUtils.setField(asyncClient, "doiClient", doiClient);

		DoiHandler doiHandler = mock(DoiHandler.class);
		DoiAsyncCallback callback = mock(DoiAsyncCallback.class);
		asyncClient.update(doiHandler, callback);

		// Wait 100 ms before verifying the call
		// the execution is on a separate thread
		Thread.sleep(100L);
		verify(doiClient, times(1)).update(doiHandler);
		verify(callback, times(1)).onSuccess(doiHandler);
	}

	@Test
	public void testUpdateError() throws Exception {

		DoiClient doiClient = mock(DoiClient.class);
		DoiHandler doiWithError = mock(DoiHandler.class);
		Exception e = new RuntimeException("Mocked exception");
		doThrow(e).when(doiClient).update(doiWithError);
		DoiAsyncClient asyncClient = new DoiAsyncClientImpl();
		ReflectionTestUtils.setField(asyncClient, "doiClient", doiClient);

		DoiAsyncCallback callback = mock(DoiAsyncCallback.class);
		asyncClient.update(doiWithError, callback);

		// Wait 100 ms before verifying the call
		// the execution is on a separate thread
		Thread.sleep(100L);
		verify(doiClient, times(1)).update(doiWithError);
		verify(callback, times(1)).onError(doiWithError, e);
	}
}
