package org.sagebionetworks.repo.manager.authentication;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.sagebionetworks.repo.manager.token.TokenGenerator;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserGroupDAO;
import org.sagebionetworks.repo.model.auth.AuthenticationDAO;
import org.sagebionetworks.repo.model.auth.AuthenticationReceiptDAO;
import org.sagebionetworks.repo.model.auth.PasswordResetSignedToken;
import org.sagebionetworks.repo.model.dbo.DBOBasicDao;
import org.sagebionetworks.repo.model.dbo.auth.DBOAuthenticationDAOImpl;
import org.sagebionetworks.securitytools.HMACUtils;
import org.sagebionetworks.util.Clock;
import org.springframework.beans.factory.annotation.Autowired;

public class PasswordResetTokenGeneratorImpl implements PasswordResetTokenGenerator {
	@Autowired
	TokenGenerator tokenGenerator;

	@Autowired
	AuthenticationDAO authenticationDAO;

	public static final long PASSWORD_RESET_TOKEN_EXPIRATION_MILLIS = 20 * 60 * 1000; //20 minutes

	private static final String DELIMITER = ".";

	@Override
	public PasswordResetSignedToken getToken(long userId){
		PasswordResetSignedToken token = createUnsignedToken(userId);
		tokenGenerator.signToken(token);
		return token;
	}

	@Override
	public boolean isValidToken(PasswordResetSignedToken token){
		try {
			tokenGenerator.validateToken(token);
		} catch (UnauthorizedException e){
			return false;
		}
		String currentValidityHash = createValidityHash(Long.parseLong(token.getUserId()));
		return currentValidityHash.equals(token.getValidity());
	}

	PasswordResetSignedToken createUnsignedToken(long userId){
		Date now = new Date();
		PasswordResetSignedToken token = new PasswordResetSignedToken();
		token.setUserId(Long.toString(userId));
		token.setCreatedOn(now);
		token.setExpiresOn(new Date(now.getTime() + PASSWORD_RESET_TOKEN_EXPIRATION_MILLIS));
		token.setValidity(createValidityHash(userId));

		return token;
	}

	String createValidityHash(long userId){
		/*
		The concatenation of the user's password hash and the user's authentication receipt.
		This ensures that in the event of:
		1. Password change - Password hash changes even if it is changed to the exact same password since the salt is randomly generated on every password change
		2. Successful login - Last login timestamp changes
		Any of these events occurring would therefore change the validity hash and consequently invalidate the token.
		 */
		String data = authenticationDAO.getPasswordHash(userId) + DELIMITER + authenticationDAO.getLastLoginTimestamp(userId);
		return DigestUtils.sha256Hex(data);
	}


}
