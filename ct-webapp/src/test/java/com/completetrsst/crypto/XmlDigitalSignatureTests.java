package com.completetrsst.crypto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;

public class XmlDigitalSignatureTests {

	@Test
	public void validate() throws Exception {
		Entry entry = TestUtil.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);
		// ElementUtils.logElement(element);

		SignatureUtil.attachSignature(element);

		boolean result = SignatureUtil.verifySignature(element);
		assertTrue(result);
	}


	// *********************************************
	// ******************************************888
	// **** Below is how to add a signature to an XML node
	@Test
	public void test() throws Exception {
		Entry entry = TestUtil.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);
		// ElementUtils.logElement(element);

		SignatureUtil.attachSignature(element);

		XmlUtil.logDomElement(element);
	}

	


}
