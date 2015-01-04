package com.completetrsst.crypto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;

public class SignatureUtilTests {

	private static final Logger log = LoggerFactory.getLogger(SignatureUtilTests.class);
	
	@Test
	public void signElement() throws Exception {
		Entry entry = TestUtil.createSimpleEntry();
		org.jdom2.Element element = TestUtil.toJdom(entry);

		SignatureUtil.signElement(element);

		// now convert element to DOM and verify
		Element signedAsDom = XmlUtil.toDom(element);

		log.info("Signed dom");
		log.info("\n" + TestUtil.format(TestUtil.serialize(element)));
		
		boolean isValid = SignatureUtil.verifySignature(signedAsDom);
		assertTrue(isValid);
	}

	/** Asserts proper verification of attached enveloped XML Digital Signature */
	@Test
	public void attachSignature() throws Exception {
		Entry entry = TestUtil.createSimpleEntry();
		Element element = TestUtil.toDom(entry);

		SignatureUtil.attachSignature(element);

		boolean result = SignatureUtil.verifySignature(element);
		assertTrue(result);
	}

}
