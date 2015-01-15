package com.completetrsst.crypto.xml;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.rome.EntryCreator;
import com.completetrsst.rome.FeedCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

public class SignatureUtilTests {

    private static final Logger log = LoggerFactory.getLogger(SignatureUtilTests.class);

    private KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

    @Test
    public void signNewElement() throws Exception {
        Entry entry = EntryCreator.create("foobar");
        Element entryDom = EntryCreator.toDom(entry);
        SignatureUtil.signElement(entryDom, keyPair);

        boolean result = SignatureUtil.verifySignature(entryDom);
        assertTrue(result);
    }

    @Test
    public void elementVerifiesAfterDomConversion() throws Exception {
        Entry entry = EntryCreator.create("foobar");
        Element entryDom = EntryCreator.toDom(entry);
        SignatureUtil.signElement(entryDom, keyPair);

        entryDom = XmlUtil.toDom(XmlUtil.toJdom(entryDom));

        boolean result = SignatureUtil.verifySignature(entryDom);
        assertTrue(result);
    }

    @Test
    public void elementVerifiesAfterSerialization() throws Exception {
        Entry entry = EntryCreator.create("foobar");
        Element entryDom = EntryCreator.toDom(entry);
        SignatureUtil.signElement(entryDom, keyPair);

        entryDom = XmlUtil.toDom(XmlUtil.serializeDom(entryDom));

        boolean result = SignatureUtil.verifySignature(entryDom);
        assertTrue(result);
    }

    @Test
    public void elementVerifiesSerializedBeforeSignature() throws Exception {
        Entry entry = EntryCreator.create("foobar");
        String rawXml = XmlUtil.serializeDom(EntryCreator.toDom(entry));

        Element entryDom = XmlUtil.toDom(rawXml);
        SignatureUtil.signElement(entryDom, keyPair);

        boolean result = SignatureUtil.verifySignature(entryDom);
        assertTrue(result);
    }

    @Test
    public void elementVerifiesSerializedBeforeAndAfterSignature() throws Exception {
        Entry entry = EntryCreator.create("foobar");
        String rawXml = XmlUtil.serializeDom(EntryCreator.toDom(entry));

        Element entryDom = XmlUtil.toDom(rawXml);
        SignatureUtil.signElement(entryDom, keyPair);

        rawXml = XmlUtil.serializeDom(entryDom);
        log.info("Foobarfoo");
        log.info(rawXml);

        entryDom = XmlUtil.toDom(rawXml);

        boolean result = SignatureUtil.verifySignature(entryDom);
        assertTrue(result);
    }

