package com.completetrsst.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.atom.AtomParser;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.store.Storage;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class CompleteTrsstOpsTest {

    private Storage storage;
    private CompleteTrsstOps ops;

    @Before
    public void init() {
        storage = mock(Storage.class);
        ops = new CompleteTrsstOps();
        ops.setStorage(storage);
    }

    @Test
    public void testSetStorage() {
        // TODO: later
    }

    @Test
    public void testReadFeed_FeedDoesNotExist() {
        // Return empty string b/c feed doesn't exist
        when(storage.getFeed("123")).thenReturn("");
        String result = ops.readFeed("123");
        assertEquals("No entries to view on feed 123", result);
    }

    @Test
    public void testReadFeed_FeedDoesExist() {
        // Test feed doesn't exist
        when(storage.getFeed("123")).thenReturn("<feed></feed>");
        when(storage.getLatestEntries("123")).thenReturn(Arrays.asList("<entry1/>", "<entry2/>"));

        String result = ops.readFeed("123");

        assertEquals("<feed><entry1/><entry2/></feed>", result);
    }

    @Test
    public void testReadFeed_FeedExistsButNoEntries() {
        // Test feed doesn't exist
        when(storage.getFeed("123")).thenReturn("<feed></feed>");
        when(storage.getLatestEntries("123")).thenReturn(Collections.<String>emptyList());

        String result = ops.readFeed("123");

        assertEquals("<feed></feed>", result);
    }

    
    @Test
    public void testPublishSignedContent_FailsIfGivenUnsignedContent() throws Exception {
        String plainEntry = TestUtil.readFile(TestUtil.PLAIN_ATOM_ENTRY);
        try {
            ops.publishSignedContent(plainEntry);
            fail("Should throw XMLSignatureException since the XML isn't signed.");
        } catch (XMLSignatureException e) {
            // We expect to get here since the XML isn't signed
        }
    }

    @Test
    public void testPublishSignedContent_FailsIfGivenTamperedSignedEntry() throws Exception {
        String rawXml = TestUtil.readFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
        try {
            ops.publishSignedContent(rawXml);
            fail("Should throw IllegalArgumentException since the entry XML is tampered.");
        } catch (IllegalArgumentException e) {
            // We expect to get here since the XML isn't signed
        }
    }

    @Test
    public void testPublishSignedContent_FailsIfGivenTamperedSignedFeed() throws Exception {
        String rawXml = TestUtil.readFile(TestUtil.FEED_TAMPERED_ENTRY_VALID);
        try {
            ops.publishSignedContent(rawXml);
            fail("Should throw IllegalArgumentException since the feed XML is tampered.");
        } catch (IllegalArgumentException e) {
            // We expect to get here since the XML isn't signed
        }
    }

    @Test
    public void testPublishSignedContent_FailsIfGivenMalformedXml() throws Exception {
        // missing end tag for feed
        String badXml = "<feed><entry>";
        try {
            ops.publishSignedContent(badXml);
            fail("Should throw IllegalArgumentException since the XML is malformed.");
        } catch (IllegalArgumentException e) {
            // We expect to get here since the XML isn't signed
        }
    }

    @Test
    public void testPublishSignedContent_FailsIfEntryDoesNotMatchFeedId() throws Exception {
        String rawXml = TestUtil.readFile(TestUtil.FEED_TAMPERED_ENTRY_VALID_FROM_OTHER_FEED);

        CompleteTrsstOps spy = spy(ops);
        // To bypass the fact that our test's feed signature doesn't verify, since
        // we copied+pasted an entry from another feed, so use a partial mock
        // to save the trouble of creating a feed signed after a containing a valid entry from another feed
        doNothing().when(spy).verifySignedContent(any(Element.class));

        try {
            spy.publishSignedContent(rawXml);
            fail("Should throw IllegalArgumentException since the XML is malformed.");
        } catch (IllegalArgumentException e) {
            assertEquals("Entries must be signed with same public key as this feed", e.getMessage());
        }
    }

    @Test
    public void testPublishSignedContent_PassesForValidInput() throws Exception {
        String rawXml = new AtomSigner().createEntry("valid entry title", new EllipticCurveKeyCreator().createKeyPair());

        Element domFeed = XmlUtil.toDom(rawXml);
        AtomParser parser = new AtomParser();
        List<Node> entryNodes = parser.removeEntryNodes(domFeed);

        String onlyFeedXml = XmlUtil.serializeDom(domFeed);
        String onlyEntryXml = XmlUtil.serializeDom(entryNodes.get(0));

        String feedId = TrsstKeyFunctions.removeFeedUrnPrefix(parser.getId(domFeed));
        
        String result = ops.publishSignedContent(rawXml);
        
        verify(storage).storeFeed(feedId, onlyFeedXml);
        verify(storage).storeEntry(feedId, "valid entry title", onlyEntryXml);

        assertEquals("Stored onto feed "+ feedId, result);
    }

    @Test
    public void testSearchEntries_HasResults() {
        when(storage.searchEntries("a search string")).thenReturn(Arrays.asList("<entry1/>", "<entry2/>"));
        String result = ops.searchEntries("a search string");
        assertTrue(result.contains("<entry1/>"));
        assertTrue(result.contains("<entry2/>"));
        assertTrue(result.contains("<feed"));
        assertTrue(result.contains("</feed>"));
    }

    @Test
    public void testSearchEntries_HasNoResults() {
        when(storage.searchEntries("a search string")).thenReturn(Collections.<String> emptyList());
        String result = ops.searchEntries("a search string");
        assertFalse(result.contains("entry"));
        assertTrue(result.contains("<feed"));
        assertTrue(result.contains("</feed>"));
    }

}
