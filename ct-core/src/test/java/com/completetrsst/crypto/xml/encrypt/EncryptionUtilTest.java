package com.completetrsst.crypto.xml.encrypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
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
import com.completetrsst.atom.AtomEncrypterTest;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
import com.completetrsst.xml.TestUtil;

public class EncryptionUtilTest {

    private static final KeyCreator creator = new EllipticCurveKeyCreator();
    private static final KeyPair encryptionKeys = creator.createKeyPair();

    private static final List<PublicKey> recipientPublicKeys = new ArrayList<PublicKey>();
    private static final List<PrivateKey> recipientPrivateKeys = new ArrayList<PrivateKey>();

    private static EncryptionUtil util;
    private AtomEncrypter atom;

    static {
        // Add a few public keys to decrypt the messages
        for (int i = 0; i < 3; i++) {
            KeyPair keyPair = creator.createKeyPair();
            recipientPublicKeys.add(keyPair.getPublic());
            recipientPrivateKeys.add(keyPair.getPrivate());
        }
    }

    @Before
    public void init() {
        atom = new AtomEncrypter();
        util = new EncryptionUtil();
    }

    @Test
    public void testEncrypt() throws Exception {
        Element unencryptedEntry = AtomEncrypterTest.createUnencryptedEntryWithContent("New titles rock");

        Node contentNode = TestUtil.getFirstElement(unencryptedEntry, AtomSigner.XMLNS, "content");
        assertEquals("New titles rock", contentNode.getTextContent());
        assertEquals(1, contentNode.getChildNodes().getLength());

        util.encrypt(unencryptedEntry, encryptionKeys, Collections.singletonList(encryptionKeys.getPublic()));

        assertFalse("New titles rock".equals(contentNode.getTextContent()));
        NodeList contentChildren = contentNode.getChildNodes();
        // Should be 2 encrypted data nodes now, one for the public key and one for the content
        assertEquals(2, contentChildren.getLength());
    }

    @Test
    public void decryptWithSignersPrivateKey() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my second encrypted entry");

        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        Node contentNode = TestUtil.getFirstElement(entry, AtomSigner.XMLNS, "content");
        assertFalse("my second encrypted entry".equals(contentNode.getTextContent()));

        Element decryptedContent = util.decryptText(entry, encryptionKeys.getPrivate());
        assertTrue("my second encrypted entry".equals(decryptedContent.getTextContent()));
    }

    @Test
    public void decryptWithWrongKey() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my third encrypted entry");

        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        KeyPair newKeyPair = new EllipticCurveKeyCreator().createKeyPair();

        Element content = null;
        try {
            content = util.decryptText(entry, newKeyPair.getPrivate());
            fail("Should have thrown an exception because we didn't use the right key to decrypt");
        } catch (GeneralSecurityException e) {
            // we want to get here
        }
        assertNull(content);
        // Just to be sure the content is still not decrypted
        Element contentDom = (Element) TestUtil.getFirstElement(entry, AtomSigner.XMLNS, "content");
        assertFalse("my third encrypted entry".equals(contentDom.getTextContent()));
    }

    @Test
    public void decryptWithRecipientsKeys() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my fourth encrypted entry");

        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        for (PrivateKey key : recipientPrivateKeys) {
            Element content = util.decryptText(entry, key);
            String contentText = content.getTextContent();
            assertEquals("my fourth encrypted entry", contentText);
        }

    }

}
