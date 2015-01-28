package com.completetrsst.spring.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.net.ConnectException;
import java.security.KeyPair;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;

/** Integration tests which only run if the local server is running */
public class RestControllerIntegrationTest {

    private static final RestTemplate rest = new RestTemplate();
    private static final KeyPair signingPair = new EllipticCurveKeyCreator().createKeyPair();
    private static final KeyPair encryptPair = new EllipticCurveKeyCreator().createKeyPair();
    private static final AtomSigner signer = new AtomSigner();

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
        String rawXml = signer.createEntry("In the jungle the mighty jungle, the lion sleeps tonight!", "", signingPair, encryptPair.getPublic());
        ResponseEntity<String> response;
        try {
            response = rest.postForEntity("http://localhost:8080/publish", rawXml, String.class);
        } catch (Exception e) {
            fail();
            return;
        }
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testPublishTamperedEntry() throws Exception {
        String rawXml = signer.createEntry("my own title to replace", "", signingPair, encryptPair.getPublic());
        rawXml = rawXml.replace("my own title to replace", "i've been tampered!");

        try {
            rest.postForEntity("http://localhost:8080/publish", rawXml, String.class);
            fail();
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_ACCEPTABLE, e.getStatusCode());
        }
    }

    @Test
    public void testReadFeed() throws Exception {
        AtomSigner signer = new AtomSigner();
        rest.postForEntity("http://localhost:8080/publish", signer.createEntry("First new post!", "", signingPair, encryptPair.getPublic()),
                String.class);
        // To ensure we have different timestamps on the entries
        Thread.sleep(1);
        rest.postForEntity("http://localhost:8080/publish", signer.createEntry("Second entry title!", "", signingPair, encryptPair.getPublic()),
                String.class);
        Thread.sleep(1);
        rest.postForEntity("http://localhost:8080/publish", signer.createEntry("Third time's the charm!", "", signingPair, encryptPair.getPublic()),
                String.class);

        // Now read the feed
        String feedId = TrsstKeyFunctions.toFeedId(signingPair.getPublic());
        ResponseEntity<String> response = rest.getForEntity("http://localhost:8080/feed/" + feedId, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("First new post!"));
        assertTrue(responseBody.contains("Second entry title!"));
        assertTrue(responseBody.contains("Third time's the charm!"));
        int expectedTop = responseBody.indexOf("Third time's the charm");
        int expectedMid = responseBody.indexOf("Second entry title!");
        int expectedLast = responseBody.indexOf("First new post!");
        assertTrue(expectedTop != 0);
        assertTrue(expectedMid != 0);
        assertTrue(expectedLast != 0);
        assertTrue(expectedTop < expectedMid);
        assertTrue(expectedMid < expectedLast);
    }

    @Test
    public void testSearchEntries() throws Exception {
        rest.postForEntity("http://localhost:8080/publish",
                signer.createEntry("George Washington wrote this new post!", "", signingPair, encryptPair.getPublic()), String.class);
        rest.postForEntity("http://localhost:8080/publish",
                signer.createEntry("John Adams wrote this new entry!", "", signingPair, encryptPair.getPublic()), String.class);

        String response = rest.getForEntity("http://localhost:8080/search/wrote", String.class).getBody();
        assertTrue(response.contains("George Washington"));
        assertTrue(response.contains("John Adams"));

        response = rest.getForEntity("http://localhost:8080/search/washington", String.class).getBody();
        assertTrue(response.contains("George Washington"));
        assertFalse(response.contains("John Adams"));

        response = rest.getForEntity("http://localhost:8080/search/john+adams", String.class).getBody();
        assertFalse(response.contains("George Washington"));
        assertTrue(response.contains("John Adams"));

        response = rest.getForEntity("http://localhost:8080/search/potato", String.class).getBody();
        assertFalse(response.contains("George Washington"));
        assertFalse(response.contains("John Adams"));
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
