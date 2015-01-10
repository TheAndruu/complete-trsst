package com.completetrsst.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.SignedEntry;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

    private Map<String, List<SignedEntry>> publishersToStories = new HashMap<String, List<SignedEntry>>();

    @Override
    public void create(String publisherId, SignedEntry story) {
        addEntry(publisherId, story);
    }

    // TODO: Move this to a utility class -- our clients will want to use it
    private String createUniqueId() {
        return UUID.randomUUID().toString();
    }

    // TODO: Refactor this with Java 8 lambdas!
    @Override
    public String readFeed(String publisherId) {
        List<SignedEntry> entries = publishersToStories.get(publisherId);
        StringBuilder builder = new StringBuilder();
        for (SignedEntry entry : entries) {
        	builder.append(entry.getRawXml());
        }
        return builder.toString();
    }

    // TODO: Only verify if signature is present
    // add other validations and responses, like "required
    // entry/title/published' nodes
    /** Takes in raw signed XML to add it to an atom feed */
    @Override
    public String publishSignedEntry(String publisherId, String signedXml) throws XMLSignatureException,
            IllegalArgumentException {
        log.info("Received signed XML to publish!");

        Element domElement;
        try {
            domElement = XmlUtil.toDom(signedXml);
        } catch (IOException e) {
            log.debug(e.getMessage());
            throw new IllegalArgumentException(e);
        }
        boolean isValid = SignatureUtil.verifySignature(domElement);
        if (!isValid) {
            throw new XMLSignatureException("Proper XML, but signature doesn't validate!");
        }

        SignedEntry entry = createSignedEntry(domElement, signedXml);
        addEntry(publisherId, entry);
        return "Stored verified signed entry on feed: " + publisherId;
    }

    // TODO: Move these to a helper class
    private SignedEntry createSignedEntry(Element domElement, String signedXml) throws IllegalArgumentException {
        String title = getTitle(domElement);
        String id = getId(domElement);
        String dateUpdated = getDateUpdated(domElement);
        SignedEntry entry = new SignedEntry();
        entry.setTitle(title);
        entry.setId(id);
        entry.setDateUpdated(dateUpdated);
        entry.setRawXml(signedXml);
        return entry;
    }

    private String getDateUpdated(Element domElement) {
        NodeList nl = domElement.getElementsByTagNameNS(SignedEntry.XMLNS, "updated");
        if (nl.getLength() == 0) {
            log.debug("Atom entries must have an <updated> element");
            throw new IllegalArgumentException("Atom entries must have a <updated> element");
        } else {
            return nl.item(0).getTextContent();
        }
    }

    private String getId(Element domElement) {
        NodeList nl = domElement.getElementsByTagNameNS(SignedEntry.XMLNS, "id");
        if (nl.getLength() == 0) {
            log.debug("Atom entries must have an <id> element");
            throw new IllegalArgumentException("Atom entries must have a <id> element");
        } else {
            return nl.item(0).getTextContent();
        }
    }

    private String getTitle(Element domElement) {
        NodeList nl = domElement.getElementsByTagNameNS(SignedEntry.XMLNS, "title");
        if (nl.getLength() == 0) {
            log.debug("Atom entries must have an <entry> element");
            throw new IllegalArgumentException("Atom entries must have a <title> element");
        } else {
            return nl.item(0).getTextContent();
        }
    }

    private void addEntry(String publisherId, SignedEntry story) {
        List<SignedEntry> existingStories = publishersToStories.get(publisherId);
        if (existingStories == null) {
            existingStories = new ArrayList<SignedEntry>();
        }
        existingStories.add(story);
        Collections.sort(existingStories);

        publishersToStories.put(publisherId, existingStories);
    }

}
