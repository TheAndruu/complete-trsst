package com.completetrsst.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class SignedEntryVerifierTest {
    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
    private static final String ENTRY_TITLE = "foo bar entry title";

    private static SignedEntryVerifier verifier;

    @Before
    public void init() {
        verifier = new SignedEntryVerifier();
    }
    
    @Test 
    public void isFeedVerified() throws Exception {
    	Element dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_VALID);
    	assertTrue(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
    	assertTrue(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_VALID);
    	assertFalse(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_TAMPERED);
    	assertFalse(verifier.isFeedVerified(dom));
    	
    	
    }
    @Test 
    public void areEntriesVerified() throws Exception {
    	Element dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_VALID);
    	assertTrue(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
    	assertFalse(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_VALID);
    	assertTrue(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_TAMPERED);
    	assertFalse(verifier.areEntriesVerified(dom));
    }

    @Test
    public void testRemoveEntryNodes() throws Exception {
        Element signedFeedAndEntries = new SignedEntryPublisher().publishNew(ENTRY_TITLE, keyPair);

        String rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);

        // The signed XML has our entry title in it
        assertTrue(rawXmlWithEntries.contains(ENTRY_TITLE));

        List<Node> removedEntries = verifier.removeEntryNodes(signedFeedAndEntries);
        rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);
        // Signed feed no longer contains entry title
        assertFalse(rawXmlWithEntries.contains(ENTRY_TITLE));

        assertTrue(removedEntries.size() == 1);
        Element entry = (Element) removedEntries.get(0);
        Node titleNode = entry.getElementsByTagNameNS(SignedEntry.XMLNS, "title").item(0);
        assertEquals(ENTRY_TITLE, titleNode.getTextContent());
    }

    @Test
    public void testAddEntryNodes() throws Exception {
        Element signedFeedAndEntries = new SignedEntryPublisher().publishNew(ENTRY_TITLE, keyPair);
        org.jdom2.Element jdomFeed = XmlUtil.toJdom(signedFeedAndEntries);
        jdomFeed.addContent(createJdomEntry("first entry"));
        jdomFeed.addContent(createJdomEntry("second entry"));

        String rawXml = XmlUtil.serializeJdom(jdomFeed);
        assertTrue(rawXml.contains("first entry"));
        assertTrue(rawXml.contains("second entry"));
        assertTrue(rawXml.contains(ENTRY_TITLE));

        Element domFeed = XmlUtil.toDom(jdomFeed);
        List<Node> removedNodes = verifier.removeEntryNodes(domFeed);

        rawXml = XmlUtil.serializeDom(domFeed);
        assertFalse(rawXml.contains("first entry"));
        assertFalse(rawXml.contains("second entry"));
        assertFalse(rawXml.contains(ENTRY_TITLE));

        // 3 nodes = 2 we added in this test and original with ENTRY_TITLE in it
        assertEquals(3, removedNodes.size());
        removedNodes.forEach(node -> assertTrue(node.getFirstChild().getTextContent().contains("entry")));
    }

    private org.jdom2.Element createJdomEntry(String text) {
        org.jdom2.Element entry = new org.jdom2.Element("entry", SignedEntry.XMLNS);
        entry.addContent(new org.jdom2.Text(text));
        return entry;
    }

}
