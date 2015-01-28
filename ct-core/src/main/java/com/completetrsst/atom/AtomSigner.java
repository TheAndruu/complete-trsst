package com.completetrsst.atom;

import static com.completetrsst.constants.Nodes.ENTRY_ID_PREFIX;
import static com.completetrsst.constants.Nodes.TRSST_ENCRYPT;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;
import static com.completetrsst.constants.Nodes.TRSST_SIGN;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.completetrsst.constants.Namespaces;
import com.completetrsst.crypto.Common;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

/**
 * Publishes a signed Atom entry on an Atom feed. Entries are signed as 'excluded' meaning they can be verified independently of their owner Feed
 * elements. Feed elements are signed exclusive of any attached Entries. Thus verification of Feed signatures implies all Entries have been removed
 * from owner document.
 */
public class AtomSigner {
    private static final Logger log = LoggerFactory.getLogger(AtomSigner.class);

    /**
     * Creates a new signed Atom entry with given title, wrapped inside an individually-signed Atom feed element.
     * 
     * @param entryTitle
     *            Title for the entry
     * @return DOM element containing a signed Feed node and independently-signed Entry node
     */
    public String createEntry(String entryTitle, String prevEntrySigValue, KeyPair signingKeyPair, PublicKey encryptingPublicKey) throws IOException,
            XMLSignatureException {
        return XmlUtil.serializeDom(createEntryAsDom(entryTitle, prevEntrySigValue, signingKeyPair, encryptingPublicKey));
    }

    /**
     * Creates a new signed Atom entry inside a feed with given title, wrapped inside an individually-signed Atom feed element.
     * 
     * @param entryTitle
     *            Title for the entry
     * @return DOM element containing a signed Feed node and independently-signed Entry node
     */
    public Element createEntryAsDom(String entryTitle, String prevEntrySigValue, KeyPair signingKeyPair, PublicKey encryptKey) throws IOException,
            XMLSignatureException {
        // Construct the feed and entry
        Element domEntry = toDom(createEntry(entryTitle, prevEntrySigValue));
        Element domFeed = toDom(createFeed(signingKeyPair.getPublic(), encryptKey));

        signAndBuildFeed(signingKeyPair, domEntry, domFeed);
        return domFeed;
    }

    /** Signs the entry and feed elements separately, then attaches the entry element to the feed */
    void signAndBuildFeed(KeyPair signingPair, Element domEntry, Element domFeed) throws XMLSignatureException {
        // An important step, as it forces XMLNS declarations as they would appear to a recipient
        // after serialization-- MUST be done prior to digitally signing
        domEntry.getOwnerDocument().normalizeDocument();
        domFeed.getOwnerDocument().normalizeDocument();

        // Sign each separately
        SignatureUtil.signElement(domEntry, signingPair);
        SignatureUtil.signElement(domFeed, signingPair);

        // Add the entry to the feed
        domFeed.getOwnerDocument().adoptNode(domEntry);
        domFeed.appendChild(domEntry);
    }

    /**
     * Creates a new unsigned feed with updated date and ID matching public key's date.
     * 
     * @param encryptingKeyPair
     * 
     * @return unsigned feed with updated date
     */
    protected Feed createFeed(PublicKey signingKeyPair, PublicKey encryptingKeyPair) {
        // Add title?
        Feed feed = new Feed("atom_1.0");
        feed.setUpdated(new Date());
        feed.setId(getFeedId(signingKeyPair));
        // Set the Trsst-specific content here
        feed.setForeignMarkup(createTrsstFeedMarkup(signingKeyPair, encryptingKeyPair));
        return feed;
    }

    private List<org.jdom2.Element> createTrsstFeedMarkup(PublicKey signingKey, PublicKey encryptingKey) {
        org.jdom2.Element signElement = new org.jdom2.Element(TRSST_SIGN, Namespaces.TRSST_NAMESPACE);
        try {
            String publicAsX509 = Common.toX509FromPublicKey(signingKey);
            signElement.setText(publicAsX509);
        } catch (GeneralSecurityException e) {
            log.error("Error converting PublicKey to x509");
            throw new RuntimeException("Error converting signing PublicKey to x509");
        }
        org.jdom2.Element encryptElement = new org.jdom2.Element(TRSST_ENCRYPT, Namespaces.TRSST_NAMESPACE);
        try {
            String publicAsX509 = Common.toX509FromPublicKey(encryptingKey);
            encryptElement.setText(publicAsX509);
        } catch (GeneralSecurityException e) {
            log.error("Error converting PublicKey to x509");
            throw new RuntimeException("Error converting encrypting PublicKey to x509");
        }

        return Arrays.asList(signElement, encryptElement);
    }

    private List<org.jdom2.Element> createTrsstEntryMarkup(String previousEntrySignatureValue) {
        org.jdom2.Element signElement = new org.jdom2.Element(TRSST_PREDECESSOR, Namespaces.TRSST_NAMESPACE);
        signElement.setText(previousEntrySignatureValue);

        return Collections.singletonList(signElement);
    }

    /** Returns unsigned entry not attached to any feed */
    protected Entry createEntry(String title, String prevEntrySignValue) {
        Entry entry = new Entry();
        entry.setTitle(title);
        entry.setUpdated(new Date());
        entry.setId(newEntryId());
        List<org.jdom2.Element> markup = createTrsstEntryMarkup(prevEntrySignValue);
        entry.setForeignMarkup(markup);
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
