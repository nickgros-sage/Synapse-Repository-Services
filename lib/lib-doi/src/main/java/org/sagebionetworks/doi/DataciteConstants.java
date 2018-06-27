package org.sagebionetworks.doi;

import org.sagebionetworks.StackConfigurationSingleton;

/**
 * Constants for EZID REST APIs.
 */
public class DataciteConstants {

	/**
	 * DOI prefix plus the separator (/).
	 */
	public static final String DOI_PREFIX = StackConfigurationSingleton.singleton().getDataciteDoiPrefix();

	/**
	 * Synapse web portal URL with protocol and host name and the path prefix '#!Synapse:'.
	 */
	public static final String TARGET_URL_PREFIX = StackConfigurationSingleton.singleton().getDataciteTargetUrlPrefix() + "/#!Synapse:";

	/**
	 * DOI Publisher is always Sage Bionetworks.
	 */
	public static final String PUBLISHER = "Synapse";

	/**
	 * Default DOI creator (author) when the corresponding information is missing in Synapse.
	 */
	public static final String DEFAULT_CREATOR = "(author name not available)";

	/**
	 * Base URL (with the trailing slash) for the EZID REST APIs.
	 */
	public static final String DATACITE_EZ_URL = StackConfigurationSingleton.singleton().getDataciteEzUrl();
	public static final String DATACITE_MDS_URL = StackConfigurationSingleton.singleton().getDataciteMdsUrl();

	/**
	 * EZID account user name.
	 */
	public static final String DATACITE_USERNAME = StackConfigurationSingleton.singleton().getDataciteUsername();

	/**
	 * EZID account password.
	 */
	public static final String DATACITE_PASSWORD = StackConfigurationSingleton.singleton().getDatacitePassword();

	/**
	 * URL (with the trailing slash) for the DOI name resolution service.
	 */
	public static final String DX_URL = "https://doi.org/";
}
