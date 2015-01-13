package com.completetrsst.rome;

import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Text;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.impl.Atom10Generator;

public class FeedCreatorTest {
	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

	private static Feed feed;

	private static final String ENTRY_TITLE = "foo bar entry title";

	@Before
	public void init() {
		feed = FeedCreator.createFor(keyPair);
		Entry entry = new Entry();
		entry.setTitle(ENTRY_TITLE);
		feed.setEntries(Arrays.asList(entry));
	}

	@Test
	public void createFeedRequiredFields() {
		String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
		assertEquals(expectedId, feed.getId());
		assertEquals("atom_1.0", feed.getFeedType());
		assertTrue(feed.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
	}

	@Test
	public void feedSerializationContainsExpected() throws Exception {
		Atom10Generator generator = new Atom10Generator();
		org.jdom2.Document doc = generator.generate(feed);
		String rawXml = XmlUtil.serializeJdom(doc.getRootElement());

		String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
		assertTrue(rawXml.contains(expectedId));
		// Entry we added to it
		assertTrue(rawXml.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\">"));
		assertTrue(rawXml.contains(ENTRY_TITLE));
	}

	@Test
	public void signedFeedValidates() throws Exception {
		Element signedFeed = FeedCreator.signFeed(feed, keyPair);

		// Fail -- we want entries removed prior to signature addition
		assertFalse(SignatureUtil.verifySignature(signedFeed));

		List<Node> removedEntries = FeedCreator.removeEntryNodes(signedFeed);
		assertTrue(removedEntries.size() > 0);

		// Should pass now
		assertTrue(SignatureUtil.verifySignature(signedFeed));
	}

	@Test
	public void testRemoveEntryNodes() throws Exception {
		Element signedFeed = FeedCreator.signFeed(feed, keyPair);
		String rawXmlWithEntries = XmlUtil.serializeDom(signedFeed);

		// The signed XML has our entry title in it
		assertTrue(rawXmlWithEntries.contains(ENTRY_TITLE));

		List<Node> removedEntries = FeedCreator.removeEntryNodes(signedFeed);
		rawXmlWithEntries = XmlUtil.serializeDom(signedFeed);
		// Signed feed no longer contains entry title
		assertFalse(rawXmlWithEntries.contains(ENTRY_TITLE));

		assertTrue(removedEntries.size() == 1);
		Element entry = (Element) removedEntries.get(0);
		Node titleNode = entry.getElementsByTagNameNS(SignedEntry.XMLNS, "title").item(0);
		assertEquals(ENTRY_TITLE, titleNode.getTextContent());
	}

	@Test
	public void testAddEntryNodes() throws Exception {
		org.jdom2.Element jdomFeed = FeedCreator.toJdom(feed);
		jdomFeed.addContent(createJdomEntry("first entry"));
		jdomFeed.addContent(createJdomEntry("second entry"));

		String rawXml = XmlUtil.serializeJdom(jdomFeed);
		assertTrue(rawXml.contains("first entry"));
		assertTrue(rawXml.contains("second entry"));
		assertTrue(rawXml.contains(ENTRY_TITLE));
		
		Element domFeed = XmlUtil.toDom(jdomFeed);
		List<Node> removedNodes = FeedCreator.removeEntryNodes(domFeed);

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
		entry.addContent(new Text(text));
		return entry;
	}
}
