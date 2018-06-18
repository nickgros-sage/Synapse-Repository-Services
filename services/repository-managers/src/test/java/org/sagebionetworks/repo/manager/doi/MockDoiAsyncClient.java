package org.sagebionetworks.repo.manager.doi;

import org.sagebionetworks.doi.DoiAsyncClient;
import org.sagebionetworks.doi.DoiAsyncCallback;
import org.sagebionetworks.doi.DoiHandler;

public class MockDoiAsyncClient implements DoiAsyncClient {

	private final long delay;

	MockDoiAsyncClient(long delay) {
		this.delay = delay;
	}

	@Override
	public void create(final DoiHandler doi, final DoiAsyncCallback callback) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
					callback.onSuccess(doi);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});
		thread.start();
	}

	@Override
	public void update(final DoiHandler doi, final DoiAsyncCallback callback) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
					callback.onSuccess(doi);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});
		thread.start();
	}
}
