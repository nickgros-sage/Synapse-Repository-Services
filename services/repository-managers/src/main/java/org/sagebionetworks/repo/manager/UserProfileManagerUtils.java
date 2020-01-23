package org.sagebionetworks.repo.manager;

import java.util.Collections;

import org.sagebionetworks.repo.manager.team.TeamConstants;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.util.ValidateArgument;

public class UserProfileManagerUtils {
	
	public static boolean isOwnerOrAdmin(UserInfo userInfo, String ownerId) {
		if (userInfo == null) return false;
		if (userInfo.isAdmin()) return true;
		if (ownerId != null && ownerId.equals(userInfo.getId().toString())) return true;
		return false;
	}

	public static boolean isOwnerACTOrAdmin(UserInfo userInfo, String ownerId) {
		if (userInfo == null) return false;
		if (userInfo.isAdmin()) return true;
		if (userInfo.getGroups().contains(TeamConstants.ACT_TEAM_ID)) return true;
		if (ownerId != null && ownerId.equals(userInfo.getId().toString())) return true;
		return false;
	}

	/**
	 * 
	 * @param userInfo
	 * @param userProfile Note this is treated as MUTABLE
	 */
	public static void clearPrivateFields(UserInfo userInfo, UserProfile userProfile) {		
		if (userProfile != null) {
			boolean canSeePrivate = isOwnerOrAdmin(userInfo, userProfile.getOwnerId());
			if (!canSeePrivate) {
				PrivateFieldUtils.clearPrivateFields(userProfile);			
			}
		}
	}
	
	/**
	 * 
	 * @param userInfo
	 * @param userProfile Note this is treated as MUTABLE
	 */
	public static void clearPrivateFields(VerificationSubmission verificationSubmission) {		
		if (verificationSubmission != null) {
			PrivateFieldUtils.clearPrivateFields(verificationSubmission);			
		}
	}
	
	/**
	 * 
	 * @param userInfo
	 * @param userGroupHeader Note this is treated as MUTABLE
	 */
	public static void clearPrivateFields(UserInfo userInfo, UserGroupHeader userGroupHeader) {		
		if (userGroupHeader != null) {
			boolean canSeePrivate = UserProfileManagerUtils.isOwnerOrAdmin(userInfo, userGroupHeader.getOwnerId());
			if (!canSeePrivate) {
				PrivateFieldUtils.clearPrivateFields(userGroupHeader);		
			}
		}
	}

	/**
	 * If the calling user is not an admin AND the profile is redacted, strip all of the fields of the user profile.
	 * If the user is an admin or the profile is not redacted, the original user bundle is returned.
	 * @param userInfo the UserInfo of the caller
	 * @param userProfile  Note this is treated as MUTABLE. The profile that may contain redacted information.
	 */
	public static void redactProfileIfRedactedAndNonAdmin(UserInfo userInfo, UserProfile userProfile) {
		if (userProfile != null && !userInfo.isAdmin() && userProfile.getIsRedacted() != null && userProfile.getIsRedacted()) {
			redactInfoFromProfile(userProfile);
		}
	}

	/**
	 * Redact personal information from a UserBundle.
	 * @param bundle Note this is treated as MUTABLE. The profile that may contain redacted information.
	 */
	public static void redactInfoFromBundle(UserBundle bundle) {
		bundle.setORCID(null);
		redactInfoFromProfile(bundle.getUserProfile());
	}

	/**
	 * Redact personal information from a UserProfile.
	 * @param profile Note this is treated as MUTABLE. The profile that may contain redacted information.
	 */
	public static UserProfile redactInfoFromProfile(UserProfile profile) {
		ValidateArgument.required("ownerId", profile.getOwnerId());

		String gdprEmail = "gdpr-synapse+" + profile.getOwnerId() + "@sagebase.org";
		profile.setEmail(gdprEmail);
		profile.setEmails(Collections.singletonList(gdprEmail));
		profile.setFirstName("");
		profile.setLastName("");
		profile.setOpenIds(Collections.emptyList());
		profile.setDisplayName(null);
		profile.setIndustry(null);
		profile.setProfilePicureFileHandleId(null);
		profile.setLocation(null);
		profile.setCompany(null);
		profile.setPosition(null);
		profile.setIsRedacted(true);
		return profile;
	}
}
