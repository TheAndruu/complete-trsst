package com.completetrsst.rome;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.impl.Atom10Generator;

public class FeedCreator {

	private static final Logger log = LoggerFactory.getLogger(FeedCreator.class);

	/**
	 * Creates a feed for the given PublicKey, which determines the feed's ID.
	 * Also has updated date set
	 */
	public static Feed createFor(KeyPair keyPair) {
		// Add title?
		Feed feed = new Feed("atom_1.0");
		feed.setUpdated(new Date());
		PublicKey publicKey = keyPair.getPublic();
		String id = TrsstKeyFunctions.toFeedUrn(TrsstKeyFunctions.toFeedId(publicKey));
		feed.setId(id);
		return feed;
	}

	/**
	 * Takes a Rome feed to sign, removing all entries prior to signing, so that
	 * feeds are signed containing only feed-specific elements (no
	 * entry/content, etc) Re-attaches entries prior to returning signed feed
	 * element.
	 */
	public static Element signFeed(Feed feed, KeyPair keyPair) throws XMLSignatureException {

		org.jdom2.Element jdomFeed = toJdom(feed);

		// Remove all Entry nodes prior to signing, as required
		Element domFeed;
		try {
			domFeed = XmlUtil.toDom(jdomFeed);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new XMLSignatureException("Couldn't convert DOM back to Jdom when signing feed", e);
		}
		List<Node> removedNodes = removeEntryNodes(domFeed);

		// Actually sign the feed
		try {
			SignatureUtil.signElement(domFeed, keyPair);
		} catch (XMLSignatureException e) {
			log.error(e.getMessage());
			throw new XMLSignatureException("Trouble when actually signing the feed", e);
		}

		// Add the removed entity nodes back
		addEntries(domFeed, removedNodes);

		// Return the signed feed, with entries added back in
		return domFeed;
	}

	static org.jdom2.Element toJdom(Feed feed) throws XMLSignatureException {
		Atom10Generator generator = new Atom10Generator();
		org.jdom2.Element jdomFeed;
		try {
			org.jdom2.Document doc = generator.generate(feed);
			jdomFeed = doc.getRootElement();
		} catch (FeedException e) {
			log.error(e.getMessage());
			throw new XMLSignatureException("Couldn't get feed element as Jdom", e);
		}
		return jdomFeed;
	}

	/**
	 * Verifies the given feed and returns true if the signature validates. Feed
	 * validation involves removing all Atom Entry nodes to do validation, so
	 * this method helps by handling that. Entries added back prior to
	 * returning.
	 * 
	 * @throws XmlSignatureException
	 *             for errors parsing signature or if signature not found
	 * 
	 * @param feed
	 *            The feed to validate, represented as a DOM Element
	 * 
	 * @return true if the signature validates the feed, false otherwise
	 * @throws XmlSignatureException
	 *             if there's an error encountered parsing or verifying the feed
	 */
	public static boolean isVerified(Element domFeed) throws XMLSignatureException {
		List<Node> removedEntries = removeEntryNodes(domFeed);
		boolean isVerified = SignatureUtil.verifySignature(domFeed);
		addEntries(domFeed, removedEntries);

		return isVerified;
	}

	/** Adds the given entities to the feed */
	static void addEntries(Element domFeed, List<Node> entityNodesToAdd) {
		entityNodesToAdd.forEach(entryNode -> domFeed.appendChild(entryNode));
	}

	/**
	 * Removes all Atom Entry nodes from the given DOM feed element and returns
	 * them as a new list. DOM Feed element is updated in place.
	 */
	static List<Node> removeEntryNodes(Element domFeed) {
		NodeList nodeList = domFeed.getElementsByTagNameNS(SignedEntry.XMLNS, "entry");
		// we know there's only one entry on this feed
		List<Node> removedNodes = new ArrayList<Node>();
		int numEntryNodes = nodeList.getLength();
		for (int i = 0; i < numEntryNodes; i++) {
			Node node = nodeList.item(i);
			removedNodes.add(node);
		}
		// Need to do the actual removal in a 2nd loop, else as we remove by
		// index,
		// above, the removals will shift down the location of the next node
		// thereby not actually removing all the nodes we want
		removedNodes.forEach(node -> domFeed.removeChild(node));
		return removedNodes;
	}
}
