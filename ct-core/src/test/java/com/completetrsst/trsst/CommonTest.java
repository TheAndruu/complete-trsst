package com.completetrsst.trsst;

import static org.junit.Assert.assertEquals;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonTest {
	private static final Logger log = LoggerFactory.getLogger(CommonTest.class);

	@Test
	public void showKeys() {
		KeyPair pair = Common.generateSigningKeyPair();
		PublicKey pubKey = pair.getPublic();
		log.info("Format of public key: " + pubKey.getFormat());
		assertEquals("X.509", pubKey.getFormat());
		log.info("Algorithm of public key: " + pubKey.getAlgorithm());
		log.info("raw bytes:" + StringUtils.newString(pubKey.getEncoded(), "UTF-8"));

		KeyPair encryptKey = Common.generateEncryptionKeyPair();
		PrivateKey privateKey = encryptKey.getPrivate();

		log.info("Format of private key: " + privateKey.getFormat());
		log.info("Algorithm of private key: " + privateKey.getAlgorithm());
		log.info("raw bytes:" + StringUtils.newString(privateKey.getEncoded(), "UTF-8"));

		String id = Common.toFeedId(pubKey);
		log.info("This id is: " + id);
	}

}
