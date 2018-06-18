package org.sagebionetworks.doi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.doi.Doi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:doi-test-configuration.xml" })
public class DxAsyncClientIntegTest {

	@Autowired DoiHandler doiHandler;
	@Autowired DoiMetadata doiMetadata;

	@Test
	public void testResolvingDoi() {
		final Doi dto = new Doi();
		doiHandler.setDto(dto);
		final String doi = "doi:10.7303/syn1720822.1";
		doiHandler.setDoi(doi);
		doiHandler.setMetadata(doiMetadata);

		long delay = 100L;
		long decay = 50L;
		DxAsyncClient dxClient = new DxAsyncClient(delay, decay);
		dxClient.resolve(doiHandler, new DxAsyncCallback() {
			@Override
			public void onSuccess(DoiHandler doiHandler) {
			}
			@Override
			public void onError(DoiHandler doiHandler, Exception e) {
				fail();
			}
		});
	}

	@Test
	public void testNonResolvingDoi() {
		final Doi dto = new Doi();
		doiHandler.setDto(dto);
		final String doi = "doi:10.7303/duygh989837979";
		doiHandler.setDoi(doi);
		doiHandler.setMetadata(doiMetadata);

		long delay = 100L;
		long decay = 30L;
		DxAsyncClient dxClient = new DxAsyncClient(delay, decay);
		final long start = System.currentTimeMillis();
		dxClient.resolve(doiHandler, new DxAsyncCallback() {
			@Override
			public void onSuccess(DoiHandler doiHandler) {
				fail();
			}
			@Override
			public void onError(DoiHandler doiHandler, Exception e) {
				long stop = System.currentTimeMillis();
				assertTrue((stop - start) > (100 + 70 + 40 + 10));
			}
		});
	}
}
