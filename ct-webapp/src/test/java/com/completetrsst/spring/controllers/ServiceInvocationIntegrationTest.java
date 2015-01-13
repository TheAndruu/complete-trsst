package com.completetrsst.spring.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNoException;

import java.net.ConnectException;
import java.security.KeyPair;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Element;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.rome.FeedCreator;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

/** Integration tests which only run if the local server is running */
public class ServiceInvocationIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(ServiceInvocationIntegrationTest.class);

    private static final RestTemplate rest = new RestTemplate();

    @BeforeClass
    /** Only run integration tests if the server is running */
    public static void onlyTestIfServerRunning() {
        try {
            sendPing();
        } catch (RuntimeException e) {
            assumeNoException(e);
        }
    }

    @Test
    public void testPublishSignedEntry() throws Exception {
        KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
        Feed feed = FeedCreator.createFor(keyPair);
        // TODO: This works correctly as a test for now. The code its testing
        // needs to be updated
        // to extract entries and feeds separately
        Element signedFeed = FeedCreator.signFeed(feed, keyPair);
        String rawXml = XmlUtil.serializeDom(signedFeed);
        log.info("Raw xml");
        log.info(rawXml);
        ResponseEntity<String> response = rest.postForEntity("http://localhost:8080/publish/1", rawXml, String.class);
        log.info("Got response: " + response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        log.info("Payload: " + response.getBody());
    }

    @Test
    public void testPing() throws ConnectException {
        ResponseEntity<String> pingResponse = sendPing();
        assertEquals(HttpStatus.OK, pingResponse.getStatusCode());
        assertEquals("pong", pingResponse.getBody());
    }

    private static ResponseEntity<String> sendPing() {
        return rest.getForEntity("http://localhost:8080/ping", String.class);
    }
}
