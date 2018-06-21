package org.sagebionetworks.doi;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.doi.Doi;

public class DoiHandlerTest {

	private DoiHandler DoiHandler;
	private final Doi doiDto = new Doi();
	private final String doi = "doi:10.9999/test.1234567";
	private final EzidMetadata metadata = new EzidMetadata();

	@Before
	public void before() {
		DoiHandler = new DoiHandler();
		DoiHandler.setDto(doiDto);
		DoiHandler.setDoi(doi);
		DoiHandler.setMetadata(metadata);
	}

	@Test
	public void testGetSet() {
		assertEquals(doiDto, DoiHandler.getDto());
		Doi doiDto = new Doi();
		DoiHandler.setDto(doiDto);
		assertEquals(doiDto, DoiHandler.getDto());
		assertEquals(doi, DoiHandler.getDoi());
		assertEquals(metadata, DoiHandler.getMetadata());
		DoiHandler.setDoi("doi");
		assertEquals("doi", DoiHandler.getDoi());
		EzidMetadata metadata = new EzidMetadata();
		DoiHandler.setMetadata(metadata);
		assertEquals(metadata, DoiHandler.getMetadata());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetDto() {
		DoiHandler doi = new DoiHandler();
		doi.setDto(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetDto() {
		DoiHandler doi = new DoiHandler();
		doi.getDto();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetDoi() {
		DoiHandler doi = new DoiHandler();
		doi.setDoi(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetDoi() {
		DoiHandler doi = new DoiHandler();
		doi.getDoi();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRequriedSetMetadata() {
		DoiHandler doi = new DoiHandler();
		doi.setMetadata(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRequriedGetMetadata() {
		DoiHandler doi = new DoiHandler();
		doi.getMetadata();
	}
}
