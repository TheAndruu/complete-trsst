package com.cuga.completetrsst.crypto;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.completetrsst.rome.Bar;
import com.completetrsst.rome.Foo;
import com.completetrsst.rome.SampleModule;
import com.completetrsst.rome.SampleModuleImpl;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

public class ElementUtils {
	private final static Logger log = LoggerFactory
			.getLogger(ElementUtils.class);

	public static Entry createEntry() {
		Entry entry = new Entry();
		entry.setId(UUID.randomUUID().toString());
		entry.setTitle("Title: create entry for element conversion test");
		entry.setUpdated(new Date());
		entry.setPublished(new Date());

		// entry.setModules(createModules());
		return entry;
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

	public static Element toElement(Entry entry) throws Exception {
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

	public static void logElement(Element node) {
		Document document = node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document
				.getImplementation();

		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false);
		String str = serializer.writeToString(node);
		log.info("Unformatted:\n" + str);
		logXml(str);
	}

	public static void printEntry(Entry entry) throws Exception {
		StringWriter writer = new StringWriter();
		Atom10Generator.serializeEntry(entry, writer);

		writer.close();
		String xml = writer.toString();

		logXml(xml);
	}

	public static void logXml(String unformattedXml) {
		log.info("\n" + format(unformattedXml));
	}

	public static String format(String xml) {

		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml
					.startsWith("<?xml"));

			// May need this:
			// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

			final DOMImplementationRegistry registry = DOMImplementationRegistry
					.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry
					.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			// Set this to true if the output needs to be beautified.
			writer.getDomConfig().setParameter("format-pretty-print",
					Boolean.TRUE);

			// Set this to true if the declaration is needed to be outputted.
			writer.getDomConfig().setParameter("xml-declaration",
					keepDeclaration);

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
