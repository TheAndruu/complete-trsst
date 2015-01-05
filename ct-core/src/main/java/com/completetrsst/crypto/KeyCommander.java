package com.completetrsst.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared utilities and constants used by both clients and servers. Much
 * bogarted from mpowers.
 */
public class KeyCommander {

	private static final Logger log = LoggerFactory.getLogger(KeyCommander.class);

	// TODO: What would it take to use Curve255519?
	public static final String CURVE_256K1 = "secp256k1";
	

	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			log.error("Could not initialize security provider: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	public static final KeyPair getKeyPair() {
		try {
	        return getKeyPairOrig();
	        // TODO: Toggle me below
//	        return generateBcKeyPair();
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	public static final KeyPair generateBcKeyPair() {
		try {
			// "prime192v1");
			// best? Curve25519
			// TODO: Discover the best curve to use
//			Curve25519.
			ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_256K1);
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
			g.initialize(ecSpec, new SecureRandom());
			return g.generateKeyPair();
		} catch (Exception e) {
			log.error("Couldn't create keypair: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	// the version from trsst
	public static final KeyPair getKeyPair2() {
		KeyPairGenerator generator = new KeyPairGeneratorSpi.EC();
		try {
			generator.initialize(new ECGenParameterSpec(CURVE_256K1));
		} catch (InvalidAlgorithmParameterException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return generator.generateKeyPair();
	}

	/**
	 * Converts an EC PublicKey to an X509-encoded string.
	 */
	private static String toX509FromPublicKey(PublicKey publicKey) throws GeneralSecurityException {
		KeyFactory factory = KeyFactory.getInstance("EC");
		X509EncodedKeySpec spec = factory.getKeySpec(publicKey, X509EncodedKeySpec.class);
		return new Base64(0, null, true).encodeToString(spec.getEncoded());
	}

	// TODO: Replace this with EC above
	// originally working / configured version
	public static KeyPair getKeyPairOrig() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		kpg.initialize(512);
		KeyPair kp = kpg.generateKeyPair();
		return kp;
	}
}
