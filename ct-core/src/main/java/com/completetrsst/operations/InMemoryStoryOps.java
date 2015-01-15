package com.completetrsst.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

    private static final AtomParser parser = new AtomParser();
    private Map<String, FeedHolder> idsToFeeds = new HashMap<String, FeedHolder>();

    @Override
    public String readFeed(String publisherId) {
        FeedHolder holder = idsToFeeds.get(publisherId);
        if (holder == null) {
            log.debug("No entries to view on feed: " + publisherId);
            return "No entries to view on feed " + publisherId;
        }

        String rawFeed = holder.feedXml;
        List<String> rawEntries = holder.entryXml;

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

        FeedHolder holder = getFeed(feedId);
        holder.feedXml = XmlUtil.serializeDom(feed);
        for (Node node : detachedEntries) {
            holder.entryXml.push(XmlUtil.serializeDom((Element) node));
        }

        idsToFeeds.put(feedId, holder);
        return "Stored onto feed " + feedId;
    }

    private FeedHolder getFeed(String feedId) {
        FeedHolder holder = idsToFeeds.get(feedId);
        if (holder == null) {
            holder = new FeedHolder();
            idsToFeeds.put(feedId, holder);
        }
        return holder;
    }

    private class FeedHolder {
        String feedXml = "";
        LinkedList<String> entryXml = new LinkedList<String>();
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

    // // TODO: Move these to a helper class
    // private SignedEntry createSignedEntry(Element domElement, String
    // signedXml) throws IllegalArgumentException {
    // String title = getTitle(domElement);
    // String id = getId(domElement);
    // String dateUpdated = getDateUpdated(domElement);
    // SignedEntry entry = new SignedEntry();
    // entry.setTitle(title);
    // entry.setId(id);
    // entry.setDateUpdated(dateUpdated);
    // entry.setRawXml(signedXml);
    // return entry;
    // }
    //
    // private void addEntry(String publisherId, SignedEntry story) {
    // List<SignedEntry> existingStories = publishersToStories.get(publisherId);
    // if (existingStories == null) {
    // existingStories = new ArrayList<SignedEntry>();
    // }
    // existingStories.add(story);
    // Collections.sort(existingStories);
    //
    // publishersToStories.put(publisherId, existingStories);
    // }

}
