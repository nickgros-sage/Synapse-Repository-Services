package org.sagebionetworks.repo.manager.doi;

import org.sagebionetworks.doi.DxAsyncCallback;
import org.sagebionetworks.doi.DxAsyncClient;
import org.sagebionetworks.doi.DoiHandler;
public class MockDxAsyncClient extends DxAsyncClient {

	private final long delay;

	MockDxAsyncClient(long delay) {
		this.delay = delay;
	}

	@Override
	public void resolve(final DoiHandler doiHandler, final DxAsyncCallback callback) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
					callback.onSuccess(doiHandler);
				} catch (InterruptedException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});
		thread.start();
	}
}
