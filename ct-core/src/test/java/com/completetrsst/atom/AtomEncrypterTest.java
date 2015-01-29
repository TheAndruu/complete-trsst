package com.completetrsst.atom;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Namespaces.ENCRYPT_XMLNS;
import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;
import static com.completetrsst.constants.Nodes.*;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
import com.completetrsst.crypto.xml.encrypt.EncryptionUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;

public class AtomEncrypterTest {
    private static final KeyCreator creator = new EllipticCurveKeyCreator();
    private static final KeyPair signingKeys = creator.createKeyPair();
    private static final KeyPair encryptionKeys = creator.createKeyPair();

    private static final AtomParser parser = new AtomParser();
    private AtomEncrypter encrypter;

    private static final List<PublicKey> recipientPublicKeys = new ArrayList<PublicKey>();
    private static final List<PrivateKey> recipientPrivateKeys = new ArrayList<PrivateKey>();

    private static final EncryptionUtil util = new EncryptionUtil();

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
        encrypter = new AtomEncrypter();
    }

    @Test
    public void createEntryTitleInContent() throws Exception {
        Entry entry = encrypter.createEntryTitleInContent("your title goes here", signingKeys.getPublic(), "");
        assertEquals(ENCRYPTED_TITLE, entry.getTitle());

        List<Content> contents = entry.getContents();
        assertEquals(1, contents.size());
        Content content = contents.get(0);
        assertEquals("text", content.getType());
        assertEquals("your title goes here", content.getValue());
    }

    @Test
    public void createEncryptedEntryIsProperlySignedTestFeedFirst() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("And yet another new title", "", signingKeys, encryptionKeys, recipientPublicKeys);

        // Assert the feed verifies
        AtomVerifier verifier = new AtomVerifier();
        boolean feedValid = verifier.isFeedVerified(entryNode);
        assertTrue(feedValid);

        // Assert the entry verifies
        boolean entryValid = verifier.areEntriesVerified(entryNode);
        assertTrue(entryValid);
    }

    @Test
    public void createEncryptedEntryIsProperlySignedTestEntryFirst() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("And yet another new title", "", signingKeys, encryptionKeys, recipientPublicKeys);

        AtomVerifier verifier = new AtomVerifier();
        // Assert the entry verifies
        boolean entryValid = verifier.areEntriesVerified(entryNode);
        assertTrue(entryValid);

        // Assert the feed verifies
        boolean feedValid = verifier.isFeedVerified(entryNode);
        assertTrue(feedValid);
    }

    @Test
    public void createEncryptedEntryDelegates() throws Exception {
        AtomEncrypter spy = spy(encrypter);
        String rawXml = spy.createEncryptedEntry("another new title", "", signingKeys, encryptionKeys, recipientPublicKeys);

        // ensure we call the guy we want
        verify(spy).createEncryptedEntryAsDom("another new title", "", signingKeys, encryptionKeys, recipientPublicKeys);

        // Ensure it contains the encrypted text as expected
        assertTrue(rawXml.contains("Encrypted content"));
        assertFalse(rawXml.contains("another new title"));
    }

    @Test
    public void createEntryHasPredecessorOnFeed() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("And yet another new title", "prev sig value here", signingKeys, encryptionKeys,
                recipientPublicKeys);
        Element preNode = (Element) parser.getFirstNode(entryNode, TRSST_XMLNS, TRSST_PREDECESSOR);

        assertEquals(preNode.getTextContent(), "prev sig value here");
    }

    /**
     * This is an important test, since moreoften we'll be verifying signatures after they've been serialized across a wire The effect of doing so
     * often means XMLNS declarations will be moved around, breaking signatures in the process
     */
    @Test
    public void createEncryptedEntryIsProperlySignedAfterSerialization() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("And yet another new title", "", signingKeys, encryptionKeys, recipientPublicKeys);

        entryNode = XmlUtil.toDom(XmlUtil.serializeDom(entryNode));

        // Assert the feed verifies
        AtomVerifier verifier = new AtomVerifier();
        boolean feedValid = verifier.isFeedVerified(entryNode);
        assertTrue(feedValid);

        // Assert the entry verifies
        boolean entryValid = verifier.areEntriesVerified(entryNode);
        assertTrue(entryValid);
    }

    @Test
    public void createEncryptedEntryHasExpectedTitle() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles rock", "", signingKeys, encryptionKeys, recipientPublicKeys);
        AtomParser parser = new AtomParser();
        List<Node> entryNodes = parser.removeEntryNodes(entryNode);
        assertEquals(1, entryNodes.size());
        // Get the entry node before grabbing the title
        // otherwise, if we ever add titles to feeds, the TestUtil.getFirstElement() would get the feed's title
        assertEquals(ENCRYPTED_TITLE, parser.getFirstNode((Element) entryNodes.get(0), ATOM_XMLNS, ATOM_TITLE).getTextContent());
    }

    @Test
    public void createEncryptedEntryHasEncryptedContent() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles rock", "", signingKeys, encryptionKeys, recipientPublicKeys);

        Element contentDom = (Element) parser.getFirstNode(entryNode, ATOM_XMLNS, ATOM_CONTENT);
        assertFalse("Titles rock".equals(contentDom.getTextContent()));

        NodeList contentChildren = contentDom.getElementsByTagNameNS(ENCRYPT_XMLNS, "EncryptedData");
        // Number encrypted data nodes = num recipients keys + author's public key + node containing actual encrypted content
        assertEquals(recipientPublicKeys.size() + 2, contentChildren.getLength());
    }

    @Test
    public void encryptedEntryIsDecryptableByRecipients() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles rock", "", signingKeys, encryptionKeys, recipientPublicKeys);

        for (PrivateKey key : recipientPrivateKeys) {
            Element content = util.decrypt(entryNode, key);
            String contentText = content.getTextContent();
            assertEquals("Titles rock", contentText);
        }

    }

    @Test
    public void encryptedEntryIsDecryptableByAuthorEncryptKey() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles rock", "", signingKeys, encryptionKeys, recipientPublicKeys);

        Element content = util.decrypt(entryNode, encryptionKeys.getPrivate());
        String contentText = content.getTextContent();
        assertEquals("Titles rock", contentText);
    }

    @Test
    public void encryptedEntryIsNotDecryptableBySigningKey() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles2 rock", "", signingKeys, encryptionKeys, recipientPublicKeys);

        Element content = null;
        try {
            content = util.decrypt(entryNode, signingKeys.getPrivate());
            fail("Should throw an exception as we can't decrypt with this key");
        } catch (GeneralSecurityException e) {
            // We hope to get here
        }
        assertNull(content);

        // Just to be sure the content is still not decrypted
        Element contentDom = (Element) parser.getFirstNode(entryNode, ATOM_XMLNS, ATOM_CONTENT);
        assertFalse("Titles rock".equals(contentDom.getTextContent()));
    }

    @Test
    public void encryptedEntryIsNotDecryptableByRandomKey() throws Exception {
        Element entryNode = encrypter.createEncryptedEntryAsDom("Titles rock", "", signingKeys, encryptionKeys, recipientPublicKeys);

        Element content = null;
        try {
            content = util.decrypt(entryNode, creator.createKeyPair().getPrivate());
            fail("Should throw an exception as we can't decrypt with this key");
        } catch (GeneralSecurityException e) {
            // We hope to get here
        }
        assertNull(content);

        // Just to be sure the content is still not decrypted
        Element contentDom = (Element) parser.getFirstNode(entryNode, ATOM_XMLNS, ATOM_CONTENT);
        assertFalse("Titles rock".equals(contentDom.getTextContent()));
    }

    // For help with some other test classes
    public static Element createUnencryptedEntryWithContent(String title) throws IOException {
        Entry entry = new AtomEncrypter().createEntryTitleInContent(title, signingKeys.getPublic(), "");
        return new AtomSigner().toDom(entry);
    }
}
