package org.sagebionetworks.doi;

/**
 * Client for DOIs.
 */
public interface DoiClient {

	/**
	 * Probes the DOI provider server status.
	 */
	boolean isStatusOk();

	/**
	 * Gets the DOI metadata given the DOI string.
	 */
	DoiHandler get(DoiHandler doiHandler);

	/**
	 * Creates a new DOI from the supplied data.
	 */
	void create(DoiHandler doiHandler);

	/**
	 * Updates with the DOI with the supplied data.
	 */
	void update(DoiHandler doiHandler);
}
