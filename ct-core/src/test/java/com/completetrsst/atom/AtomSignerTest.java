package com.completetrsst.atom;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;
import static com.completetrsst.constants.Nodes.ATOM_ENTRY;
import static com.completetrsst.constants.Nodes.ENTRY_ID_PREFIX;
import static com.completetrsst.constants.Nodes.TRSST_ENCRYPT;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;
import static com.completetrsst.constants.Nodes.TRSST_SIGN;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.completetrsst.crypto.Common;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

public class AtomSignerTest {

    private static final KeyPair signingPair = new EllipticCurveKeyCreator().createKeyPair();
    private static final PublicKey encryptPublicKey = new EllipticCurveKeyCreator().createKeyPair().getPublic();
    private AtomSigner signer;
    private static final AtomParser parser = new AtomParser();

    @Before
    public void init() {
        signer = new AtomSigner();
    }

    @Test
    public void createEntryAsDom() throws Exception {
        Element signedFeedAndEntry = signer.createEntryAsDom("hi everybody!", "", signingPair, encryptPublicKey);
        AtomVerifier verifier = new AtomVerifier();
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));

        // Twice to ensure the entries are left verifiable
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));
    }

    @Test
    public void createEntryAsDomTamperedFeed() throws Exception {
        Element signedFeedAndEntry = signer.createEntryAsDom("hi everybody!", "", signingPair, encryptPublicKey);

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
        Element signedFeedAndEntry = signer.createEntryAsDom("hi everybody!", "", signingPair, encryptPublicKey);

        Text newText = signedFeedAndEntry.getOwnerDocument().createTextNode("new node");
        Node entryNode = signedFeedAndEntry.getElementsByTagNameNS(ATOM_XMLNS, ATOM_ENTRY).item(0);
        entryNode.appendChild(newText);

        AtomVerifier verifier = new AtomVerifier();
        // Feed should still validate
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));

        // Entry should fail validation
        assertFalse(verifier.areEntriesVerified(signedFeedAndEntry));
    }

    @Test
    public void createEntryHasFieldsProperlySet() {
        Entry entry = signer.createEntry("new title", signingPair.getPublic(), "");
        assertEquals("new title", entry.getTitle());
        assertTrue(entry.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
        assertTrue(entry.getUpdated().toInstant().isAfter(new Date().toInstant().plusSeconds(-60L)));
        assertTrue(entry.getId().startsWith(ENTRY_ID_PREFIX));
    }

    @Test
    public void createEntryHasPreviousSignatureProperlySet() {
        Entry entry = signer.createEntry("new title", signingPair.getPublic(), "prevSigValue");
        assertTrue(entry.getId().startsWith(ENTRY_ID_PREFIX));

        List<org.jdom2.Element> markup = entry.getForeignMarkup();
        boolean hasSigValue = false;
        for (org.jdom2.Element mark : markup) {
            if (mark.getText().equals("prevSigValue")) {
                assertTrue(mark.getName().equals(TRSST_PREDECESSOR));
                assertTrue(mark.getNamespace().equals(TRSST_NAMESPACE));
                hasSigValue = true;
                break;
            }
        }
        assertTrue(hasSigValue);
    }

    @Test
    public void createEntryHasPredecessorOnFeed() throws Exception {
        Element feed = signer.createEntryAsDom("title doesnt matter", "previous value of sig", signingPair, encryptPublicKey);
        Element preNode = parser.getFirstPredecessorNode(feed);

        assertEquals(preNode.getTextContent(), "previous value of sig");
    }

    @Test
    public void createEntryHasSignElementOnFeed() throws Exception {
        Element feed = signer.createEntryAsDom("title with sign node", "", signingPair, encryptPublicKey);
        Element signNode = (Element) parser.getFirstNode(feed, TRSST_XMLNS, TRSST_SIGN);
        assertEquals(signNode.getTextContent(), Common.toX509FromPublicKey(signingPair.getPublic()));
    }

    @Test
    public void createEntryHasEncryptElementOnFeed() throws Exception {
        Element feed = signer.createEntryAsDom("title with encrypt node", "", signingPair, encryptPublicKey);
        Element signNode = (Element) parser.getFirstNode(feed, TRSST_XMLNS, TRSST_ENCRYPT);
        assertEquals(signNode.getTextContent(), Common.toX509FromPublicKey(encryptPublicKey));
    }

    @Test
    public void createEntrySupplyingAndExtractingPredecessor() throws Exception {
        Element feed = signer.createEntryAsDom("title doesnt matter", "", signingPair, encryptPublicKey);
        String firstSignedValue = parser.getEntrySignatureValue(feed);

        feed = signer.createEntryAsDom("second title doesnt matter", firstSignedValue, signingPair, encryptPublicKey);

        Element latestEntrysPredecessorValue = parser.getFirstPredecessorNode(feed);

        // The latest Entry node's 'predecessor' value should be equal to the older entry's signature value
        assertEquals(firstSignedValue, latestEntrysPredecessorValue.getTextContent());
    }

    /** Assert that newEntry is really just using our other "create new signed entry" method */
    @Test
    public void createEntryDelegatesToCreateEntryAsDom() throws Exception {
        AtomSigner spy = spy(signer);
        spy.createEntry("hi everybody!", "", signingPair, encryptPublicKey);
        verify(spy).createEntryAsDom("hi everybody!", "", signingPair, encryptPublicKey);
    }

    @Test
    public void newEntryId() {
        String id = signer.newEntryId(signingPair.getPublic());
        String expectedFeedPart = TrsstKeyFunctions.toFeedId(signingPair.getPublic());

        String[] splitId = id.split(":");
        assertTrue(id.startsWith(ENTRY_ID_PREFIX));
        String [] splitPrefix = ENTRY_ID_PREFIX.split(":");
        assertEquals(splitPrefix[0], splitId[0]);
        assertEquals(splitPrefix[1], splitId[1]);
        assertEquals(expectedFeedPart, splitId[2]);
        long timePart = Long.parseLong(splitId[3]);

        long now = System.currentTimeMillis();
        assertTrue(timePart > now - 2000);
        assertTrue(timePart <= now);
    }

    @Test
    public void createFeed() {
        Feed feed = signer.createFeed(signingPair.getPublic(), encryptPublicKey);
        String expectedId = toFeedUrn(toFeedId(signingPair.getPublic()));
        assertEquals(expectedId, feed.getId());
        assertEquals("atom_1.0", feed.getFeedType());
        assertTrue(feed.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
        assertTrue(feed.getUpdated().toInstant().isAfter(new Date().toInstant().plusSeconds(-60L)));
    }

    @Test
    public void getFeedId() {
        // Equals expected
        String expectedId = toFeedUrn(toFeedId(signingPair.getPublic()));
        assertEquals(expectedId, signer.getFeedId(signingPair.getPublic()));
        // Doesn't equal some other key's ID
        KeyPair newKey = new EllipticCurveKeyCreator().createKeyPair();
        assertFalse(expectedId.equals(new AtomSigner().getFeedId(newKey.getPublic())));
    }

    /**
     * This is an important test, since moreoften we'll be verifying signatures after they've been serialized across a wire The effect of doing so
     * often means XMLNS declarations will be moved around, breaking signatures in the process
     */
    @Test
    public void signatureVerifiesAfterSerialization() throws Exception {
        Element signedFeedAndEntry = signer.createEntryAsDom("another new title!", "", signingPair, encryptPublicKey);

        signedFeedAndEntry = XmlUtil.toDom(XmlUtil.serializeDom(signedFeedAndEntry));

        AtomVerifier verifier = new AtomVerifier();
        assertTrue(verifier.isFeedVerified(signedFeedAndEntry));
        assertTrue(verifier.areEntriesVerified(signedFeedAndEntry));
    }

}
