package org.sagebionetworks.doi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DoiAsyncClientImpl implements DoiAsyncClient {

	public void create(final DoiHandler doiHandler, final DoiAsyncCallback callback) {
		if (callback == null) {
			throw new IllegalArgumentException("Callback handle must not be null.");
		}
		executor.execute(new Runnable () {
			@Override
			public void run() {
				try {
					doiClient.create(doiHandler);
					callback.onSuccess(doiHandler);
				} catch (Exception e) {
					callback.onError(doiHandler, e);
				}
			}});
	}

	public void update(final DoiHandler doiHandler, final DoiAsyncCallback callback) {
		if (callback == null) {
			throw new IllegalArgumentException("Callback handle must not be null.");
		}
		executor.execute(new Runnable () {
			@Override
			public void run() {
				try {
					doiClient.update(doiHandler);
					callback.onSuccess(doiHandler);
				} catch (Exception e) {
					callback.onError(doiHandler, e);
				}
			}});
	}

	// If the thread pool is to have more than 1 thread,
	// the blocking client must also use a pool of connections.
	// The blocking client currently uses SingleClientConnManager.
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	@Autowired
	private DoiClient doiClient;
}
