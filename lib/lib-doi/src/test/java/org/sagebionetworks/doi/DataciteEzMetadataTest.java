package org.sagebionetworks.doi;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.doi.datacite22.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataciteEzMetadataTest {

	private DataciteEzMetadata metadata;
	private final String target = "http://eiqucmdjeisxkd.org/";
	private final String creator = "Last Name, First Name";
	private final String title = "Some test title";
	private final String publisher = "publisher";
	private final String doi = "10.9999/abcdef";
	private final int year = Calendar.getInstance().get(Calendar.YEAR);

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

	@Before
	public void before() {
		metadata = new DataciteEzMetadata();
		metadata.setTarget(target);
		metadata.setCreator(creator);
		metadata.setTitle(title);
		metadata.setPublisher(publisher);
		metadata.setPublicationYear(year);
		metadata.setDoi(doi);
	}

	@Test
	public void testGetMetadataAsString() throws Exception {
		String plainText = metadata.getMetadataAsString();
		assertNotNull(plainText);
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new StringReader(plainText));
		String line = reader.readLine();
		while (line != null) {
			String[] splits = line.split(":");
			assertNotNull(splits);
			assertTrue(splits.length > 1);
			map.put(splits[0].trim(), splits[1].trim());
			line = reader.readLine();
		}
		reader.close();
		assertEquals("datacite", map.get("_profile"));
		assertEquals(URLEncoder.encode(target, "UTF-8"), map.get("_target"));
		Resource parsedMetadata = getMetadataObjectFromXml(URLDecoder.decode(map.get("datacite"), "UTF-8"));
		assertEquals(creator, parsedMetadata.getCreators().getCreator().get(0).getCreatorName());
		assertEquals(title, parsedMetadata.getTitles().getTitle().get(0).getValue());
		assertEquals(publisher, parsedMetadata.getPublisher());
		assertEquals(doi, parsedMetadata.getIdentifier().getValue());
		assertEquals(year, Integer.parseInt(parsedMetadata.getPublicationYear()));
	}

	@Test
	public void testInitFromString() throws Exception {
		String plainText = metadata.getMetadataAsString();
		plainText = plainText + "\r\n_status: public";
		DataciteEzMetadata metadata = new DataciteEzMetadata();
		metadata.initFromString(plainText);
		assertEquals(title, metadata.getTitle());
		assertEquals(creator, metadata.getCreator());
		assertEquals(publisher, metadata.getPublisher());
		assertEquals(year, metadata.getPublicationYear());
		assertEquals(target, metadata.getTarget());
	}

	@Test
	public void testInitFromEmptyString() throws Exception {
		DataciteEzMetadata emptyMetadata = new DataciteEzMetadata();
		String plainText = metadata.getMetadataAsString();
		plainText = plainText + "\r\n_status: public";
		DataciteEzMetadata metadata = new DataciteEzMetadata();
		metadata.initFromString(plainText);
		assertEquals(title, metadata.getTitle());
		assertEquals(creator, metadata.getCreator());
		assertEquals(publisher, metadata.getPublisher());
		assertEquals(year, metadata.getPublicationYear());
		assertEquals(target, metadata.getTarget());
	}

	@Test
	public void testGetSet() {
		assertEquals(title, metadata.getTitle());
		metadata.setTitle("title");
		assertEquals("title", metadata.getTitle());
		assertEquals(creator, metadata.getCreator());
		metadata.setCreator("creator");
		assertEquals("creator", metadata.getCreator());
		assertEquals(publisher, metadata.getPublisher());
		metadata.setPublisher("publisher");
		assertEquals("publisher", metadata.getPublisher());
		assertEquals(year, metadata.getPublicationYear());
		metadata.setPublicationYear(-300); // 300 BC
		assertEquals(-300, metadata.getPublicationYear());
		assertEquals(target, metadata.getTarget());
		metadata.setTarget("target");
		assertEquals("target", metadata.getTarget());
		assertEquals(doi, metadata.getDoi());
		metadata.setDoi("doi");
		assertEquals("doi", metadata.getDoi());
	}
}
