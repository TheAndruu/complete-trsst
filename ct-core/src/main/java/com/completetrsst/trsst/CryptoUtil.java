package com.completetrsst.trsst;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtil {

	private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);

	public static final String CURVE_NAME = "secp256k1";

	static {
		try {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		} catch (Exception e) {
			log.error("Could not initialize security provider: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public static final KeyPair generateSigningKeyPair() {
		KeyPairGenerator generator = new org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.EC();
		try {
			generator.initialize(new ECGenParameterSpec(CURVE_NAME));
		} catch (InvalidAlgorithmParameterException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
		return generator.generateKeyPair();
	}

}
