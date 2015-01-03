package com.completetrsst.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;

public class SignatureUtilTests {

	

	/** Asserts proper verification of attached enveloped XML Digital Signature */
	@Test
	public void validateSignatureAttachedToDomElement() throws Exception {
		Entry entry = TestUtil.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);

		SignatureUtil.attachSignature(element);

		boolean result = SignatureUtil.verifySignature(element);
		assertTrue(result);
	}

}
