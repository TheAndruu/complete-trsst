package com.completetrsst.rome;

import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.rome.modules.TrsstModule;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

public class FeedCreatorTest {
	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

	private static Feed feed;

	@Before
	public void init() {
		feed = FeedCreator.createFor(keyPair);
		Entry entry = new Entry();
		entry.setTitle("foo bar");
		feed.setEntries(Arrays.asList(entry));
	}

	@Test
	public void testCreateFeed() {
		String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
		assertEquals(expectedId, feed.getId());
		assertEquals("atom_1.0", feed.getFeedType());

		List<Module> modules = feed.getModules();
		assertTrue(modules.size() == 1);
		TrsstModule module = (TrsstModule) modules.get(0);
		assertTrue(module.getIsSigned());
		assertEquals(keyPair, module.getKeyPair());
		assertTrue(feed.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
	}

	@Test
	public void feedWritingAndSigning() throws Exception {
		String rawXml = serializeToString();
		assertTrue(rawXml.contains("SignatureValue"));
		assertTrue(rawXml.contains("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"));
		String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
		assertTrue(rawXml.contains(expectedId));
		assertTrue(rawXml.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\">"));
		assertTrue(rawXml.contains("foo bar"));
	}

	// TODO: change the canonilization method used in creating signatures?
	// exclusive means nodes are detachable and signable
	@Ignore
	@Test
	public void signedFeedValidates() throws Exception {
		Element signedFeed = serializeFeed();
		
		// Fails because entry attached - we want feeds signed without entries
		assertFalse(SignatureUtil.verifySignature(signedFeed));

		NodeList entries = signedFeed.getElementsByTagNameNS(SignedEntry.XMLNS, "entry");
		// we know there's only one entry on this feed
		Node node = entries.item(0);
		assertNotNull(node);
		signedFeed.removeChild(node);
		
//		System.out.println("Here goes:");
//		System.out.println(rawXml);
//		System.out.println(XmlUtil.serializeDom(signedFeed));
		
		// Should pass now
		assertTrue(SignatureUtil.verifySignature(signedFeed));
	}

	private static String serializeToString() throws Exception {
		WireFeedOutput outputter = new WireFeedOutput();
		StringWriter writer = new StringWriter();
		outputter.output(feed, writer);
		writer.close();
		return writer.toString();
	}
	
	private Element serializeFeed() throws IOException, FeedException {
		WireFeedOutput outputter = new WireFeedOutput();
//		StringWriter writer = new StringWriter();
		Document doc = outputter.outputW3CDom(feed);
		return doc.getDocumentElement();
//		outputter.output(feed, writer);
//		writer.close();
//		return writer.toString();

	}
}
