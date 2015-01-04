package com.completetrsst.xml.crypto;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
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

import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.xml.XmlUtil;

public class SignatureUtil {
	private final static Logger log = LoggerFactory.getLogger(SignatureUtil.class);

	/**
	 * Signs a JDOM Element, such as one containing a single Atom Entry.
	 * Parameter will be updated to include XML Digital Signature on the object.
	 */
	public static void signElement(org.jdom2.Element jdomElement) {
		org.w3c.dom.Element signedDomElement = null;
		try {
			signedDomElement = XmlUtil.toDom(jdomElement);
		} catch (IOException e1) {
			log.error(e1.getMessage());
			throw new RuntimeException(e1);
		}

		try {
			SignatureUtil.attachSignature(signedDomElement);
		} catch (Exception e) {
			log.error(e.getMessage());
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
	static void attachSignature(Element domElement) throws Exception {
		// document builder for building the xml
		// Document doc = builder.parse(element);

		// key pair to use in signing
		// TODO: Replace this with a keyPair being passed in, move
		// insecureKeyPair to tests
		KeyPair kp = generateInsecureKeyPair();
		DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), domElement);
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// reference indicates which xml node will be signed
		Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
		        Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (XMLStructure) null)), null, null);

		// The object that'll actually be signed (contains the reference above)
		SignedInfo si = fac.newSignedInfo(
		        fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (XMLStructure) null),
		        fac.newSignatureMethod(SignatureMethod.DSA_SHA1, null), Collections.singletonList(ref));

		// generate the key info, which will put the public key in the xml so
		// ppl can decrypt it
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(kp.getPublic());
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

		// actually sign the xml
		XMLSignature signature = fac.newXMLSignature(si, ki);
		signature.sign(dsc);
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

	protected static XMLSignature extractSignature(DOMValidateContext valContext) throws XMLSignatureException {
		XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature;
		try {
			signature = factory.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			throw new XMLSignatureException("Could not unmarshall XML Signature", e);
		}
		return signature;
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

	// TODO: Replace this with parameters
	private static KeyPair generateInsecureKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		kpg.initialize(512);
		KeyPair kp = kpg.generateKeyPair();
		return kp;
	}

}
