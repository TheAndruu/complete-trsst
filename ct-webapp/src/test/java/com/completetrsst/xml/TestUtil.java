package com.completetrsst.xml;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.rometools.rome.feed.atom.Entry;

public class TestUtil {

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
}
