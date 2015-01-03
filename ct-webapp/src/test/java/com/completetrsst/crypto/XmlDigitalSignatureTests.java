package com.completetrsst.crypto;

import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.ElementUtils;
import com.rometools.rome.feed.atom.Entry;

public class XmlDigitalSignatureTests {

	@Test
	public void validate() throws Exception {
		Entry entry = ElementUtils.createEntry();
		Element element = ElementUtils.toW3cElement(entry);
		// ElementUtils.logElement(element);

		ElementUtils.attachSignature(element);

		boolean result = verifySignature(element);
		assertTrue(result);
	}

	private boolean verifySignature(Element element) throws Exception {
		Document doc = element.getOwnerDocument();

		// specify the signature to validate
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		if (nl.getLength() == 0) {
			throw new Exception("Cannot find Signature element");
		}

		// context for validation -- incl selector to extract key from the XML
		DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));

		// grab the signature from the document
		XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = factory.unmarshalXMLSignature(valContext);

		// and finally validate:
		boolean coreValidity = signature.validate(valContext);

		// did the signature fail to cryptographically validate?
		boolean sv = signature.getSignatureValue().validate(valContext);
		System.out.println("signature validation status: " + sv);

		// did any of the elements fail to validate?
		Iterator i = signature.getSignedInfo().getReferences().iterator();
		for (int j = 0; i.hasNext(); j++) {
			boolean refValid = ((Reference) i.next()).validate(valContext);
			System.out.println("ref[" + j + "] validity status: " + refValid);
		}

		return coreValidity;

		// Document doc = builder.parse(new FileInputStream(argv[0]));
	}

	private static class SimpleKeySelectorResult implements KeySelectorResult {
		private Key key;

		public SimpleKeySelectorResult(Key key) {
			this.key = key;
		}

		@Override
		public Key getKey() {
			return key;
		}

	}

	private static class KeyValueKeySelector extends KeySelector {

		public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
		        XMLCryptoContext context) throws KeySelectorException {

			if (keyInfo == null) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			List list = keyInfo.getContent();

			for (int i = 0; i < list.size(); i++) {
				XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof KeyValue) {
					PublicKey pk = null;
					try {
						pk = ((KeyValue) xmlStructure).getPublicKey();
					} catch (KeyException ke) {
						throw new KeySelectorException(ke);
					}
					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
						return new SimpleKeySelectorResult(pk);
					}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		static boolean algEquals(String algURI, String algName) {
			if (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
				return true;
			} else if (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
				return true;
			} else {
				return false;
			}
		}
	}

	// *********************************************
	// ******************************************888
	// **** Below is how to add a signature to an XML node
	@Test
	public void test() throws Exception {
		Entry entry = ElementUtils.createEntry();
		Element element = ElementUtils.toW3cElement(entry);
		// ElementUtils.logElement(element);

		ElementUtils.attachSignature(element);

		ElementUtils.logDomElement(element);
	}

	


}
