package org.sagebionetworks.doi;

import org.sagebionetworks.doi.datacite22.*;
import javax.xml.bind.*;
import java.io.*;
import java.util.*;

/**
 * EZID metadata stored with the DOI.
 */
public class DataciteEzMetadata implements DoiMetadata {

	public String getTitle() {
		if (title == null || title.isEmpty()) {
			throw new NullPointerException("Missing title. Title is required");
		}
		return title;
	}

	public void setTitle(String title) {
		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Missing title. Title is required");
		}
		this.title = title;
	}

	public String getCreator() {
		if (creator == null || creator.isEmpty()) {
			throw new NullPointerException("Missing creator. Creator is required");
		}
		return creator;
	}

	public void setCreator(String creator) {
		if (creator == null || creator.isEmpty()) {
			throw new IllegalArgumentException("Missing creator. Creator is required");
		}
		this.creator = creator;
	}

	public String getTarget() {
		if (target == null || target.isEmpty()) {
			throw new NullPointerException("Missing target. Target is required");
		}
		return target;
	}

	public void setTarget(String target) {
		if (target == null || target.isEmpty()) {
			throw new IllegalArgumentException("Missing target. Target is required");
		}
		this.target = target;
	}

	public String getPublisher() {
		if (publisher == null || publisher.isEmpty()) {
			throw new NullPointerException("Missing publisher. Publisher is required");
		}
		return publisher;
	}

	public void setPublisher(String publisher) {
		if (publisher == null || publisher.isEmpty()) {
			throw new IllegalArgumentException("Missing publisher. Publisher is required");
		}
		this.publisher = publisher;
	}

	public int getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(int publicationYear) {
		this.publicationYear = publicationYear;
	}

	public String getDoi() {
		if (doi == null || doi.isEmpty()) {
			throw new NullPointerException("Missing DOI. DOI is required");
		}
		return doi;
	}

	public void setDoi(String doi) {
		if (doi == null || doi.isEmpty()) {
			throw new IllegalArgumentException("Missing DOI. DOI is required");
		}
		this.doi = doi;
	}

	/**
	 * This is the minimum required metadata to create a DataCite DOI via EZID.
	 * Special characters in the values are encoded by percent-encoding per EZID documentation.
	 */
	public String getMetadataAsString() {
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(FIELD_PROFILE, "datacite");
		metadata.put(FIELD_TARGET, getTarget());
		metadata.put(FIELD_METADATAXML, getMetadataAsXMLString());
		return metadataToAnvl(metadata);
	}

	/**Datacite requires the metadata field to be in XML (adherent to their schema)**/
	private String getMetadataAsXMLString() {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Resource.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			Resource resource = new Resource();
			Resource.Identifier identifier = new Resource.Identifier();
			identifier.setIdentifierType(RelatedIdentifierType.DOI.value());
			identifier.setValue(doi);
			resource.setIdentifier(identifier);

			Resource.Creators.Creator creator = new Resource.Creators.Creator();
			creator.setCreatorName(this.creator);
			Resource.Creators creators = new Resource.Creators();
			creators.getCreator().add(creator);
			resource.setCreators(creators);

			Resource.Titles.Title title = new Resource.Titles.Title();
			title.setValue(this.title);
			Resource.Titles titles = new Resource.Titles();
			titles.getTitle().add(title);
			resource.setTitles(titles);

			resource.setPublisher(publisher);
			resource.setPublicationYear(Integer.toString(publicationYear));

			StringWriter sw = new StringWriter();
			marshaller.marshal(resource, sw);
			return sw.toString();
		} catch (JAXBException e){
			throw new RuntimeException("Error occurred creating metadata XML within DOI schema", e);
		}
	}

	public void initFromString(String metadata) {
		if (metadata == null || metadata.isEmpty()) {
			throw new IllegalArgumentException("Metadata cannot be null.");
		}

		Map<String,String> parsedMetadata = anvlToMetadata(metadata);
		// Old DOIs in Synapse may not have a '_target' field
		if (parsedMetadata.containsKey(FIELD_TARGET) && !parsedMetadata.get(FIELD_TARGET).isEmpty()) {
			setTarget(parsedMetadata.get(FIELD_TARGET));
		}

		// Old DOIs in Synapse may not have a metadata field
		if (parsedMetadata.containsKey(FIELD_METADATAXML) && !parsedMetadata.get(FIELD_METADATAXML).isEmpty()) {
			Resource metadataObj = getMetadataObjectFromXml(parsedMetadata.get(FIELD_METADATAXML));
			setTitle(metadataObj.getTitles().getTitle().get(0).getValue());
			setCreator(metadataObj.getCreators().getCreator().get(0).getCreatorName());
			setPublisher(metadataObj.getPublisher());
			setPublicationYear(Integer.parseInt(metadataObj.getPublicationYear()));
		}
	}

	private Resource getMetadataObjectFromXml(String xml) {
		Resource metadataObject;
		try {
			JAXBContext jc = JAXBContext.newInstance(Resource.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StringReader xmlBuffer = new StringReader(xml);
			metadataObject = (Resource) unmarshaller.unmarshal(xmlBuffer);
		} catch (JAXBException e) {
			throw new RuntimeException("Error occurred parsing XML metadata", e);
		}
		return metadataObject;
	}

	/*
	 * Escape percent, newline, and colon because they cannot be parsed by Datacite EZ API
	 * See ANVL format
	 * From the Datacite EZ API docs
	 */
	private String escape (String s){
		return s.replace("%", "%25").replace("\n", "%0A").
				replace("\r", "%0D").replace(":", "%3A");
	}

	/*
	 * Convert escape characters in retrieved metadata to the unescaped representation
	 * From the Datacite EZ API docs
	 */
	private String unescape (String s) {
		StringBuffer b = new StringBuffer();
		int i;
		while ((i = s.indexOf("%")) >= 0) {
			b.append(s.substring(0, i));
			b.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
			s = s.substring(i+3);
		}
		b.append(s);
		return b.toString();
	}

	/*
	 * Convert a Map of metadata K,V pairs into ANVL to submit to the Datacite EZ API
	 * From the Datacite EZ API docs
	 */
	private String metadataToAnvl(Map<String,String> metadata) {
		Iterator<Map.Entry<String, String>> i = metadata.entrySet().iterator();
		StringBuffer b = new StringBuffer();
		while (i.hasNext()) {
			Map.Entry<String, String> e = i.next();
			b.append(escape(e.getKey()) + ": " + escape(e.getValue()) + "\n");
		}
		return b.toString();
	}

	/*
	 * Parse ANVL metadata into K,V pairs
	 * From the Datacite EZ API docs
	 */
	private Map<String, String> anvlToMetadata(String anvl) {
		HashMap<String, String> metadata = new HashMap<String, String>();
		for (String l : anvl.split("[\\r\\n]+")) {
			String[] kv = l.split(":", 2);
			if(kv.length > 1) {
				metadata.put(unescape(kv[0]).trim(), unescape(kv[1]).trim());
			}
		}
		return metadata;
	}

	private static final String FIELD_TARGET = "_target";
	private static final String FIELD_PROFILE = "_profile";
	private static final String FIELD_METADATAXML = "datacite";


	// Required
	private String title;
	private String creator;
	private String publisher;
	private int publicationYear;
	private String target;
	private String doi;
}
