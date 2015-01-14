package com.completetrsst.rome;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

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

    
    
    public static org.jdom2.Element toJdom(Entry entry) {
        // Build a feed containing only the entry
        final List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        final Feed feed1 = new Feed();
        feed1.setFeedType("atom_1.0");
        feed1.setEntries(entries);

        // Get Rome to output feed as a JDOM document
        final WireFeedOutput wireFeedOutput = new WireFeedOutput();
        Document feedDoc;
        try {
            feedDoc = wireFeedOutput.outputJDom(feed1);
        } catch (IllegalArgumentException | FeedException e) {
            log.debug("Error writing entry to jdom");
            throw new IllegalArgumentException(e);
        }

        // Grab entry element from feed and get JDOM to serialize it
        final Element entryElement = feedDoc.getRootElement().getChildren().get(0);
        return entryElement;
    }

    public static String serialize(Element entryElement) throws IOException {
        Writer writer = new StringWriter();
        final XMLOutputter outputter = new XMLOutputter();
        try {
            outputter.output(entryElement, writer);
            writer.close();
        } catch (IOException e) {
            log.debug("Couldn't write entry element to string");
            throw e;
        }

        return writer.toString();
    }
}
