package com.completetrsst.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.xml.TestUtils;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;

public class SignatureUtilTests {

	/** DOM implementation of toString equals expected */
	@Test
	public void domToStringEqualsExpected() throws Exception {
		Element entryElement = TestUtils.readDomFromFile(TestUtils.PLAIN_ATOM_ENTRY);
		String asXmlString = XmlUtil.toString(entryElement);
		String straightFromFile = TestUtils.readFile(TestUtils.PLAIN_ATOM_ENTRY);
		assertEquals(straightFromFile, asXmlString);
	}
	

	/** Asserts proper verification of attached enveloped XML Digital Signature */
	@Test
	public void validate() throws Exception {
		Entry entry = TestUtils.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);

		SignatureUtil.attachSignature(element);

		boolean result = SignatureUtil.verifySignature(element);
		assertTrue(result);
	}

	// *********************************************
	// ******************************************888
	// **** Below is how to add a signature to an XML node
	@Test
	public void test() throws Exception {
		Entry entry = TestUtils.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);
		// ElementUtils.logElement(element);

		SignatureUtil.attachSignature(element);

		XmlUtil.logDomElement(element);
	}
}
