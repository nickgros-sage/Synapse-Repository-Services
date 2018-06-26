package org.sagebionetworks.doi;

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.doi.Doi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:doi-test-configuration.xml" })
public class DataciteEzIntegServiceTest {

	@Autowired
	private DoiHandler doiHandler;

	private final DataciteEzClient dataciteEzClient = new DataciteEzClient();
	private DataciteEzMetadata metadata = new DataciteEzMetadata();
	private final String target = "http://eiqucmdjeisxkd.org/";
	private final String creator = "Last Name, First Name";
	private final String title = "Some test title";
	private final String publisher = "publisher";
	private final String doi = DataciteConstants.DOI_PREFIX + "/" + generateString().substring(16);
	private final int year = Calendar.getInstance().get(Calendar.YEAR);

	private String generateString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Test
	public void resolveOldDoi() {
		Doi dto = new Doi();
		dto.setId("syn300013");
		doiHandler.setDto(dto);
		doiHandler.setDoi("10.7303/syn300013");
		dataciteEzClient.get(doiHandler);
	}

	@Test
	public void resolveDoiWithNoMetadata() {
		Doi dto = new Doi();
		dto.setId("sagetest0006");
		doiHandler.setDto(dto);
		doiHandler.setDoi("10.5072/sagetest0006");
		dataciteEzClient.get(doiHandler);
	}

	@Test
	public void makeResolveDoiTest() throws Exception {
		//Create an object to resolve to a DOI
		Doi dto = new Doi();
		dto.setId(doi);
		doiHandler.setDto(dto);
		metadata.setTarget(target);
		metadata.setCreator(creator);
		metadata.setTitle(title);
		metadata.setPublisher(publisher);
		metadata.setPublicationYear(year);
		metadata.setDoi(doi);
		doiHandler.setDoi(doi);
		doiHandler.setMetadata(metadata);
		dataciteEzClient.create(doiHandler);
		dataciteEzClient.get(doiHandler);
		metadata = (DataciteEzMetadata) doiHandler.getMetadata();
		assertEquals(title, metadata.getTitle());
		assertEquals(creator, metadata.getCreator());
		assertEquals(title, metadata.getTitle());
		assertEquals(publisher, metadata.getPublisher());
		assertEquals(year, metadata.getPublicationYear());
		assertEquals(doi, metadata.getDoi());
	}
}