package com.completetrsst.rome;

import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.TestUtil;
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
		Entry entry = EntryCreator.create(ENTRY_TITLE);
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

		System.out.println("Signed feed exclusive: " + XmlUtil.serializeDom(signedFeed));
		// Fail -- we want entries removed prior to signature addition
		assertFalse(SignatureUtil.verifySignature(signedFeed));

		List<Node> removedEntries = FeedCreator.removeEntryNodes(signedFeed);
		assertTrue(removedEntries.size() > 0);

		// Should pass now
		assertTrue(SignatureUtil.verifySignature(signedFeed));
	}

	@Test
	public void entriesAlsoSignedOnSignedFeed() throws Exception {
		Entry entry1 = EntryCreator.create("first entry");
		Entry entry2 = EntryCreator.create("second entry");
		feed.setEntries(Arrays.asList(entry1, entry2));

		Element signedFeed = FeedCreator.signFeed(feed, keyPair);

		assertTrue(FeedCreator.isVerified(signedFeed));

		// Pop off each entry and try validating them individually
		// removeEntryNodes tested below so we know it works correctly
		List<Node> entries = FeedCreator.removeEntryNodes(signedFeed);

		assertTrue(SignatureUtil.verifySignature(signedFeed));
		assertEquals(2, entries.size());
		// Again eclipse preventing lambda bc of exception bug in eclipse
		for (Node node : entries) {
			assertTrue(SignatureUtil.verifySignature((Element) node));
		}
	}

	@Test
	public void verificationFailsIfEntryNotSigned() throws Exception {
		feed.setEntries(Collections.emptyList());
		Element signedFeed = FeedCreator.signFeed(feed, keyPair);

		Entry unsignedEntry = EntryCreator.create("unsigned entry title");
		// Use jdom for ease of attaching
		org.jdom2.Element jdomUnsignedEntry = RomeUtil.toJdom(unsignedEntry);
		org.jdom2.Element jdomSignedFeed = XmlUtil.toJdom(signedFeed);
		jdomUnsignedEntry.detach();
		jdomSignedFeed.addContent(jdomUnsignedEntry);

		Element signedFeedWithUnsignedEntry = XmlUtil.toDom(jdomSignedFeed);
		// Fail -- feed is signed but attached entry isn't
		System.out.println("Foobarfoo");
		System.out.println(XmlUtil.serializeDom(signedFeedWithUnsignedEntry));
		try {
			FeedCreator.isVerified(signedFeedWithUnsignedEntry);
			fail("An XMLSignatureException should be thrown bc attached Entry has no signature");
		} catch (XMLSignatureException e) {
			// expected
		}
	}

	@Test
	public void verificationFailsIfEntrySignedButInvalidAndFeedValidSignature() throws Exception {
		Element element = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
		Element entryElement = (Element)element.getElementsByTagNameNS(SignedEntry.XMLNS, "entry").item(0);
		assertFalse(SignatureUtil.verifySignature(entryElement));
		// Have to remove the entry from the parent Feed node to test the feed's signature
		Node feedNode = entryElement.getParentNode();
		feedNode.removeChild(entryElement);
		assertTrue(SignatureUtil.verifySignature((Element)feedNode));
	}

	/** 
	 * TODO: This is the way we need to sign feeds and entries - separately prior to associating
	 * them with each other, so that they may be individually verified */
	@Test
	public void validFeedAndEntryBothValidate() throws Exception {
	    
	    Entry entry = EntryCreator.create("my new entry");
	    Element signedEntry = EntryCreator.toDom(entry);
	    SignatureUtil.signElement(signedEntry, keyPair);
	    
	    // have signed entry, ready to be placed on signed feed
	    
	    assertTrue(SignatureUtil.verifySignature(signedEntry));
	    
	    Feed feed = FeedCreator.createFor(keyPair);
	    Element signedFeed = FeedCreator.signFeed(feed, keyPair);
	    assertTrue(SignatureUtil.verifySignature(signedFeed));
	    
	    
	    assertTrue(SignatureUtil.verifySignature(signedEntry));
	    signedEntry.getParentNode().removeChild(signedEntry);
	    signedFeed.getOwnerDocument().adoptNode(signedEntry);
	    signedFeed.appendChild(signedEntry);
	    
	    signedFeed.removeChild(signedEntry);
	    DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    Document doc = dbf.newDocumentBuilder().newDocument();
	    doc.adoptNode(signedEntry);
	    assertTrue(SignatureUtil.verifySignature(signedEntry));
	    
	    assertTrue(SignatureUtil.verifySignature(signedFeed));
	}

	
	@Test
	public void verificationOfEntryPassesThenFeedAlteredAndFeedFailsVerification() throws Exception {
		Element signedFeed = FeedCreator.signFeed(feed, keyPair);

		// assert that the entry validates
		Element entryElement = (Element) FeedCreator.removeEntryNodes(signedFeed).get(0);
		assertTrue(SignatureUtil.verifySignature(entryElement));

		org.jdom2.Element jdomSignedFeed = XmlUtil.toJdom(signedFeed);
		org.jdom2.Element feedId = jdomSignedFeed.getChild("id", Namespace.getNamespace(SignedEntry.XMLNS));
		feedId.setText("new id");
		signedFeed = XmlUtil.toDom(jdomSignedFeed);

		// false b/c feed is invalid now
		assertFalse(FeedCreator.isVerified(signedFeed));

		// Note: this test is a little interesting.
		// Entries validate prior to feed alteration, but after feed alteration,
		// entries no longer validate.
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
		entry.addContent(new org.jdom2.Text(text));
		return entry;
	}
}
