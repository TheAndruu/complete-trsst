package com.cuga.completetrsst.spring.controllers;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cuga.completetrsst.model.Story;
import com.cuga.completetrsst.operations.StoryOperations;

@RestController
public class RestfulStoryOperations implements StoryOperations {

	private final static Logger log = LoggerFactory
			.getLogger(RestfulStoryOperations.class);

	@Autowired
	private StoryOperations storyOperations;

	@RequestMapping(value = "/createStory/{publisherId}", method = RequestMethod.POST)
	@Override
	public void create(@PathVariable String publisherId,
			@RequestBody Story story) {
		log.info("Got create handler");
		storyOperations.create(publisherId, story);
	}

	@RequestMapping(value = "/viewPublisher/{publisherId}", method = RequestMethod.GET)
	@Override
	public List<Story> getStories(@PathVariable String publisherId) {
		log.info("Got viewPublisher handler with id: " + publisherId);
		return storyOperations.getStories(publisherId);
	}
}
