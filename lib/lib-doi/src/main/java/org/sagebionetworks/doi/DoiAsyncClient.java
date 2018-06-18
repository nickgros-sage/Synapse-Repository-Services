package org.sagebionetworks.doi;

public interface DoiAsyncClient {

	/**
	 * Creates a new DOI from the supplied data.
	 */
	void create(DoiHandler doiHandler, DoiAsyncCallback callback);

	/**
	 * Updates with the DOI with the supplied data.
	 */
	void update(DoiHandler doiHandler, DoiAsyncCallback callback);
}
