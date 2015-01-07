package com.completetrsst.crypto.xml;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;

public class EllipticCurveKeyCreatorTest {

    private static final Logger log = LoggerFactory.getLogger(EllipticCurveKeyCreatorTest.class);

    private KeyCreator keyCreator;

    private KeyPair keyPair;

    // TODO: Try this at home before java 8 jdk
    // http://www.java2s.com/Tutorial/Java/0490__Security/DSAwithEllipticCurve.htm

    @Before
    public void init() {
        keyCreator = new EllipticCurveKeyCreator();
        keyPair = keyCreator.createKeyPair();
    }

    @Test
    public void createKeyPairPrivateKey() throws Exception {
        PrivateKey key = keyPair.getPrivate();
        assertEquals("EC", key.getAlgorithm());
        assertEquals("PKCS#8", key.getFormat());
    }

    @Test
    public void createKeyPairPublicKey() throws Exception {
        PublicKey key = keyPair.getPublic();
        assertEquals("EC", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
    }

    @Test
    public void testSignature() throws Exception {
        byte[] dataToSign = "derek heston".getBytes("UTF-8");
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(dataToSign);

        byte[] signature = signer.sign();
        signer.initVerify(keyPair.getPublic());
        signer.update(dataToSign);

        boolean isVerified = signer.verify(signature);
        assertTrue(isVerified);
    }
}
