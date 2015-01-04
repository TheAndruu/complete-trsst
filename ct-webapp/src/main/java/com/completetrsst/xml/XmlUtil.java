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

import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.DOMOutputter;
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

	public static org.w3c.dom.Document toDom(org.jdom2.Document doc) throws RuntimeException {
		try {
			XMLOutputter xmlOutputter = new XMLOutputter();
			StringWriter elemStrWriter = new StringWriter();
			xmlOutputter.output(doc, elemStrWriter);
			byte[] xmlBytes = elemStrWriter.toString().getBytes();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	public static org.w3c.dom.Element toDom(org.jdom2.Element jdomElement) {
		return toDom(jdomElement.getDocument()).getDocumentElement();
	}

	public static org.w3c.dom.Element convertToDOM(org.jdom2.Element jdomElement) throws JDOMException {
		DOMOutputter outputter = new DOMOutputter();
		org.w3c.dom.Element element = outputter.output(jdomElement);
		return element;
	}

	// keep this?
	public static org.jdom2.Element toJdom(org.w3c.dom.Element e) {
		DOMBuilder builder = new DOMBuilder();
		org.jdom2.Element jdomElem = builder.build(e);
		return jdomElem;
	}

	private static List<Module> createModules() {
		SampleModule module = new SampleModuleImpl();
		Foo foo = new Foo();
		Bar bar = new Bar();
		bar.setItem("bar item");
		foo.setBar(bar);
		module.setFoo(foo);
		List<Module> modules = new ArrayList<Module>();
		modules.add(module);
		return modules;
	}
	
	public static org.jdom2.Element toJdom(Entry entry) throws Exception {
		// Note: This is from Atom10Generator.serializeEntry()
		// it also has .generate(WireFeed) which returns a jdom2 element

		// Build a feed containing only the entry
		final List<Entry> entries = new ArrayList<Entry>();
		entries.add(entry);
		final Feed feed1 = new Feed();
		feed1.setFeedType("atom_1.0");
		feed1.setEntries(entries);

		// Get Rome to output feed as a JDOM document
		final WireFeedOutput wireFeedOutput = new WireFeedOutput();
		org.jdom2.Document feedDoc = wireFeedOutput.outputJDom(feed1);

        // Grab entry element from feed and get JDOM to serialize it
        final org.jdom2.Element entryElement = feedDoc.getRootElement().getChildren().get(0);
		return entryElement;
	}

	public static Element toDom(Entry entry) throws Exception {
		// Note: This is from Atom10Generator.serializeEntry()
		// it also has .generate(WireFeed) which returns a jdom2 element

		// Build a feed containing only the entry
		final List<Entry> entries = new ArrayList<Entry>();
		entries.add(entry);
		final Feed feed1 = new Feed();
		feed1.setFeedType("atom_1.0");
		feed1.setEntries(entries);

		// Get Rome to output feed as a JDOM document
		final WireFeedOutput wireFeedOutput = new WireFeedOutput();
		Document feedDoc = wireFeedOutput.outputW3CDom(feed1);
		// org.jdom2.
		// Grab the entry element from the feed
		Node node = feedDoc.getDocumentElement().getFirstChild();
		return (Element) node;
	}

	public static String serialize(org.jdom2.Element jdomElement) {
		StringWriter writer = new StringWriter();
		final XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(jdomElement, writer);
			writer.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return writer.toString();
	}


	public static String serialize(Element domElement) throws TransformerFactoryConfigurationError {
		StringWriter buffer = null;
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(domElement), new StreamResult(buffer));
			buffer.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return buffer.toString();
	}

	private static String serialize(Entry entry) throws Exception {
		StringWriter writer = new StringWriter();
		Atom10Generator.serializeEntry(entry, writer);
		writer.close();
		return writer.toString();
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
