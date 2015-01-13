package com.completetrsst.rome;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.impl.Atom10Generator;

public class EntryCreator {

	private static final Logger log = LoggerFactory.getLogger(EntryCreator.class);

	static final String ENTRY_ID_PREFIX = "urn:uuid:";

	/**
	 * Creates an entry with the given text, simple use case for now.
	 */
	public static Entry create(String title) {
		Entry entry = new Entry();
		entry.setTitle(title);
		entry.setUpdated(new Date());
		entry.setId(newEntryId());
		return entry;
	}

	static String newEntryId() {
		return ENTRY_ID_PREFIX + UUID.randomUUID().toString();
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
