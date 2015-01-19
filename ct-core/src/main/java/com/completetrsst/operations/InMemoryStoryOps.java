package com.completetrsst.operations;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.atom.AtomParser;
import com.completetrsst.atom.AtomVerifier;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.store.Storage;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

	private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

	private static final AtomParser parser = new AtomParser();

	// For database access
	private Storage storage;

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	@Override
	public String readFeed(String publisherId) {
		String rawFeed = storage.getFeed(publisherId);
		if (rawFeed.equals("")) {
			log.debug("No entries to view on feed: " + publisherId);
			return "No entries to view on feed " + publisherId;
		}

		List<String> rawEntries = storage.getLatestEntries(publisherId);
		if (rawEntries.size() == 0) {
			log.debug("No entries to view on feed: " + publisherId);
			return rawFeed;
		}

		Element feedDom;
		try {
			feedDom = XmlUtil.toDom(rawFeed);

			for (String entry : rawEntries) {
				Element domEntry = XmlUtil.toDom(entry);
				feedDom.getOwnerDocument().adoptNode(domEntry);
				feedDom.appendChild(domEntry);
			}

			log.debug("Successfully got serialized feed");
			return XmlUtil.serializeDom(feedDom);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	/** Takes in raw signed XML and creates new or adds to existing Atom feed */
	@Override
	public String publishSignedContent(String signedXml) throws XMLSignatureException, IllegalArgumentException {
		log.info("Received signed XML to publish!");

		Element feed;
		try {
			feed = XmlUtil.toDom(signedXml);
		} catch (IOException e) {
			log.debug(e.getMessage());
			throw new IllegalArgumentException(e);
		}

		// Verify the feed and entries. Throws exception if not verified
		verifySignedContent(feed);

		// Detach the entries themselves
		List<Node> detachedEntries = parser.removeEntryNodes(feed);

		// 'feed' is now just a 'feed' node with no entries attached
		String feedId = TrsstKeyFunctions.removeFeedUrnPrefix(parser.getId(feed));

		// Verify the feed matches the signature of each node's public key
		boolean entriesMatchFeed = parser.doEntriesMatchFeedId(feedId, detachedEntries);
		if (!entriesMatchFeed) {
			throw new IllegalArgumentException("Entries must be signed with same public key as this feed");
		}

		// So now we know the entries belong on this feed, we have the feed by
		// itself, and we have the entries by themselves

		// TODO: Parse this date from the input, don't set it with system date!
		storage.storeFeed(feedId, new Date(), XmlUtil.serializeDom(feed));
		
		for (Node node : detachedEntries) {
			// TODO: Remove urn prefix on entry ids!
			String entryId = parser.getId((Element)node);
			// TODO: parse the date, don't set it to system date!
			storage.storeEntry(feedId, entryId, new Date(), XmlUtil.serializeDom((Element) node));
		}

		return "Stored onto feed " + feedId;
	}

	private void verifySignedContent(Element domElement) throws XMLSignatureException {
		AtomVerifier verifier = new AtomVerifier();
		boolean isFeedValid = verifier.isFeedVerified(domElement);
		if (!isFeedValid) {
			log.debug("Feed didn't validate with its signature");
			throw new IllegalArgumentException("Feed didn't validate with its signature");
		}
		boolean isEntryValid = verifier.areEntriesVerified(domElement);
		if (!isEntryValid) {
			log.debug("Entry(s) didn't validate with their signature.");
			throw new IllegalArgumentException("Entry(s) didn't validate with their signature.");
		}
	}
}
