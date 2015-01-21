package com.completetrsst.spring.controllers;

import static org.junit.Assert.assertEquals;
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
public class RestEndpointControllerIntegrationTest {

    private static final RestTemplate rest = new RestTemplate();
    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
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
        String rawXml = signer.newEntry("First new entry title!", keyPair);
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
        String rawXml = signer.newEntry("my own title to replace", keyPair);
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
        rest.postForEntity("http://localhost:8080/publish", signer.newEntry("First new post!", keyPair), String.class);
        // To ensure we have different timestamps on the entries
        Thread.sleep(1);
        rest.postForEntity("http://localhost:8080/publish", signer.newEntry("Second entry title!", keyPair), String.class);
        Thread.sleep(1);
        rest.postForEntity("http://localhost:8080/publish", signer.newEntry("Third time's the charm!", keyPair), String.class);

        // Now read the feed
        String feedId = TrsstKeyFunctions.toFeedId(keyPair.getPublic());
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

//    @Test
//    public void testSearchEntries() throws Exception {
//        rest.postForEntity("http://localhost:8080/s", signer.newEntry("First new post!", keyPair), String.class);
//        // To ensure we have different timestamps on the entries
//        Thread.sleep(1);
//        rest.postForEntity("http://localhost:8080/publish", signer.newEntry("Second entry title!", keyPair), String.class);
//        Thread.sleep(1);
//        rest.postForEntity("http://localhost:8080/publish", signer.newEntry("Third time's the charm!", keyPair), String.class);
//
//        // Now read the feed
//        String feedId = TrsstKeyFunctions.toFeedId(keyPair.getPublic());
//    }
    
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
