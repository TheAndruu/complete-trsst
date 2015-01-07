package com.completetrsst.crypto.xml;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.apache.jcp.xml.dsig.internal.dom.DOMCanonicalizationMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMDigestMethod;
import org.apache.jcp.xml.dsig.internal.dom.DOMTransform;
import org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory;
import org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.xml.XmlUtil;

public class SignatureUtil {
    private final static Logger log = LoggerFactory.getLogger(SignatureUtil.class);

    static final String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";

    /**
     * Signs a JDOM Element, such as one containing a single Atom Entry.
     * Parameter will be updated to include XML Digital Signature on the object.
     */
    public static void signElement(org.jdom2.Element jdomElement, KeyPair keyPair) {
        org.w3c.dom.Element signedDomElement = null;
        try {
            signedDomElement = XmlUtil.toDom(jdomElement);
        } catch (IOException e1) {
            log.error("Error signing element: " + e1.getMessage());
            throw new RuntimeException(e1);
        }

        try {
            SignatureUtil.attachSignature(signedDomElement, keyPair);
        } catch (Exception e) {
            log.error("Error attaching signature: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        org.jdom2.Element newJdomWithSignature = XmlUtil.toJdom(signedDomElement);

        // grab the signature element:
        org.jdom2.Element signatureElement = newJdomWithSignature.getChild("Signature",
                Namespace.getNamespace(XMLSignature.XMLNS));

        // attach to original jdom element
        signatureElement.detach();
        jdomElement.addContent(signatureElement);
    }

    /** Attaches a signature to the given DOM element */
    static void attachSignature(Element domElement, KeyPair keyPair) {
        // key pair to use in signing
        DOMSignContext dsc = new DOMSignContext(keyPair.getPrivate(), domElement);
        XMLSignatureFactory fac = DOMXMLSignatureFactory.getInstance("DOM", new XMLDSigRI());
        // reference indicates which xml node will be signed
        Reference ref;
        try {
            ref = fac.newReference("", fac.newDigestMethod(DOMDigestMethod.SHA1, null),
                    Collections.singletonList(fac.newTransform(DOMTransform.ENVELOPED, (XMLStructure) null)), null,
                    null);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.error("Problem constructing XML reference to sign", e);
            throw new RuntimeException(e);
        }

        // The object that'll actually be signed (contains the reference above)
        SignedInfo si;
        try {
            si = fac.newSignedInfo(fac.newCanonicalizationMethod(DOMCanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                    (XMLStructure) null), fac.newSignatureMethod(ECDSA_SHA1, null), Collections.singletonList(ref));
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            log.error("Problem configuring signature information", e);
            throw new RuntimeException(e);
        }

        // generate the key info, which will put the public key in the xml so
        // ppl can decrypt it
        log.info(fac.getMechanismType());
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        PublicKey publicKey = keyPair.getPublic();

        // prints key algo: ECDSA
        log.info("key algo: " + publicKey.getAlgorithm());

        KeyValue kv;
        try {
            kv = kif.newKeyValue(publicKey);
        } catch (KeyException e) {
            log.error("Problem constructing KeyInfo XML from public key", e);
            throw new RuntimeException(e);
        }
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        // actually sign the xml
        XMLSignature signature = fac.newXMLSignature(si, ki);
        try {
            signature.sign(dsc);
        } catch (MarshalException | XMLSignatureException e) {
            log.error("Problem signing xml with signature", e);
            throw new RuntimeException(e);
        }
    }

    static XMLSignature extractSignature(DOMValidateContext valContext) throws XMLSignatureException {
        XMLSignatureFactory factory = DOMXMLSignatureFactory.getInstance("DOM", new XMLDSigRI());
        XMLSignature signature;
        try {
            signature = factory.unmarshalXMLSignature(valContext);
        } catch (MarshalException e) {
            throw new XMLSignatureException("Could not unmarshall XML Signature", e);
        }
        return signature;
    }

    /** Verifies the XML digital signature on a DOM element */
    public static boolean verifySignature(Element domElement) throws XMLSignatureException {
        DOMValidateContext valContext = extractValidationContext(domElement);
        // grab the signature from the document
        XMLSignature signature = extractSignature(valContext);

        // validation on the referenced nodes
        return signature.validate(valContext);
    }

    /**
     * Determines whether any referenced elements in the XML document are
     * invalid.
     * 
     * @return True if all the elements are valid. False if any are invalid.
     *         Logs the index of any invalid elements
     */
    @SuppressWarnings("rawtypes")
    static void areElementsValid(DOMValidateContext valContext, XMLSignature signature) throws XMLSignatureException {
        Iterator i = signature.getSignedInfo().getReferences().iterator();
        for (int j = 0; i.hasNext(); j++) {
            boolean refValid = ((Reference) i.next()).validate(valContext);
            log.info("ref[" + j + "] validity status: " + refValid);
        }
    }

    /**
     * Determines where a signature is cryptologically valid -- i.e. is the
     * signature good
     */
    static boolean isCryptologicallyValid(DOMValidateContext valContext, XMLSignature signature)
            throws XMLSignatureException {
        return signature.getSignatureValue().validate(valContext);
    }

    /**
     * Gets the validation context from an element, necessary for extracting the
     * signature
     */
    static DOMValidateContext extractValidationContext(Element element) throws XMLSignatureException {
        Document doc = element.getOwnerDocument();

        // specify the signature to validate
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new XMLSignatureException("Could not find XML Signature element");
        }

        // context for validation -- incl selector to extract key from the XML
        return new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));
    }

}
