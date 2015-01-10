package com.completetrsst.spring.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.completetrsst.operations.StoryOperations;

//@Controller
public class StoryFeedController {

	private final static Logger log = LoggerFactory
			.getLogger(StoryFeedController.class);

	@Autowired
	private StoryOperations storyOperations;

	@RequestMapping(value = "/feed/{publisherId}", method = RequestMethod.GET)
	public ModelAndView getContent(@PathVariable String publisherId) {
		log.info("Got request to show feed by storyFeedController");
		ModelAndView mav = new ModelAndView();
//		mav.setViewName("storyContent");
//		mav.addObject("stories", storyOperations.getStories(publisherId));
		return mav;
	}
}