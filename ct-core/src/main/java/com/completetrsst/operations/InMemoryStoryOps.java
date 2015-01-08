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
import com.completetrsst.model.Story;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

	private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

	private Map<String, List<Story>> publishersToStories = new HashMap<String, List<Story>>();

	@Override
	public void create(String publisherId, Story story) {
		List<Story> existingStories = getStories(publisherId);
		story.setId(createUniqueId());
		existingStories.add(story);
		sortByDateDescending(existingStories);

		publishersToStories.put(publisherId, existingStories);
	}

	private String createUniqueId() {
		return UUID.randomUUID().toString();
	}

	private void sortByDateDescending(List<Story> existingStories) {
		Collections.sort(existingStories, new Comparator<Story>() {
			@Override
			public int compare(Story story1, Story story2) {
				return story2.getDatePublished().compareTo(story1.getDatePublished());
			}
		});
	}

	@Override
	public List<Story> getStories(String publisherId) {
		List<Story> stories = publishersToStories.get(publisherId);
		return stories == null ? new ArrayList<Story>() : stories;
	}

	// TODO: Verify the signature from the passed-in input
	// that'd be a big success
	@Override
	public String publishEntry(String publisherId, String xml) {
		// TODO: need DOM element, and then verify it
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
