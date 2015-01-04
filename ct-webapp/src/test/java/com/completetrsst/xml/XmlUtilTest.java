package com.completetrsst.xml;

import static com.completetrsst.xml.TestUtil.PLAIN_ATOM_ENTRY;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Element;

public class XmlUtilTest {

	/** Verifies DOM element read from a file matches expected */
	@Test
	public void domXmlElementFromFileEqualsExpected() throws Exception {
		Element entryElement = TestUtil.readDomFromFile(PLAIN_ATOM_ENTRY);
		String asXmlString = TestUtil.serialize(entryElement);
		String straightFromFile = TestUtil.readFile(PLAIN_ATOM_ENTRY);
		assertEquals(straightFromFile, asXmlString);
	}

}
