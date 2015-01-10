package com.completetrsst.spring.controllers;

import java.io.IOException;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.completetrsst.operations.StoryOperations;

@Controller
public class RestfulEntryOperations {

    private final static Logger log = LoggerFactory.getLogger(RestfulEntryOperations.class);

    @Autowired
    private StoryOperations storyOperations;

    // TODO: Get this URL showing just the entries
    // http://localhost:8080/viewPublisher/1
    @RequestMapping(value = "/feed/{publisherId}", method = RequestMethod.GET)
    public ResponseEntity<String> readFeed(@PathVariable String publisherId) {
        log.info("Got viewPublisher handler with id: " + publisherId);
        String xmlEntities = storyOperations.readFeed(publisherId);
        // TODO: Add feed xml
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_ATOM_XML);
        return new ResponseEntity<String>(xmlEntities, responseHeaders, HttpStatus.OK);
    }

    /**
     * Expects an already-signed or already-encrypted Atom entry. Validates
     * signature. If validation fails or no signature present, returns
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/publish/{publisherId}", method = RequestMethod.POST, headers = "Accept=application/xml")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody String publishSignedEntry(@PathVariable String publisherId, @RequestBody String signedXml)
            throws XMLSignatureException, IllegalArgumentException {
        log.info("Publish entry");
        return storyOperations.publishSignedEntry(publisherId, signedXml);
    }
}
