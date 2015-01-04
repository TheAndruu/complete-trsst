package com.completetrsst.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class XmlUtil {

	private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);

	private XmlUtil() {
		// utility class
	}

	public static org.jdom2.Element toJdom(org.w3c.dom.Element e) {
		DOMBuilder builder = new DOMBuilder();
		org.jdom2.Element jdomElem = builder.build(e);
		return jdomElem;
	}

	public static org.w3c.dom.Element toDom(org.jdom2.Element element) throws IOException {
		return toDom(element.getDocument()).getDocumentElement();
	}

	private static org.w3c.dom.Document toDom(org.jdom2.Document doc) throws IOException {
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
			String message = "IO exception" + e.getMessage();
			log.error(message);
			throw new IOException(message, e);
		} catch (ParserConfigurationException e) {
			String message = "Parser exception" + e.getMessage();
			log.error(message);
			throw new IOException(message, e);
		} catch (SAXException e) {
			String message = "Sax exception" + e.getMessage();
			log.error(message);
			throw new IOException(message, e);
		}
	}

}
