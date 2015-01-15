package com.completetrsst.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.atom.AtomParser;
import com.completetrsst.atom.AtomVerifier;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

	private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

	private static final AtomParser parser = new AtomParser();
	private Map<String, List<SignedEntry>> publishersToStories = new HashMap<String, List<SignedEntry>>();

	@Override
	public String readFeed(String publisherId) {
		List<SignedEntry> entries = publishersToStories.get(publisherId);
		if (entries == null) {
			return "No entries to view on feed " + publisherId;
		}
		StringBuilder builder = new StringBuilder();

		builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		builder.append("<feed xmlns=\"http://www.w3.org/2005/Atom\">");
		builder.append("\"<title>Example Feed</title>");
		builder.append("<updated>2014-12-13T18:30:02Z</updated>");
		builder.append("<author>");
		builder.append("<name>John Deere</name>");
		builder.append("</author>");
		builder.append("<id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>");

		// First Java 8 Lambda!
		entries.forEach(entry -> builder.append(entry.getRawXml()));

		builder.append("</feed>");
		return builder.toString();
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

		// Verify the feed and entries
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
		
		// TODO: Create new map of Feed : entries if feed doesn't exist, else use old
		// TODO: Store feed xml and entry xml under Feed Id
		// -- new class?  SignedContent (string latestFeed, List<String> entries)

		return "Stored verified signed entry onto its associated feed";
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

//	// TODO: Move these to a helper class
//	private SignedEntry createSignedEntry(Element domElement, String signedXml) throws IllegalArgumentException {
//		String title = getTitle(domElement);
//		String id = getId(domElement);
//		String dateUpdated = getDateUpdated(domElement);
//		SignedEntry entry = new SignedEntry();
//		entry.setTitle(title);
//		entry.setId(id);
//		entry.setDateUpdated(dateUpdated);
//		entry.setRawXml(signedXml);
//		return entry;
//	}
//
//	private void addEntry(String publisherId, SignedEntry story) {
//		List<SignedEntry> existingStories = publishersToStories.get(publisherId);
//		if (existingStories == null) {
//			existingStories = new ArrayList<SignedEntry>();
//		}
//		existingStories.add(story);
//		Collections.sort(existingStories);
//
//		publishersToStories.put(publisherId, existingStories);
//	}

}
