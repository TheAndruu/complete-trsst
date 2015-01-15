package com.completetrsst.model;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

/**
 * Publishes a signed Atom entry on an Atom feed. Entries are signed as
 * 'excluded' meaning they can be verified independently of their owner Feed
 * elements. Feed elements are signed exclusive of any attached Entries. Thus
 * verification of Feed signatures implies all Entries have been removed from
 * owner document.
 */
public class SignedEntryPublisher {
    private static final Logger log = LoggerFactory.getLogger(SignedEntryPublisher.class);
    public static final String XMLNS = "http://www.w3.org/2005/Atom";
    public static final String ENTRY_ID_PREFIX = "urn:uuid:";

    /**
     * Creates a new signed Atom entry with given title, wrapped inside an
     * individually-sigend Atom feed element.
     * 
     * @param entryTitle
     *            Title for the entry
     * @return DOM element containing a signed Feed node and
     *         independently-signed Entry node
     */
    public String publish(String entryTitle, KeyPair keyPair) throws IOException, XMLSignatureException {
        return XmlUtil.serializeDom(publishNew(entryTitle, keyPair));
    }

    /**
     * Creates a new signed Atom entry with given title, wrapped inside an
     * individually-sigend Atom feed element.
     * 
     * @param entryTitle
     *            Title for the entry
     * @return DOM element containing a signed Feed node and
     *         independently-signed Entry node
     */
    protected Element publishNew(String entryTitle, KeyPair keyPair) throws IOException, XMLSignatureException {
        // Construct the feed and entry
        Element domEntry = toDom(createEntry(entryTitle));
        Element domFeed = toDom(createFeed(keyPair.getPublic()));

        // Sign each separately
        SignatureUtil.signElement(domEntry, keyPair);
        SignatureUtil.signElement(domFeed, keyPair);

        // Add the entry to the feed
        domFeed.getOwnerDocument().adoptNode(domEntry);
        domFeed.appendChild(domEntry);

        return domFeed;
    }

    /**
     * Creates a new unsigned feed with updated date and ID matching public
     * key's date.
     * 
     * @return unsigned feed with updated date
     */
    protected Feed createFeed(PublicKey publicKey) {
        // Add title?
        Feed feed = new Feed("atom_1.0");
        feed.setUpdated(new Date());
        feed.setId(getFeedId(publicKey));
        return feed;
    }

    //
    // private static signFeed(Element domFeed) {
    // List<Node> removedNodes = removeEntryNodes(domFeed);
    //
    // // Actually sign the feed, including individual entry nodes separately
    // try {
    // SignatureUtil.signElement(domFeed, keyPair);
    // // eclipse bug prevents a closure from being applied below
    // for (Node entry : removedNodes) {
    // SignatureUtil.signElement((Element) entry, keyPair);
    // }
    // } catch (XMLSignatureException e) {
    // log.error(e.getMessage());
    // throw new XMLSignatureException("Trouble when actually signing the feed",
    // e);
    // }
    //
    // // Add the removed, now-signed entity nodes back
    // addEntries(domFeed, removedNodes);
    //
    // // Return the signed feed, with signed entries added back in
    // return domFeed;
    // }

    /** Returns unsigned entry not attached to any feed */
    protected Entry createEntry(String title) {
        Entry entry = new Entry();
        entry.setTitle(title);
        entry.setUpdated(new Date());
        entry.setId(newEntryId());
        return entry;
    }

    /** New random, globally unique id for an entry */
    protected String newEntryId() {
        return ENTRY_ID_PREFIX + UUID.randomUUID().toString();
    }

    /** Returns Feed ID to go with the associated public key from the KeyPair */
    protected String getFeedId(PublicKey publicKey) {
        return TrsstKeyFunctions.toFeedUrn(TrsstKeyFunctions.toFeedId(publicKey));
    }

    /** Converts Feed to DOM element */
    Element toDom(Feed feed) throws IOException {
        Element domFeed;
        try {
            domFeed = XmlUtil.toDom(toJdom(feed));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        return domFeed;
    }

    /** Converts Feed to JDOM element. Necessary step to get to DOM */
    org.jdom2.Element toJdom(Feed feed) throws IOException {
        Atom10Generator generator = new Atom10Generator();
        org.jdom2.Element jdomFeed;
        try {
            org.jdom2.Document doc = generator.generate(feed);
            jdomFeed = doc.getRootElement();
        } catch (FeedException e) {
            log.error(e.getMessage(), e);
            throw new IOException("Couldn't get feed as DOM element", e);
        }
        return jdomFeed;
    }

    /** Converts an Entry to a DOM element */
    Element toDom(Entry entry) throws IOException {
        return XmlUtil.toDom(toJdom(entry));
    }

    /** Converts an Entry to a JDOM element. Necessary step to DOM. */
    org.jdom2.Element toJdom(Entry entry) {
        // Build a feed containing only the entry
        final List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        final Feed feed1 = new Feed();
        feed1.setFeedType("atom_1.0");
        feed1.setEntries(entries);

        // Get Rome to output feed as a JDOM document
        final WireFeedOutput wireFeedOutput = new WireFeedOutput();
        org.jdom2.Document feedDoc;
        try {
            feedDoc = wireFeedOutput.outputJDom(feed1);
        } catch (IllegalArgumentException | FeedException e) {
            log.debug("Error writing entry to jdom");
            throw new IllegalArgumentException(e);
        }

        // Grab entry element from feed and get JDOM to serialize it
        return feedDoc.getRootElement().getChildren().get(0);
    }
}
