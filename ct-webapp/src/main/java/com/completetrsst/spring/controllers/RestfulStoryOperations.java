package com.completetrsst.spring.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.completetrsst.model.CtEntry;
import com.completetrsst.operations.StoryOperations;

@RestController
public class RestfulStoryOperations implements StoryOperations {

    private final static Logger log = LoggerFactory.getLogger(RestfulStoryOperations.class);

    @Autowired
    private StoryOperations storyOperations;

    @RequestMapping(value = "/createStory/{publisherId}", method = RequestMethod.POST)
    @Override
    public void create(@PathVariable String publisherId, @RequestBody CtEntry story) {
        log.info("Got create handler");
        storyOperations.create(publisherId, story);
    }

    @RequestMapping(value = "/viewPublisher/{publisherId}", method = RequestMethod.GET)
    @Override
    public List<CtEntry> getStories(@PathVariable String publisherId) {
        log.info("Got viewPublisher handler with id: " + publisherId);
        return storyOperations.getStories(publisherId);
    }

    
    // TODO: Need to make a jaxb- annotated class to represent signed content?
    // if need to, an prob re-use SignatureMethodType (org.apache.xml.security.binding.xmldsig)
    // can follow example here: http://blog.bdoughan.com/2010/09/processing-atom-feeds-with-jaxb.html
    // can generate the classes with that xjc link above
    /** Expects an already-signed or already-encrypted Atom entry */
    @RequestMapping(value = "/publish/{publisherId}", method = RequestMethod.POST, headers = "Accept=application/xml")
    @Override
    public String publishEntry(@PathVariable String publisherId, @RequestBody String entryElement) {
        log.info("Publish entry");
        return storyOperations.publishEntry(publisherId, entryElement);
    }
}