    // TODO Move this to signature utils
    private static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();
        return doc;
    }

    @Test
    public void verifySignedElementCanBeMovedAndIndependentlyVerified() throws Exception {
        Element signedEntry = EntryCreator.toDom(EntryCreator.create("my new entry"));
        
        Document doc = newDocument();
        doc.adoptNode(signedEntry);
        doc.appendChild(signedEntry);
        SignatureUtil.signElement(signedEntry, keyPair);

        assertTrue(SignatureUtil.verifySignature(signedEntry));

        // Verify we can move this signedEntry to a new document
        doc = newDocument();
        doc.adoptNode(signedEntry);
        doc.appendChild(signedEntry);
        assertTrue(SignatureUtil.verifySignature(signedEntry));

        // Try serializing it and back and verifying again
        String rawXml = XmlUtil.serializeDom(signedEntry);
        signedEntry = XmlUtil.toDom(rawXml);
        assertTrue(SignatureUtil.verifySignature(signedEntry));
    }
    
    @Test
    public void verifySignedElementCanBeMovedToFeedAndIndependentlyVerified() throws Exception {

        // TODO: Take lessons learned from here into main classes
        
        Entry entry = EntryCreator.create("my new entry");
        Element signedEntry = EntryCreator.toDom(entry);
        
        Document doc = newDocument();
        doc.adoptNode(signedEntry);
        doc.appendChild(signedEntry);
        SignatureUtil.signElement(signedEntry, keyPair);

        assertTrue(SignatureUtil.verifySignature(signedEntry));

        Feed feed = FeedCreator.createFor(keyPair);
        Element signedFeed = FeedCreator.signFeed(feed, keyPair);
        assertTrue(SignatureUtil.verifySignature(signedFeed));
        
        signedFeed.getOwnerDocument().adoptNode(signedEntry);
        signedFeed.appendChild(signedEntry);
        // TODO: Seprate out the removal of stuff below
        signedFeed = XmlUtil.toDom(XmlUtil.serializeDom(signedFeed));
        List<Node> removedEntries = FeedCreator.removeEntryNodes(signedFeed);
        assertTrue(SignatureUtil.verifySignature(signedFeed));

        // This is how we can verify signatures by themselves! - refactor into other class
        assertEquals(1, removedEntries.size());
        for (Node node : removedEntries) {
            Element signedNode = (Element)node;
            doc = newDocument();
            doc.adoptNode(signedNode);
            doc.appendChild(signedNode);
            assertTrue(SignatureUtil.verifySignature(signedNode));
        }
        // TODO: resume here
        assertTrue(false);
    }

    /** Asserts proper verification of attached enveloped XML Digital Signature */
    @Test
    public void attachSignature() throws Exception {
        Element element = TestUtil.readDomFromFile(TestUtil.PLAIN_ATOM_ENTRY);

        SignatureUtil.signElement(element, keyPair);

        boolean result = SignatureUtil.verifySignature(element);
        assertTrue(result);
    }

    /**
     * Asserts verification properly fails if XML document has been tampered
     * with
     */
    @Test
    public void verifyTamperedXmlFailsSignature() throws Exception {
        Element element = TestUtil.readDomFromFile(TestUtil.PLAIN_ATOM_ENTRY);

        SignatureUtil.signElement(element, keyPair);

        assertTrue(SignatureUtil.verifySignature(element));

        // Convert to jdom so we can edit it easier
        org.jdom2.Element asJdom = XmlUtil.toJdom(element);
        org.jdom2.Element anything = new org.jdom2.Element("tamper");
        anything.setText("doesn't belong");
        asJdom.addContent(anything);
        // back to w3 dom
        element = XmlUtil.toDom(asJdom);

        // TODO: This is because the signature is signing the empty document!
        boolean result = SignatureUtil.verifySignature(element);
        assertFalse(result);
    }

    /**
     * Reads an already-signed element and verifies its signature, with the
     * read-as-JDOM method
     */
    @Test
    public void verifyStoredSignedXmlReadAsJdom() throws Exception {
        org.jdom2.Element element = TestUtil.readJDomFromFile(TestUtil.SIGNED_ATOM_ENTRY);

        // now convert element to DOM and verify
        Element signedAsDom = XmlUtil.toDom(element);

        boolean isValid = SignatureUtil.verifySignature(signedAsDom);
        assertTrue(isValid);
    }

    /**
     * Reads an already-signed element and verifies its signature, with the
     * read-as-JDOM method
     */
    @Test
    public void verifyStoredTamperedAsJDomXmlFails() throws Exception {
        org.jdom2.Element element = TestUtil.readJDomFromFile(TestUtil.TAMPERED_ATOM_ENTRY);

        // now convert element to DOM and verify
        Element signedAsDom = XmlUtil.toDom(element);

        boolean isValid = SignatureUtil.verifySignature(signedAsDom);
        assertFalse(isValid);
    }

    /**
     * Reads an already-signed element and verifies its signature, with the
     * read-as-DOM method
     */
    @Test
    public void verifyStoredTamperedAsDomXmlFails() throws Exception {
        org.w3c.dom.Element element = TestUtil.readDomFromFile(TestUtil.TAMPERED_ATOM_ENTRY);

        boolean isValid = SignatureUtil.verifySignature(element);
        assertFalse(isValid);
    }

    /**
     * Reads an already-signed element and verifies its signature, with the
     * read-as-JDOM method
     */
    @Test
    public void verifyStoredSignedXmlReadAsDom() throws Exception {
        org.w3c.dom.Element element = TestUtil.readDomFromFile(TestUtil.SIGNED_ATOM_ENTRY);

        boolean isValid = SignatureUtil.verifySignature(element);
        assertTrue(isValid);
    }
}
