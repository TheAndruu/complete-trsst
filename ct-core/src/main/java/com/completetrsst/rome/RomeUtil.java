package com.completetrsst.rome;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

public class RomeUtil {

	private static Entry createSimpleEntry() {
		Entry entry = new Entry();
		entry.setId(UUID.randomUUID().toString());
		entry.setTitle("Title: create entry for element conversion test");
		entry.setUpdated(new Date());
		entry.setPublished(new Date());
		return entry;
	}

	private static org.jdom2.Element toJdom(Entry entry) throws Exception {
		// Note: This is from Atom10Generator.serializeEntry()
		// it also has .generate(WireFeed) which returns a jdom2 element

		// Build a feed containing only the entry
		final List<Entry> entries = new ArrayList<Entry>();
		entries.add(entry);
		final Feed feed1 = new Feed();
		feed1.setFeedType("atom_1.0");
		feed1.setEntries(entries);

		// Get Rome to output feed as a JDOM document
		final WireFeedOutput wireFeedOutput = new WireFeedOutput();
		org.jdom2.Document feedDoc = wireFeedOutput.outputJDom(feed1);

		// Grab entry element from feed and get JDOM to serialize it
		final org.jdom2.Element entryElement = feedDoc.getRootElement().getChildren().get(0);
		return entryElement;
	}

	private static Element toDom(Entry entry) throws Exception {
		// Note: This is from Atom10Generator.serializeEntry()
		// it also has .generate(WireFeed) which returns a jdom2 element

		// Build a feed containing only the entry
		final List<Entry> entries = new ArrayList<Entry>();
		entries.add(entry);
		final Feed feed1 = new Feed();
		feed1.setFeedType("atom_1.0");
		feed1.setEntries(entries);

		// Get Rome to output feed as a JDOM document
		final WireFeedOutput wireFeedOutput = new WireFeedOutput();
		Document feedDoc = wireFeedOutput.outputW3CDom(feed1);
		// org.jdom2.
		// Grab the entry element from the feed
		Node node = feedDoc.getDocumentElement().getFirstChild();
		return (Element) node;
	}

	private static String serialize(Entry entry) throws Exception {
		StringWriter writer = new StringWriter();
		Atom10Generator.serializeEntry(entry, writer);
		writer.close();
		return writer.toString();
	}
}
