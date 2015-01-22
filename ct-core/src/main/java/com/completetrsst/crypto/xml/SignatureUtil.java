package com.completetrsst.crypto.xml;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.atom.AtomSigner;

public class SignatureUtil {
	private final static Logger log = LoggerFactory.getLogger(SignatureUtil.class);

	static final String ECDSA_SHA1 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1";

	static {
        org.apache.xml.security.Init.init();
    }
	
	/**
	 * Attaches a signature to the given DOM element, in place.
	 * 
	 * This should be the favored mechanism, as the XML Digital Signature APIs
	 * operate on DOM elements, thusly it'd be more efficient than the JDOM
	 * variant, which uses this anyway and eliminates the need for converting
	 * between types
	 * */
	public static void signElement(Element domElement, KeyPair keyPair) throws XMLSignatureException {
		// key pair to use in signing
		DOMSignContext dsc = new DOMSignContext(keyPair.getPrivate(), domElement);
		XMLSignatureFactory fac = DOMXMLSignatureFactory.getInstance("DOM", new XMLDSigRI());
		// reference indicates which xml node will be signed
		Reference ref;
		try {
			List<Transform> transforms = new ArrayList<Transform>(3);
			Map<String, String> namespaces = new HashMap<String, String>(1);
			namespaces.put("atom", AtomSigner.XMLNS);
			transforms.add(fac.newTransform(DOMTransform.ENVELOPED, (XMLStructure) null));
			ref = fac.newReference("", fac.newDigestMethod(DOMDigestMethod.SHA1, null), transforms, null, null);

		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			log.debug("Problem constructing XML reference to sign", e);
			throw new XMLSignatureException(e);
		}

		// The object that'll actually be signed (contains the reference above)
		SignedInfo si;
		try {
			si = fac.newSignedInfo(fac.newCanonicalizationMethod(DOMCanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
			        (XMLStructure) null), fac.newSignatureMethod(ECDSA_SHA1, null), Collections.singletonList(ref));
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			log.error("Problem configuring signature information", e);
			throw new XMLSignatureException(e);
		}

		// generate the key info, which will put the public key in the xml so
		// ppl can decrypt it
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		PublicKey publicKey = keyPair.getPublic();

		KeyValue kv;
		try {
			kv = kif.newKeyValue(publicKey);
		} catch (KeyException e) {
			log.debug("Problem constructing KeyInfo XML from public key", e);
			throw new XMLSignatureException(e);
		}
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

		// actually sign the xml
		XMLSignature signature = fac.newXMLSignature(si, ki);
		try {
			signature.sign(dsc);
		} catch (MarshalException | XMLSignatureException e) {
			log.debug("Problem signing xml with signature", e);
			throw new XMLSignatureException(e);
		}
	}

	public static XMLSignature extractSignature(DOMValidateContext valContext) throws XMLSignatureException {
		XMLSignatureFactory factory = DOMXMLSignatureFactory.getInstance("DOM", new XMLDSigRI());
		XMLSignature signature;
		try {
			signature = factory.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			log.debug("Could not unmarshall XML Signature");
			throw new XMLSignatureException("Could not unmarshall XML Signature");
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
		log.info("About to iterate over nodes");
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
	public static DOMValidateContext extractValidationContext(Element element) throws XMLSignatureException {
		NodeList nl = element.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		if (nl.getLength() == 0) {
			log.debug("Could not find XML signature element");
			throw new XMLSignatureException("Could not find XML Signature element");
		}

		log.debug("Number of signatures found: " + nl.getLength());

		// context for validation -- incl selector to extract key from the XML
		return new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));
	}

}
