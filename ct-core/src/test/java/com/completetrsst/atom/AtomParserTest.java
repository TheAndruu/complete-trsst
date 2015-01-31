package com.completetrsst.atom;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;
import static com.completetrsst.constants.Nodes.ATOM_ENTRY;
import static com.completetrsst.constants.Nodes.ATOM_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.constants.Namespaces;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class AtomParserTest {
    private static final KeyPair signingPair = new EllipticCurveKeyCreator().createKeyPair();
    private static final PublicKey encryptKey = new EllipticCurveKeyCreator().createKeyPair().getPublic();

    private static final String ENTRY_TITLE = "foo bar entry title";

    private static final AtomSigner signer = new AtomSigner();;
    private AtomParser parser;

    @Before
    public void init() {
        parser = new AtomParser();
    }

    @Test
    public void testRemoveEntryNodes() throws Exception {
        Element signedFeedAndEntries = new AtomSigner().createEntryAsDom(ENTRY_TITLE, "", signingPair, encryptKey);

        String rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);

        // The signed XML has our entry title in it
        assertTrue(rawXmlWithEntries.contains(ENTRY_TITLE));

        List<Node> removedEntries = parser.removeEntryNodes(signedFeedAndEntries);
        rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);
        // Signed feed no longer contains entry title
        assertFalse(rawXmlWithEntries.contains(ENTRY_TITLE));

        assertTrue(removedEntries.size() == 1);
        Element entry = (Element) removedEntries.get(0);
        Node titleNode = entry.getElementsByTagNameNS(Namespaces.ATOM_XMLNS, ATOM_TITLE).item(0);
        assertEquals(ENTRY_TITLE, titleNode.getTextContent());
    }

    @Test
    public void testAddEntryNodes() throws Exception {
        Element signedFeedAndEntries = signer.createEntryAsDom(ENTRY_TITLE, "", signingPair, encryptKey);
        org.jdom2.Element jdomFeed = XmlUtil.toJdom(signedFeedAndEntries);
        jdomFeed.addContent(createJdomEntry("first entry"));
        jdomFeed.addContent(createJdomEntry("second entry"));

        String rawXml = XmlUtil.serializeJdom(jdomFeed);
        assertTrue(rawXml.contains("first entry"));
        assertTrue(rawXml.contains("second entry"));
        assertTrue(rawXml.contains(ENTRY_TITLE));

        Element domFeed = XmlUtil.toDom(jdomFeed);
        List<Node> removedNodes = parser.removeEntryNodes(domFeed);

        rawXml = XmlUtil.serializeDom(domFeed);
        assertFalse(rawXml.contains("first entry"));
        assertFalse(rawXml.contains("second entry"));
        assertFalse(rawXml.contains(ENTRY_TITLE));

        // 3 nodes = 2 we added in this test and original with ENTRY_TITLE in it
        assertEquals(3, removedNodes.size());
        removedNodes.forEach(node -> assertTrue(node.getFirstChild().getTextContent().contains(ATOM_ENTRY)));
    }

    private org.jdom2.Element createJdomEntry(String text) {
        org.jdom2.Element entry = new org.jdom2.Element(ATOM_ENTRY, Namespaces.ATOM_XMLNS);
        entry.addContent(new org.jdom2.Text(text));
        return entry;
    }

    @Test
    public void testGetFirstNode() throws Exception {
        Element signedFeedAndEntries = signer.createEntryAsDom(ENTRY_TITLE, "", signingPair, encryptKey);
        // Encrypt node shouldn't exist, should get null
        Node fetchedNode = parser.getFirstNode(signedFeedAndEntries, TRSST_XMLNS, "nonexistantnode");
        assertNull(fetchedNode);

        // get the only one if there's only one
        fetchedNode = parser.getFirstNode(signedFeedAndEntries, ATOM_XMLNS, ATOM_TITLE);
        assertNotNull(fetchedNode);
        assertEquals(ENTRY_TITLE, fetchedNode.getTextContent());

        // add a couple child nodes
        org.jdom2.Element jdomFeed = XmlUtil.toJdom(signedFeedAndEntries);
        org.jdom2.Element entry = new org.jdom2.Element("newnode", Namespaces.ATOM_XMLNS);
        entry.addContent(new org.jdom2.Text("first new node"));
        jdomFeed.addContent(entry);
        entry = new org.jdom2.Element("newnode", Namespaces.ATOM_XMLNS);
        entry.addContent(new org.jdom2.Text("second new node"));
        jdomFeed.addContent(entry);

        signedFeedAndEntries = XmlUtil.toDom(jdomFeed);

        // if more than one, should get the first
        fetchedNode = parser.getFirstNode(signedFeedAndEntries, ATOM_XMLNS, "newnode");
        assertNotNull(fetchedNode);
        assertEquals("first new node", fetchedNode.getTextContent());
    }

    @Test
    public void testGetEntrySignatureValueFailsNoSignature() throws Exception {
        Element feedAndDom = TestUtil.readDomFromFile(TestUtil.PLAIN_ATOM_ENTRY);
        try {
            parser.getEntrySignatureValue(feedAndDom);
            fail("Should have thrown illegal argument exception b/c we have no signature on this input");
        } catch (IllegalArgumentException e) {
            // expect to get here
        }
    }

    @Test
    public void testGetEntrySignatureValueGetsEntrySigValueUnderFeed() throws Exception {
        Element feedAndDom = TestUtil.readDomFromFile(TestUtil.SIGNED_ATOM_ENTRY);
        String sigValue = parser.getEntrySignatureValue(feedAndDom);
        assertEquals("DgZ93jR2RlIOUBTf42GXDEp8pv/6m70SfrGmfZU4VUGpIiGpjvRo+2QJ0RjwUZtwpcwofbWZbopj\n"+"PHnwlH6GhA==", sigValue);
    }

    @Test
    public void testGetEntrySignatureValueGetsStandaloneEntrySigValue() throws Exception {
        Element feedAndDom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_VALID);
        String sigValue = parser.getEntrySignatureValue(feedAndDom);
        assertEquals("q3Bt85qgLYgc7GnbLqBHAjDjxZlarUbtpkvnWr8CzQe3eBBUwm8ykCbukjrCJj9UjBlfRvbwCTAU\n"+"QJ3XOVFQIA==", sigValue);
    }
}
