package org.sagebionetworks.doi;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.doi.Doi;

public class EzidDoiHandlerTest {

	private EzidDoiHandler ezidDoiHandler;
	private final Doi doiDto = new Doi();
	private final String doi = "doi:10.9999/test.1234567";
	private final EzidMetadata metadata = new EzidMetadata();

	@Before
	public void before() {
		ezidDoiHandler = new EzidDoiHandler();
		ezidDoiHandler.setDto(doiDto);
		ezidDoiHandler.setDoi(doi);
		ezidDoiHandler.setMetadata(metadata);
	}

	@Test
	public void testGetSet() {
		assertEquals(doiDto, ezidDoiHandler.getDto());
		Doi doiDto = new Doi();
		ezidDoiHandler.setDto(doiDto);
		assertEquals(doiDto, ezidDoiHandler.getDto());
		assertEquals(doi, ezidDoiHandler.getDoi());
		assertEquals(metadata, ezidDoiHandler.getMetadata());
		ezidDoiHandler.setDoi("doi");
		assertEquals("doi", ezidDoiHandler.getDoi());
		EzidMetadata metadata = new EzidMetadata();
		ezidDoiHandler.setMetadata(metadata);
		assertEquals(metadata, ezidDoiHandler.getMetadata());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetDto() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.setDto(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetDto() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.getDto();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetDoi() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.setDoi(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetDoi() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.getDoi();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetMetadata() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.setMetadata(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetMetadata() {
		EzidDoiHandler doi = new EzidDoiHandler();
		doi.getMetadata();
	}
}
