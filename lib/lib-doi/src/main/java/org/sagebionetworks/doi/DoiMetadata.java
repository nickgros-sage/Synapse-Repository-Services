package org.sagebionetworks.doi;

/*
 * Metadata fields necessary for a valid DOI object
 */
public interface DoiMetadata {
	String getTitle();

	void setTitle(String title);

	String getCreator();

	void setCreator(String creator);

	String getTarget();

	void setTarget(String target);

	String getPublisher();

	void setPublisher(String publisher);

	int getPublicationYear();

	void setPublicationYear(int publicationYear);

	String getMetadataAsString();
}
