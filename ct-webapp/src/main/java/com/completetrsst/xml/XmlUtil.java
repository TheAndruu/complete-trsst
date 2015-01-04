package com.completetrsst.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.completetrsst.rome.Bar;
import com.completetrsst.rome.Foo;
import com.completetrsst.rome.SampleModule;
import com.completetrsst.rome.SampleModuleImpl;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

public class XmlUtil {

	private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);

	private XmlUtil() {
		// utility class
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
			throw new IOException("IO exception" + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new IOException("Parser exception" + e.getMessage(), e);
		} catch (SAXException e) {
			throw new IOException("Sax exception" + e.getMessage(), e);
		}
	}

	public static org.w3c.dom.Element toDom(org.jdom2.Element element) throws IOException {
		return toDom(element.getDocument()).getDocumentElement();
	}

	public static org.jdom2.Element toJdom(org.w3c.dom.Element e) {
		DOMBuilder builder = new DOMBuilder();
		org.jdom2.Element jdomElem = builder.build(e);
		return jdomElem;
	}

	public static String format(String xml) {
		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
			        .getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

			// May need this:
			// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			// Set this to true if the output needs to be beautified.
			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);

			// Set this to true if the declaration is needed to be outputted.
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
