package com.cuga.completetrsst.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

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
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Element;

import com.rometools.rome.feed.atom.Entry;

public class XmlDigitalSignatureTests {

	@Test
	public void test() throws Exception {
		Entry entry = ElementUtils.createEntry();
		Element element = ElementUtils.toElement(entry);
//		ElementUtils.logElement(element);
		
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
