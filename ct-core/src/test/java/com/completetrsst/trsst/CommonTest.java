package com.completetrsst.trsst;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;

public class CommonTest {

	@Test
	public void showKeys() {
		KeyPair pair = Common.generateSigningKeyPair();
		PublicKey pubKey = pair.getPublic();
		System.out.println("Format of public key: " + pubKey.getFormat());
		assertEquals("X.509", pubKey.getFormat());
		System.out.println("Algorithm of public key: " + pubKey.getAlgorithm());
		System.out.println("raw bytes:" + StringUtils.newString(pubKey.getEncoded(), "UTF-8"));

		KeyPair encryptKey = Common.generateEncryptionKeyPair();
		PrivateKey privateKey = encryptKey.getPrivate();

		System.out.println();
		System.out.println("Format of private key: " + privateKey.getFormat());
		System.out.println("Algorithm of private key: " + privateKey.getAlgorithm());
		System.out.println("raw bytes:" + StringUtils.newString(privateKey.getEncoded(), "UTF-8"));

		String id = Common.toFeedId(pubKey);
		System.out.println("This id is: " + id);
	}

}
