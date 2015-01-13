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
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.impl.Atom10Generator;

public class EntryCreator {

    // make this a 'has a ' relationship with Entrys
    // and oo (non static) fields)
    // such that it has methods like sign() and .verify()
    // include rawXml field to back this obj?
    
    
    private static final Logger log = LoggerFactory.getLogger(EntryCreator.class);

    /**
     * Creates an entry with the given text, simple use case for now.
     */
    public static Entry create() {
        // Add title?
        Entry entry = new Entry();
        return entry;
    }

    /**
     * Takes a Rome feed to sign, removing all entries prior to signing, so that
     * feeds are signed containing only feed-specific elements (no
     * entry/content, etc) Re-attaches entries prior to returning signed feed
     * element.
     */
    public static Element signFeed(Feed feed, KeyPair keyPair) throws IOException {
        Atom10Generator generator = new Atom10Generator();
        org.jdom2.Element jdomFeed;
        try {
            org.jdom2.Document doc = generator.generate(feed);
            jdomFeed = doc.getRootElement();
        } catch (FeedException e) {
            log.error(e.getMessage());
            throw new IOException(e);
        }

        // Remove all Entry nodes prior to signing, as required
        Element domFeed = XmlUtil.toDom(jdomFeed);
        List<Node> removedNodes = removeEntryNodes(domFeed);

        // Actually sign the feed
        try {
            SignatureUtil.signElement(domFeed, keyPair);
        } catch (XMLSignatureException e) {
            log.error(e.getMessage());
            throw new IOException(e);
        }

        // Add back all entry nodes after signing completes
        removedNodes.forEach(entryNode -> domFeed.appendChild(entryNode));

        // Return the signed feed, with entries added back in
        return domFeed;
    }

    /**
     * Removes all Atom Entry nodes from the given DOM feed element and returns
     * them as a new list. DOM Feed element is updated in place.
     */
    public static List<Node> removeEntryNodes(Element domFeed) {
        NodeList nodeList = domFeed.getElementsByTagNameNS(SignedEntry.XMLNS, "entry");
        // we know there's only one entry on this feed
        List<Node> removedNodes = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            domFeed.removeChild(node);
            removedNodes.add(node);
        }
        return removedNodes;
    }
}
