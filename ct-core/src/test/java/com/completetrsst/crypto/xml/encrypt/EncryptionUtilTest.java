package com.completetrsst.crypto.xml.encrypt;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Nodes.ATOM_CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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

import com.completetrsst.atom.AtomEncrypterTest;
import com.completetrsst.atom.AtomParser;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;

public class EncryptionUtilTest {

    private static final KeyCreator creator = new EllipticCurveKeyCreator();
    private static final KeyPair encryptionKeys = creator.createKeyPair();

    private static final List<PublicKey> recipientPublicKeys = new ArrayList<PublicKey>();
    private static final List<PrivateKey> recipientPrivateKeys = new ArrayList<PrivateKey>();

    private static final AtomParser parser = new AtomParser();

    private static EncryptionUtil util;

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
        util = new EncryptionUtil();
    }

    @Test
    public void testEncrypt() throws Exception {
        Element unencryptedEntry = AtomEncrypterTest.createUnencryptedEntryWithContent("New titles rock");

        Node contentNode = parser.getFirstNode(unencryptedEntry, ATOM_XMLNS, ATOM_CONTENT);
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

        Node contentNode = parser.getFirstNode(entry, ATOM_XMLNS, ATOM_CONTENT);
        assertFalse("my second encrypted entry".equals(contentNode.getTextContent()));

        Element decryptedContent = util.decrypt(entry, encryptionKeys.getPrivate());
        assertTrue("my second encrypted entry".equals(decryptedContent.getTextContent()));
    }

    @Test
    public void decryptWithWrongKey() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my third encrypted entry");

        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        KeyPair newKeyPair = new EllipticCurveKeyCreator().createKeyPair();

        Element content = null;
        try {
            content = util.decrypt(entry, newKeyPair.getPrivate());
            fail("Should have thrown an exception because we didn't use the right key to decrypt");
        } catch (GeneralSecurityException e) {
            // we want to get here
        }
        assertNull(content);
        // Just to be sure the content is still not decrypted
        Element contentDom = (Element) parser.getFirstNode(entry, ATOM_XMLNS, ATOM_CONTENT);
        assertFalse("my third encrypted entry".equals(contentDom.getTextContent()));
    }

    @Test
    public void decryptWithRecipientsKeys() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my fourth encrypted entry");
        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        for (PrivateKey key : recipientPrivateKeys) {
            Element content = util.decrypt(entry, key);
            String contentText = content.getTextContent();
            assertEquals("my fourth encrypted entry", contentText);
        }
    }

    @Test
    public void decryptTextDelegatesToDecrypt() throws Exception {
        Element entry = AtomEncrypterTest.createUnencryptedEntryWithContent("my fifth encrypted entry");
        util.encrypt(entry, encryptionKeys, recipientPublicKeys);

        EncryptionUtil spy = spy(util);
        String result = spy.decryptText(entry, encryptionKeys.getPrivate());
        verify(spy).decrypt(entry, encryptionKeys.getPrivate());
        assertEquals("my fifth encrypted entry", result);
    }

}
