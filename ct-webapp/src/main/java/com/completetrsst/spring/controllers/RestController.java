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

import com.completetrsst.operations.TrsstOperations;

@Controller
public class RestController {

    private final static Logger log = LoggerFactory.getLogger(RestController.class);

    @Autowired
    private TrsstOperations trsstOperations;

    // http://localhost:8080/feed/1asdfasdfd
    @RequestMapping(value = "/feed/{publisherId}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<String> readFeed(@PathVariable String publisherId) {
        log.info("Got viewPublisher handler with id: " + publisherId);
        String xmlEntities = trsstOperations.readFeed(publisherId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_ATOM_XML);
        return new ResponseEntity<String>(xmlEntities, responseHeaders, HttpStatus.OK);
    }
    
    // http://localhost:8080/search/searchTerms
    @RequestMapping(value = "/search/{searchTerms}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<String> searchEntries(@PathVariable String searchTerms) {
        log.info("Searching through entries");
        String entries = trsstOperations.searchEntries(searchTerms);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_ATOM_XML);
        return new ResponseEntity<String>(entries, responseHeaders, HttpStatus.OK);
    }

    /**
     * Expects an already-signed or already-encrypted Atom entry. Validates
     * signature. If validation fails or no signature present, returns
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/publish", method = RequestMethod.POST, headers = "Accept=application/xml")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody String publishSignedEntry(@RequestBody String signedXml)
            throws XMLSignatureException, IllegalArgumentException {
        log.info("Publish entry");
        return trsstOperations.publishSignedContent(signedXml);
    }

    
    /**
     * Ping to assert the server is running
     * 
     * @throws IOException
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String ping() {
        log.info("Ping called");
        return "pong";
    }
}
