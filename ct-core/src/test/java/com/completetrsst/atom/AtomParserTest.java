package com.completetrsst.atom;

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
import com.completetrsst.xml.XmlUtil;

public class AtomParserTest {
	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
	private static final String ENTRY_TITLE = "foo bar entry title";

	private AtomParser parser;

	@Before
	public void init() {
		parser = new AtomParser();
	}

	@Test
	public void testRemoveEntryNodes() throws Exception {
		Element signedFeedAndEntries = new AtomSigner().createNewSignedEntry(ENTRY_TITLE, keyPair);

		String rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);

		// The signed XML has our entry title in it
		assertTrue(rawXmlWithEntries.contains(ENTRY_TITLE));

		List<Node> removedEntries = parser.removeEntryNodes(signedFeedAndEntries);
		rawXmlWithEntries = XmlUtil.serializeDom(signedFeedAndEntries);
		// Signed feed no longer contains entry title
		assertFalse(rawXmlWithEntries.contains(ENTRY_TITLE));

		assertTrue(removedEntries.size() == 1);
		Element entry = (Element) removedEntries.get(0);
		Node titleNode = entry.getElementsByTagNameNS(AtomSigner.XMLNS, "title").item(0);
		assertEquals(ENTRY_TITLE, titleNode.getTextContent());
	}

	@Test
	public void testAddEntryNodes() throws Exception {
		Element signedFeedAndEntries = new AtomSigner().createNewSignedEntry(ENTRY_TITLE, keyPair);
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
		removedNodes.forEach(node -> assertTrue(node.getFirstChild().getTextContent().contains("entry")));
	}

	private org.jdom2.Element createJdomEntry(String text) {
		org.jdom2.Element entry = new org.jdom2.Element("entry", AtomSigner.XMLNS);
		entry.addContent(new org.jdom2.Text(text));
		return entry;
	}

}