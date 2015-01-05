package com.completetrsst.crypto;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.trsst.Common;

public class KeyCommanderTest {

	private static final Logger log = LoggerFactory.getLogger(KeyCommanderTest.class);
	
	// examples: http://www.bouncycastle.org/wiki/display/JA1/Elliptic+Curve+Key+Pair+Generation+and+Key+Factories#EllipticCurveKeyPairGenerationandKeyFactories-FromNamedCurves
	@Test
	public void generateBcKeyPair() throws Exception {

		// TODO: Run signatureUtilTests.java
		// update the algorithm to support eliptic key:
		// SignatureUtil - Error attaching signature unsupported key algorithm: ECDSA

		
		KeyPair pair = KeyCommander.generateBcKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();
		// RuntimeException: curve255519 not an OID
		log.info(Common.toX509FromPublicKey(publicKey));
		
	}

}
