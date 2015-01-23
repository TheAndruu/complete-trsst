package com.completetrsst.crypto.xml.encrypt;

import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.atom.AtomEncrypter;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.Crypto;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class EncryptionUtilTest {

    private AtomSigner atom;
    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

    private EncryptionUtil util;

    @Before
    public void init() {
        atom = new AtomEncrypter();
        util = new EncryptionUtil();
    }

    @Test
    public void testEncrypt() throws Exception {
        String rawXmlWithContent = atom.newEntry("my first encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);

        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));

        String rawXmlWithEncryptedContent = TestUtil.format(XmlUtil.serializeDom(domEntryWithContent));

        // TODO: Add to AtomEncrypter
        // TODO: Add asserts
    }

    @Test
    public void decrypt() throws Exception {
        String rawXmlWithContent = atom.newEntry("my second encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);

        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));
        
        Element decrypted = util.decryptText(domEntryWithContent, keyPair.getPrivate());
        System.out.println(TestUtil.format(XmlUtil.serializeDom(decrypted)));
    }

}
