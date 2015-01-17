package com.completetrsst.crypto.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.junit.Before;
import org.junit.Test;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;

public class EllipticCurveKeyCreatorTest {

    private KeyCreator keyCreator;

    private KeyPair keyPair;

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
