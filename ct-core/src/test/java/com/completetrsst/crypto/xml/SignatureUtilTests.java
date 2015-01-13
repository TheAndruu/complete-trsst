package com.completetrsst.crypto.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class SignatureUtilTests {

    private static final Logger log = LoggerFactory.getLogger(SignatureUtilTests.class);

    // Can later have this load a key from file in TestUtil, if desired
    private KeyCreator keyCreator;

    @Before
    public void init() {
        keyCreator = new EllipticCurveKeyCreator();
    }

    @Test
    public void signElement() throws Exception {
        org.jdom2.Element element = TestUtil.readJDomFromFile(TestUtil.PLAIN_ATOM_ENTRY);

        SignatureUtil.signElement(element, keyCreator.createKeyPair());

        // now convert element to DOM and verify
        Element signedAsDom = XmlUtil.toDom(element);

        boolean isValid = SignatureUtil.verifySignature(signedAsDom);
        assertTrue(isValid);
    }

    /** Asserts proper verification of attached enveloped XML Digital Signature */
    @Test
    public void attachSignature() throws Exception {
        Element element = TestUtil.readDomFromFile(TestUtil.PLAIN_ATOM_ENTRY);

        SignatureUtil.signElement(element, keyCreator.createKeyPair());

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

        SignatureUtil.signElement(element, keyCreator.createKeyPair());

        // Convert to jdom so we can edit it easier
        org.jdom2.Element asJdom = XmlUtil.toJdom(element);
        org.jdom2.Element anything = new org.jdom2.Element("tamper");
        anything.setText("doesn't belong");
        asJdom.addContent(anything);
        // back to w3 dom
        element = XmlUtil.toDom(asJdom);

        boolean result = SignatureUtil.verifySignature(element);
        assertFalse(result);
    }

    /** Reads an already-signed element and verifies its signature, with the read-as-JDOM method */
    @Test
    public void verifyStoredSignedXmlReadAsJdom() throws Exception {
        org.jdom2.Element element = TestUtil.readJDomFromFile(TestUtil.SIGNED_ATOM_ENTRY);

        // now convert element to DOM and verify
        Element signedAsDom = XmlUtil.toDom(element);

        boolean isValid = SignatureUtil.verifySignature(signedAsDom);
        assertTrue(isValid);
    }
    
    /** Reads an already-signed element and verifies its signature, with the read-as-JDOM method */
    @Test
    public void verifyStoredTamperedAsJDomXmlFails() throws Exception {
        org.jdom2.Element element = TestUtil.readJDomFromFile(TestUtil.TAMPERED_ATOM_ENTRY);

        // now convert element to DOM and verify
        Element signedAsDom = XmlUtil.toDom(element);

        boolean isValid = SignatureUtil.verifySignature(signedAsDom);
        assertFalse(isValid);
    }
    
    /** Reads an already-signed element and verifies its signature, with the read-as-DOM method */
    @Test
    public void verifyStoredTamperedAsDomXmlFails() throws Exception {
        org.w3c.dom.Element element = TestUtil.readDomFromFile(TestUtil.TAMPERED_ATOM_ENTRY);

        boolean isValid = SignatureUtil.verifySignature(element);
        assertFalse(isValid);
    }
    
    /** Reads an already-signed element and verifies its signature, with the read-as-JDOM method */
    @Test
    public void verifyStoredSignedXmlReadAsDom() throws Exception {
        org.w3c.dom.Element element = TestUtil.readDomFromFile(TestUtil.SIGNED_ATOM_ENTRY);

        boolean isValid = SignatureUtil.verifySignature(element);
        assertTrue(isValid);
    }
}
