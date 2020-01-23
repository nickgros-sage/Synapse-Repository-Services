package org.sagebionetworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sagebionetworks.client.SynapseAdminClient;
import org.sagebionetworks.client.SynapseAdminClientImpl;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseUnauthorizedException;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.auth.LoginRequest;

public class ITUserInfoRedaction {

	private static SynapseAdminClient adminClient;
	private static SynapseClient client;

	private static Long userId;

	private static String username = UUID.randomUUID().toString();
	private static String password = "password" + UUID.randomUUID().toString();

	@BeforeAll
	public static void beforeClass() throws Exception {
		adminClient = new SynapseAdminClientImpl();
		client = new SynapseClientImpl();
		
		SynapseClientHelper.setEndpoints(adminClient);
		SynapseClientHelper.setEndpoints(client);

		adminClient.setUsername(StackConfigurationSingleton.singleton().getMigrationAdminUsername());
		adminClient.setApiKey(StackConfigurationSingleton.singleton().getMigrationAdminAPIKey());
		adminClient.clearAllLocks();
		
		userId = SynapseClientHelper.createUser(adminClient, client, username, password);
	}

	@AfterAll
	public static void afterClass() throws Exception {
		try {
			adminClient.deleteUser(userId);
		} catch (SynapseException e) {
		}
	}

	@Test
	public void testRedactUserInfo() throws SynapseException {
		UserProfile profile = client.getMyProfile();
		profile.setFirstName("First");
		profile.setLastName("Last");
		profile.setDisplayName("Some display name");
		profile.setFirstName("First");
		profile.setLastName("Last");
		profile.setPosition("Job");
		profile.setCompany("Organization");
		profile.setLocation("Seattle");
		profile.setOpenIds(Collections.singletonList("OpenID1"));

		client.updateMyProfile(profile);
		profile = client.getMyProfile();
		// Method under test
		adminClient.redactUserInformation(userId.toString());

		int mask = 0xFFFF; // This should get everything
		// When the admin gets the bundle, it should be the same (except for a few fields)
		UserBundle unredactedActual = adminClient.getUserBundle(userId, mask);
		assertEquals(userId.toString(), unredactedActual.getUserId());
		assertNull(unredactedActual.getORCID());
		UserProfile unredactedProfile = unredactedActual.getUserProfile();
		assertEquals(profile.getFirstName(), unredactedProfile.getFirstName());
		assertEquals(profile.getLastName(), unredactedProfile.getLastName());
		assertEquals(profile.getOpenIds(), unredactedProfile.getOpenIds());
		assertEquals(profile.getEmail(), unredactedProfile.getEmail());
		assertEquals(profile.getEmails(), unredactedProfile.getEmails());
		assertEquals(profile.getUserName(), unredactedProfile.getUserName());
		assertEquals(profile.getUrl(), unredactedProfile.getUrl());
		assertEquals(profile.getRStudioUrl(), unredactedProfile.getRStudioUrl());
		assertEquals(profile.getDisplayName(), unredactedProfile.getDisplayName());
		assertEquals(profile.getLocation(), unredactedProfile.getLocation());
		assertEquals(profile.getProfilePicureFileHandleId(), unredactedProfile.getProfilePicureFileHandleId());
		assertEquals(profile.getIndustry(), unredactedProfile.getIndustry());
		assertEquals(profile.getCompany(), unredactedProfile.getCompany());
		assertEquals(profile.getSummary(), unredactedProfile.getSummary());
		assertTrue(unredactedActual.getUserProfile().getIsRedacted());
		assertFalse(unredactedActual.getUserProfile().getNotificationSettings().getSendEmailNotifications());

		// When another user (including the owner) gets the info, it should be redacted
		UserBundle redactedActual = client.getMyOwnUserBundle(mask);
		UserProfile redactedProfile = redactedActual.getUserProfile();
		String expectedEmail = "gdpr-synapse+" + userId.toString() + "@sagebase.org";
		assertNull(redactedActual.getORCID());
		assertEquals(expectedEmail, redactedProfile.getEmail());
		assertNull(redactedProfile.getFirstName());
		assertNull(redactedProfile.getLastName());
		assertEquals(userId.toString(), redactedProfile.getUserName());
		assertEquals(Collections.emptyList(), redactedProfile.getOpenIds());
		assertFalse(redactedProfile.getNotificationSettings().getSendEmailNotifications());
		Assertions.assertNull(redactedProfile.getDisplayName());
		Assertions.assertNull(redactedProfile.getIndustry());
		Assertions.assertNull(redactedProfile.getProfilePicureFileHandleId());
		Assertions.assertNull(redactedProfile.getLocation());
		Assertions.assertNull(redactedProfile.getCompany());
		Assertions.assertNull(redactedProfile.getPosition());
		Assertions.assertTrue(redactedProfile.getIsRedacted());


		// Verify we cannot log in with the old username, user ID, or email address (the password should be changed)
		client.logout();
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername(username);
		loginRequest.setPassword(password);
		assertThrows(SynapseUnauthorizedException.class, () -> client.login(loginRequest));
		loginRequest.setUsername(userId.toString());
		assertThrows(SynapseUnauthorizedException.class, () -> client.login(loginRequest));
		loginRequest.setUsername(expectedEmail);
		assertThrows(SynapseUnauthorizedException.class, () -> client.login(loginRequest));
	}
}
