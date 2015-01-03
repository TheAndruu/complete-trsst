package com.completetrsst.xml;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DomUtil {

	public static Element readDomFromFile(String filePath) throws ParserConfigurationException, SAXException,
	        IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document doc = builder.parse(filePath);
		Element entryElement = (Element) doc.getDocumentElement();
		return entryElement;
	}

}
