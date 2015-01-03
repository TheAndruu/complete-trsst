package com.completetrsst.xml;

import static com.completetrsst.xml.TestUtils.PLAIN_ATOM_ENTRY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.crypto.SignatureUtil;
import com.rometools.rome.feed.atom.Entry;
public class DomUtilTests {
	
	@Test
	public void xmlFromFileEqualsExpected() throws Exception {
		Element entryElement = TestUtils.readDomFromFile(PLAIN_ATOM_ENTRY);
		String asXmlString = XmlUtil.toString(entryElement);
		String straightFromFile = TestUtils.readFile(PLAIN_ATOM_ENTRY);
		assertEquals(straightFromFile, asXmlString);
	}

	
	
		@Test
	public void validate() throws Exception {
		Entry entry = TestUtils.createSimpleEntry();
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
		Entry entry = TestUtils.createSimpleEntry();
		Element element = XmlUtil.toW3cElement(entry);
		// ElementUtils.logElement(element);

		SignatureUtil.attachSignature(element);

		XmlUtil.logDomElement(element);
	}

}
