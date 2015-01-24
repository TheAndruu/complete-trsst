package com.completetrsst.atom;

import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

public class AtomSignerTest {

    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
    private AtomSigner publisher;

    @Before
    public void init() {
        publisher = new AtomSigner();
    }

    @Test
    public void createEntryAsDom() throws Exception {
        Element signedFeedAndEntry = publisher.createEntryAsDom("hi everybody!", keyPair);

        AtomVerifier verifier = new AtomVerifier();
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));

        // Twice to ensure the entries are left verifiable
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));
    }

    @Test
    public void createEntryAsDomTamperedFeed() throws Exception {
        Element signedFeedAndEntry = publisher.createEntryAsDom("hi everybody!", keyPair);

        Text newText = signedFeedAndEntry.getOwnerDocument().createTextNode("new node");
        signedFeedAndEntry.appendChild(newText);

        AtomVerifier verifier = new AtomVerifier();
        // Feed should fail validation
        assertFalse(verifier.isFeedVerified(signedFeedAndEntry));

        // Entry should still pass validation
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));
    }

    @Test
    public void createNewSignedEntryTamperedEntry() throws Exception {
        Element signedFeedAndEntry = publisher.createEntryAsDom("hi everybody!", keyPair);

        Text newText = signedFeedAndEntry.getOwnerDocument().createTextNode("new node");
        Node entryNode = signedFeedAndEntry.getElementsByTagNameNS(AtomSigner.XMLNS, "entry").item(0);
        entryNode.appendChild(newText);

        AtomVerifier verifier = new AtomVerifier();
        // Feed should still validate
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));

        // Entry should fail validation
        assertFalse(verifier.areEntriesVerified(signedFeedAndEntry));
    }

    @Test
    public void createEntryHasFieldsProperlySet() {
        Entry entry = publisher.createEntry("new title");
        assertEquals("new title", entry.getTitle());
        assertTrue(entry.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
        assertTrue(entry.getUpdated().toInstant().isAfter(new Date().toInstant().plusSeconds(-60L)));
        assertTrue(entry.getId().startsWith(AtomSigner.ENTRY_ID_PREFIX));
    }

    /** Assert that newEntry is really just using our other "create new signed entry" method */
    @Test
    public void createEntryDelegatesToCreateEntryAsDom() throws Exception {
        AtomSigner spy = spy(publisher);
        spy.createEntry("hi everybody!", keyPair);
        verify(spy).createEntryAsDom("hi everybody!", keyPair);
    }

    @Test
    public void newEntryId() {
        String id = publisher.newEntryId();
        assertTrue(id.startsWith(AtomSigner.ENTRY_ID_PREFIX));
        String justUuid = id.substring(AtomSigner.ENTRY_ID_PREFIX.length());
        // Type 4 UUID
        assertTrue(justUuid.substring(13, 15).equals("-4"));
        assertTrue(justUuid.substring(18, 19).equals("-"));
        String y = justUuid.substring(19, 20);
        List<String> allowedY = Arrays.asList("8", "9", "A", "B", "a", "b");
        assertTrue(allowedY.contains(y));
    }

    @Test
    public void createFeed() {
        Feed feed = publisher.createFeed(keyPair.getPublic());
        String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
        assertEquals(expectedId, feed.getId());
        assertEquals("atom_1.0", feed.getFeedType());
        assertTrue(feed.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
        assertTrue(feed.getUpdated().toInstant().isAfter(new Date().toInstant().plusSeconds(-60L)));
    }

    @Test
    public void getFeedId() {
        // Equals expected
        String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
        assertEquals(expectedId, publisher.getFeedId(keyPair.getPublic()));
        // Doesn't equal some other key's ID
        KeyPair newKey = new EllipticCurveKeyCreator().createKeyPair();
        assertFalse(expectedId.equals(new AtomSigner().getFeedId(newKey.getPublic())));
    }

}
