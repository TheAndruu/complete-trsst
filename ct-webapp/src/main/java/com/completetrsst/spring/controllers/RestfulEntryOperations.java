package com.completetrsst.spring.controllers;

import java.io.IOException;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.completetrsst.model.SignedEntry;
import com.completetrsst.operations.StoryOperations;

@RestController
public class RestfulEntryOperations implements StoryOperations {

    private final static Logger log = LoggerFactory.getLogger(RestfulEntryOperations.class);

    @Autowired
    private StoryOperations storyOperations;

    @RequestMapping(value = "/createStory/{publisherId}", method = RequestMethod.POST)
    @Override
    public void create(@PathVariable String publisherId, @RequestBody SignedEntry story) {
        log.info("Got create handler");
        storyOperations.create(publisherId, story);
    }

    // TODO: Get this URL showing just the entries
    // http://localhost:8080/viewPublisher/1
    @RequestMapping(value = "/viewPublisher/{publisherId}", method = RequestMethod.GET)
    @Override
    public List<String> getStories(@PathVariable String publisherId) {
        log.info("Got viewPublisher handler with id: " + publisherId);
        return storyOperations.getStories(publisherId);
    }

    /**
     * Expects an already-signed or already-encrypted Atom entry. Validates
     * signature. If validation fails or no signature present, returns
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/publish/{publisherId}", method = RequestMethod.POST, headers = "Accept=application/xml")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public String publishSignedEntry(@PathVariable String publisherId, @RequestBody String signedXml)
            throws XMLSignatureException, IllegalArgumentException {
        log.info("Publish entry");
        return storyOperations.publishSignedEntry(publisherId, signedXml);
    }
}
