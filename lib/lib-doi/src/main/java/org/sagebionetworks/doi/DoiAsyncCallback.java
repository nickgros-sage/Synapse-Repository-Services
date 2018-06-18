package org.sagebionetworks.doi;

/**
 * Callback handle for the asynchronous client.
 */
public interface DoiAsyncCallback {

	/** When the execution is successful. */
	void onSuccess(DoiHandler doiHandler);

	/** When the execution fails with an error. */
	void onError(DoiHandler doiHandler, Exception e);
}
