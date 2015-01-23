package com.completetrsst.crypto.xml.encrypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.atom.AtomEncrypter;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
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

        Node contentNode = TestUtil.getFirstElement(domEntryWithContent, AtomSigner.XMLNS, "content");
        assertEquals("my first encrypted entry", contentNode.getTextContent());
        assertEquals(1, contentNode.getChildNodes().getLength());

        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));

        assertFalse("my first encrypted entry".equals(contentNode.getTextContent()));
        NodeList contentChildren = contentNode.getChildNodes();
        // Should be 2 encrypted data nodes now
        assertEquals(2, contentChildren.getLength());
    }

    @Test
    public void decryptWithProperKey() throws Exception {
        String rawXmlWithContent = atom.newEntry("my second encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);

        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));

        Node contentNode = TestUtil.getFirstElement(domEntryWithContent, AtomSigner.XMLNS, "content");
        assertFalse("my second encrypted entry".equals(contentNode.getTextContent()));

        Element decryptedContent = util.decryptText(domEntryWithContent, keyPair.getPrivate());
        assertTrue("my second encrypted entry".equals(decryptedContent.getTextContent()));
    }

    @Test
    public void decryptWithWrongKey() throws Exception {
        String rawXmlWithContent = atom.newEntry("my third encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);

        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));

        KeyPair newKeyPair = new EllipticCurveKeyCreator().createKeyPair();

        try {
            util.decryptText(domEntryWithContent, newKeyPair.getPrivate());
            fail("Should have thrown an exception because we didn't use the right key to decrypt");
        } catch (GeneralSecurityException e) {
            // we want to get here
        }
    }

    @Test
    public void decryptWitRightKeyAmongOtherKeys() throws Exception {
        String rawXmlWithContent = atom.newEntry("my fourth encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);

        KeyCreator creator = new EllipticCurveKeyCreator();
        List<PublicKey> publicKeys = new ArrayList<PublicKey>();
        publicKeys.add(creator.createKeyPair().getPublic());
        KeyPair correctKeys = creator.createKeyPair();
        publicKeys.add(correctKeys.getPublic());

        util.encrypt(domEntryWithContent, keyPair, publicKeys);

        Node contentNode = TestUtil.getFirstElement(domEntryWithContent, AtomSigner.XMLNS, "content");
        assertFalse("my fourth encrypted entry".equals(contentNode.getTextContent()));

        // This also tests that a key other than the one which signed it can decrypt it
        Element decryptedContent = util.decryptText(domEntryWithContent, correctKeys.getPrivate());
        assertTrue("my fourth encrypted entry".equals(decryptedContent.getTextContent()));

        // And the keypair which encrypted it can still decrypt it, even tho it wasn't in the list of public keys
        // since the encrypt() method adds it automatically
        decryptedContent = util.decryptText(domEntryWithContent, keyPair.getPrivate());
        assertTrue("my fourth encrypted entry".equals(decryptedContent.getTextContent()));

        // Again reinforce that other keys fail
        try {
            decryptedContent = util.decryptText(domEntryWithContent, creator.createKeyPair().getPrivate());
            fail("Exception should be thrown since the given private key can't decrypt the message");
        } catch (GeneralSecurityException e) {
            // we want this, the given private key isn't able to decrypt the message
        }
    }

}
