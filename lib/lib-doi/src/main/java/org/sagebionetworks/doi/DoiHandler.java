package org.sagebionetworks.doi;

import org.sagebionetworks.repo.model.doi.Doi;

public interface DoiHandler {

	/*
	 * Gets the data transfer object for the DOI
	 */
	public Doi getDto();

	/*
	 * Sets the data transfer object for the DOI
	 */
	public void setDto(Doi dto);

	/*
	 * Gets a string representing the DOI for the object
	 */
	public String getDoi();

	/*
	 * Sets the DOI string
	 */
	public void setDoi(String doi);

	/*
	 * Gets the metadata for the DOI
	 */
	public DoiMetadata getMetadata();

	/*
	 * Sets the metadata for the DOI
	 */
	public void setMetadata(DoiMetadata metadata);
}
