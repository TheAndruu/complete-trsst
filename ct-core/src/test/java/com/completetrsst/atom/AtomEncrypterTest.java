package com.completetrsst.atom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;

public class AtomEncrypterTest {

    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
    private AtomEncrypter encrypter;

    @Before
    public void init() {
        encrypter = new AtomEncrypter();
    }

    @Test
    public void testCreateEntry() throws Exception {
        Entry entry = encrypter.createEntry("your title goes here");
        assertEquals(AtomEncrypter.ENCRYPTED_TITLE, entry.getTitle());

        List<Content> contents = entry.getContents();
        assertEquals(1, contents.size());
        Content content = contents.get(0);
        assertEquals("text", content.getType());
        assertEquals("your title goes here", content.getValue());
    }

    @Test
    public void signedElementHasTitleAsContent() throws Exception {
        String rawXml = encrypter.newEntry("My other new title", keyPair);
        Element entryNode = XmlUtil.toDom(rawXml);
        NodeList contentNodeList = entryNode.getElementsByTagNameNS(AtomSigner.XMLNS, "content");
        assertEquals(1, contentNodeList.getLength());
        Node contentNode = contentNodeList.item(0);

        Node titleNode = TestUtil.getFirstElement(entryNode, AtomSigner.XMLNS, "title");
        assertEquals(AtomEncrypter.ENCRYPTED_TITLE, titleNode.getTextContent());
        
        // TODO: Once encryption happens, the 'type' should be 'application/xenc+xml'
        // attribute is a node of "type='text'"
        assertEquals("text", contentNode.getAttributes().item(0).getNodeValue());
        // TODO: Once encryption happens, assert this doesn't equal the given title
        assertEquals("My other new title", contentNode.getTextContent());
    }

    @Test
    public void signedElementSignatureVerifies() throws Exception {
        String rawXml = encrypter.newEntry("And yet another new title", keyPair);
        Element entryNode = XmlUtil.toDom(rawXml);
        AtomVerifier verifier = new AtomVerifier();
        // Assert the entry verifies
        boolean entryValid = verifier.areEntriesVerified(entryNode);
        assertTrue(entryValid);
        // Assert the feed verifies
        boolean feedValid = verifier.isFeedVerified(entryNode);
        assertTrue(feedValid);
    }

}
