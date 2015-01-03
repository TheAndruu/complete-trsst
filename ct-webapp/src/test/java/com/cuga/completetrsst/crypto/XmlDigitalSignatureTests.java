package com.cuga.completetrsst.crypto;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rometools.rome.feed.atom.Entry;
import static org.junit.Assert.*;

public class XmlDigitalSignatureTests {

	@Test
	public void validate() throws Exception {
		Entry entry = ElementUtils.createEntry();
		Element element = ElementUtils.toElement(entry);
		// ElementUtils.logElement(element);

		attachSignature(element);

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
		Element element = ElementUtils.toElement(entry);
		// ElementUtils.logElement(element);

		attachSignature(element);

		ElementUtils.logElement(element);
	}

	protected static void attachSignature(Element element) throws Exception {
		// document builder for building the xml
		// Document doc = builder.parse(element);

		// key pair to use in signing
		KeyPair kp = generateInsecureKeyPair();
		DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), element);
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

		// try logging to see if it shows up
		ElementUtils.logElement(element);
	}

	protected static KeyPair generateInsecureKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		kpg.initialize(512);
		KeyPair kp = kpg.generateKeyPair();
		return kp;
	}

}
