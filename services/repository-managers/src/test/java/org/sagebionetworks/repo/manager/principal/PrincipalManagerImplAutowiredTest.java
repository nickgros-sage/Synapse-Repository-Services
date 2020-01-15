package org.sagebionetworks.repo.manager.principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.ids.IdType;
import org.sagebionetworks.repo.manager.AuthenticationManager;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.UnauthenticatedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserProfileDAO;
import org.sagebionetworks.repo.model.auth.LoginRequest;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.dao.FileHandleDao;
import org.sagebionetworks.repo.model.dbo.persistence.DBOCredential;
import org.sagebionetworks.repo.model.dbo.persistence.DBOTermsOfUseAgreement;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.message.Settings;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.principal.PrincipalAlias;
import org.sagebionetworks.repo.model.principal.PrincipalAliasDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class PrincipalManagerImplAutowiredTest {

	@Autowired
	private PrincipalManager principalManager;

	@Autowired
	private PrincipalAliasDAO principalAliasDao;

	@Autowired
	private UserManager userManager;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private FileHandleDao fileHandleDao;

	@Autowired
	private UserProfileDAO userProfileDAO;

	@Autowired
	private IdGenerator idGenerator;

	private UserInfo adminUserInfo;
	private UserInfo testUser;

	private DBOCredential credential;
	private final String password = "A User's Pa$$word";

	private String fileHandleId;

	@BeforeEach
	public void before() throws Exception {
		adminUserInfo = userManager.getUserInfo(AuthorizationConstants.BOOTSTRAP_PRINCIPAL.THE_ADMIN_USER.getPrincipalId());

		NewUser nu = new NewUser();
		nu.setEmail(UUID.randomUUID().toString() + "@test.com");
		nu.setUserName(UUID.randomUUID().toString());
		credential = new DBOCredential();
		DBOTermsOfUseAgreement tou = new DBOTermsOfUseAgreement();
		tou.setAgreesToTermsOfUse(Boolean.TRUE);
		testUser = userManager.createOrGetTestUser(adminUserInfo, nu, credential, tou);
		authenticationManager.setPassword(testUser.getId(), password);
		S3FileHandle fh = new S3FileHandle();
		fh.setId(idGenerator.generateNewId(IdType.FILE_IDS).toString());
		fh.setKey("key.jpg");
		fh.setBucketName("bucket");
		fh.setContentType("image/jpg");
		fh.setEtag("etag");
		fh.setCreatedBy(testUser.getId().toString());
		fh.setFileName("profile pic.jpg");
		fileHandleId = fileHandleDao.createFile(fh).getId();
	}
	
	@AfterEach
	public void after() throws Exception {
		principalAliasDao.removeAllAliasFromPrincipal(testUser.getId());
		userProfileDAO.delete(testUser.getId().toString());
		fileHandleDao.delete(fileHandleId);
	}


	@Test
	public void testClearPrincipal() {
		String username = UUID.randomUUID().toString();

		// Add some aliases to make sure they get removed
		PrincipalAlias orcid = new PrincipalAlias();
		orcid.setPrincipalId(testUser.getId());
		orcid.setType(AliasType.USER_ORCID);
		orcid.setAlias("https://orcid.org/0000-0000-0000-0000");
		principalAliasDao.bindAliasToPrincipal(orcid);

		PrincipalAlias usernameAlias = new PrincipalAlias();
		usernameAlias.setPrincipalId(testUser.getId());
		usernameAlias.setType(AliasType.USER_NAME);
		usernameAlias.setAlias(username);
		principalAliasDao.bindAliasToPrincipal(usernameAlias);

		// Verify that we can log in before we change the password
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUsername(username);
		loginRequest.setPassword(password);
		authenticationManager.login(loginRequest);

		// Modify the existing profile to ensure content changes
		UserProfile profile = userProfileDAO.get(testUser.getId().toString());
		profile.setUserName(username);
		profile.setDisplayName("Some display name");
		profile.setFirstName("First");
		profile.setLastName("Last");
		profile.setPosition("Job");
		profile.setCompany("Organization");
		profile.setEmails(Collections.singletonList("email1@gmail.com"));
		profile.setLocation("Seattle");
		profile.setOpenIds(Collections.singletonList("OpenID1"));
		profile.setProfilePicureFileHandleId(fileHandleId);
		profile.setIsRedacted(false);
		Settings notificationSettings = new Settings();
		notificationSettings.setSendEmailNotifications(true);
		profile.setNotificationSettings(notificationSettings);
		profile = userProfileDAO.update(profile);

		UserInfo adminUserInfo = new UserInfo(true);

		// Call under test
		principalManager.redactPrincipalInformation(adminUserInfo, testUser.getId());

		// Verify fields
		UserProfile redactedProfile = userProfileDAO.get(testUser.getId().toString());
		assertEquals(redactedProfile.getEmail(), profile.getEmail());
		assertEquals(redactedProfile.getEmails(), profile.getEmails());
		assertEquals(redactedProfile.getFirstName(), profile.getFirstName());
		assertEquals(redactedProfile.getLastName(), profile.getLastName());
		assertEquals(redactedProfile.getOpenIds(), profile.getOpenIds());
		assertEquals(redactedProfile.getDisplayName(), profile.getDisplayName());
		assertEquals(redactedProfile.getIndustry(), profile.getIndustry());
		assertEquals(redactedProfile.getProfilePicureFileHandleId(), profile.getProfilePicureFileHandleId());
		assertEquals(redactedProfile.getLocation(), profile.getLocation());
		assertEquals(redactedProfile.getCompany(), profile.getCompany());
		assertEquals(redactedProfile.getPosition(), profile.getPosition());

		assertTrue(redactedProfile.getIsRedacted());
		// Send notifications should be FALSE
		assertFalse(redactedProfile.getNotificationSettings().getSendEmailNotifications());
		assertNotEquals(redactedProfile.getNotificationSettings().getSendEmailNotifications(), profile.getNotificationSettings().getSendEmailNotifications());

		// The only principal alias should be the redaction email
		List<PrincipalAlias> pas = principalAliasDao.listPrincipalAliases(testUser.getId());
		assertEquals(1, pas.size());
		assertEquals(AliasType.USER_EMAIL, pas.get(0).getType());
		assertEquals("gdpr-synapse+" + testUser.getId() + "@sagebase.org", pas.get(0).getAlias());

		// Verify that the password has been changed
		assertThrows(UnauthenticatedException.class, () -> authenticationManager.login(loginRequest));
	}
}
