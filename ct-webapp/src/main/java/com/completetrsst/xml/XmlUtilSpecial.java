package com.completetrsst.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

public class XmlUtilSpecial {

	 public static org.jdom2.Element toJdom(org.w3c.dom.Element e) {
		    DOMBuilder builder = new DOMBuilder();
		    org.jdom2.Element jdomElem = builder.build(e);
		    return jdomElem;
		  }
	
	public static org.w3c.dom.Document toDom(org.jdom2.Document doc) {
		try {
			XMLOutputter xmlOutputter = new XMLOutputter();
			StringWriter writer = new StringWriter();
			xmlOutputter.output(doc, writer);
			byte[] xmlBytes = writer.toString().getBytes();
			writer.close();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
		} catch (IOException e) {
			throw new RuntimeException("IO exception" + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Parser exception" + e.getMessage(), e);
		} catch (SAXException e) {
			throw new RuntimeException("Sax exception" + e.getMessage(), e);
		} 
	}

	public static org.w3c.dom.Element toDom(org.jdom2.Element element) {
		return toDom(element.getDocument()).getDocumentElement();
	}
}
