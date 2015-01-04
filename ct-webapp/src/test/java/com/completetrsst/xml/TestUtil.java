package com.completetrsst.xml;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

public class TestUtil {

	private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

	public static final String PLAIN_ATOM_ENTRY;
	static {
		PLAIN_ATOM_ENTRY = TestUtil.class.getResource("plainAtomEntry.xml").getPath();
	}

	public static String readFile(String path) throws IOException {
		return new String(readAllBytes(get(path)));
	}

	public static org.jdom2.Element readJDomFromFile(String filePath) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document doc = builder.build(filePath);
		return doc.getRootElement();
	}

	public static Element readDomFromFile(String filePath) throws ParserConfigurationException, SAXException,
	        IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document doc = builder.parse(filePath);
		Element entryElement = (Element) doc.getDocumentElement();
		return entryElement;
	}

	public static Entry createSimpleEntry() {
		Entry entry = new Entry();
		entry.setId(UUID.randomUUID().toString());
		entry.setTitle("Title: create entry for element conversion test");
		entry.setUpdated(new Date());
		entry.setPublished(new Date());
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

	static String serialize(Element domElement) throws TransformerFactoryConfigurationError {
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

}
