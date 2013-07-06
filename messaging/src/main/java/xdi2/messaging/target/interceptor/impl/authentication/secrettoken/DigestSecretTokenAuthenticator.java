package xdi2.messaging.target.interceptor.impl.authentication.secrettoken;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.exceptions.Xdi2RuntimeException;

/**
 * A SecretTokenAuthenticator that can authenticate a secret token against
 * a stored digested secret token, using both a global salt and local salts
 * for producing the digest.
 */
public abstract class DigestSecretTokenAuthenticator implements SecretTokenAuthenticator {

	public static String PREFIX_XDI2_DIGEST = "xdi2-digest";

	private static Logger log = LoggerFactory.getLogger(DigestSecretTokenAuthenticator.class.getName());

	private String globalSalt;

	public DigestSecretTokenAuthenticator(String globalSalt) {

		this.globalSalt = globalSalt;
	}

	public DigestSecretTokenAuthenticator() {

	}

	public boolean authenticate(String localSaltAndDigestSecretToken, String secretToken) {

		String[] parts = localSaltAndDigestSecretToken.split(":");

		if (parts.length != 3) {

			if (log.isWarnEnabled()) log.warn("Invalid digest format (not 3 parts).");

			return false;
		}

		if (! PREFIX_XDI2_DIGEST.equals(parts[0])) {

			if (log.isWarnEnabled()) log.warn("Invalid digest format (prefix '" + parts[0] + "' doesn't match expected '" + PREFIX_XDI2_DIGEST + "')");

			return false;
		}

		String localSalt = parts[1];
		String digestSecretToken = parts[2];

		return digestSecretToken.equals(digestSecretToken(secretToken, this.getGlobalSalt(), localSalt));
	}

	public static String localSaltAndDigestSecretToken(String secretToken, String globalSalt) {

		String localSalt = randomSalt();

		return localSaltAndDigestSecretToken(secretToken, globalSalt, localSalt);
	}

	public static String localSaltAndDigestSecretToken(String secretToken, String globalSalt, String localSalt) {

		String digestSecretToken = digestSecretToken(secretToken, globalSalt, localSalt);

		return PREFIX_XDI2_DIGEST + ":" + localSalt + ":" + digestSecretToken;
	}

	public static String digestSecretToken(String secretToken, String globalSalt, String localSalt) {

		if (! isValidSalt(globalSalt)) throw new Xdi2RuntimeException("Invalid global salt.");
		if (! isValidSalt(localSalt)) throw new Xdi2RuntimeException("Invalid local salt.");

		try {

			return DigestUtils.sha512Hex(globalSalt + ":" + localSalt + ":" + DigestUtils.sha512Hex(globalSalt + ":" + Base64.encodeBase64String(secretToken.getBytes("UTF-8"))));
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static String randomSalt() {

		return UUID.randomUUID().toString();
	}

	public static boolean isValidSalt(String salt) {

		try {

			UUID.fromString(salt);
		} catch (IllegalArgumentException ex) {

			return false;
		}

		if (salt.length() != 36) return false;

		return true;
	}

	public static void main(String[] args) {

		if (args.length < 2 || args.length > 3) {

			System.out.println("Parameters: secretToken globalSalt [localSalt]");
			return;
		}

		String secretToken = args[0];
		String globalSalt = args[1];
		String localSalt = args.length > 2 ? args[2] : null;

		String localSaltAndDigestSecretToken;

		if (localSalt == null)
			localSaltAndDigestSecretToken = localSaltAndDigestSecretToken(secretToken, globalSalt);
		else
			localSaltAndDigestSecretToken = localSaltAndDigestSecretToken(secretToken, globalSalt, localSalt);

		System.out.println(localSaltAndDigestSecretToken);
	}

	public String getGlobalSalt() {

		return this.globalSalt;
	}

	public void setGlobalSalt(String globalSalt) {

		this.globalSalt = globalSalt;
	}
}
