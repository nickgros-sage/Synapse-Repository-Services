package org.sagebionetworks.doi;

public interface DxAsyncCallback {

	/** When the execution is successful. */
	void onSuccess(DoiHandler doi);

	/** When the execution fails with an error. */
	void onError(DoiHandler doi, Exception e);
}
