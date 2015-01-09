package com.completetrsst.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.CtEntry;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

	private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

	private Map<String, List<CtEntry>> publishersToStories = new HashMap<String, List<CtEntry>>();

	@Override
	public void create(String publisherId, CtEntry story) {
		List<CtEntry> existingStories = getStories(publisherId);
		story.setId(createUniqueId());
		existingStories.add(story);

		Collections.sort(existingStories);

		publishersToStories.put(publisherId, existingStories);
	}

	private String createUniqueId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public List<CtEntry> getStories(String publisherId) {
		List<CtEntry> stories = publishersToStories.get(publisherId);
		return stories == null ? new ArrayList<CtEntry>() : stories;
	}

	// TODO: Only verify if signature is present
	// add other validations and responses, like "required entry/title/published' nodes
	/** Takes in raw signed XML to add it to an atom feed */
	@Override
	public String publishEntry(String publisherId, String xml) {
		log.info("Got to in memory ops!");

		Element domElement;
		try {
			domElement = XmlUtil.toDom(xml);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return "oops: " + e.getMessage();
		}
		boolean isValid;
		try {
			isValid = SignatureUtil.verifySignature(domElement);
		} catch (XMLSignatureException e) {
			log.error(e.getMessage(), e);
			return "oops: " + e.getMessage();
		}

		return "Valid? " + isValid + " " + xml;
	}

}
