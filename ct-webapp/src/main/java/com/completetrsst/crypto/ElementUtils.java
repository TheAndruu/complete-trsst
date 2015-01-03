package com.completetrsst.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
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

public class ElementUtils {
	private final static Logger log = LoggerFactory.getLogger(ElementUtils.class);

	public static void attachSignature(Element element) throws Exception {
		// document builder for building the xml
		// Document doc = builder.parse(element);

		// key pair to use in signing
		KeyPair kp = generateInsecureKeyPair();
		DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), element);
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// reference indicates which xml node will be signed
		Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
		        Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (XMLStructure) null)), null, null);

		// The object that'll actually be signed (contains the reference above)
		SignedInfo si = fac.newSignedInfo(
		        fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (XMLStructure) null),
		        fac.newSignatureMethod(SignatureMethod.DSA_SHA1, null), Collections.singletonList(ref));

		// generate the key info, which will put the public key in the xml so
		// ppl can decrypt it
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(kp.getPublic());
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

		// actually sign the xml
		XMLSignature signature = fac.newXMLSignature(si, ki);
		signature.sign(dsc);
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

	public static org.w3c.dom.Element toDom(org.jdom2.Element element) {
		return toDom(element.getDocument()).getDocumentElement();
	}

	public static org.w3c.dom.Element convertToDOM(org.jdom2.Element jdomElement) throws JDOMException {
		DOMOutputter outputter = new DOMOutputter();
		org.w3c.dom.Element element = outputter.output(jdomElement);
		ElementUtils.logDomElement(element);
		return element;
	}

	protected static KeyPair generateInsecureKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
		kpg.initialize(512);
		KeyPair kp = kpg.generateKeyPair();
		return kp;
	}

	public static Entry createEntry() {
		Entry entry = new Entry();
		entry.setId(UUID.randomUUID().toString());
		entry.setTitle("Title: create entry for element conversion test");
		entry.setUpdated(new Date());
		entry.setPublished(new Date());

		// entry.setModules(createModules());
		return entry;
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

	public static Element toW3cElement(Entry entry) throws Exception {
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

	public static void logJdomElement(org.jdom2.Element node) {
		StringWriter writer = new StringWriter();
		final XMLOutputter outputter = new XMLOutputter();
		try {
			outputter.output(node, writer);
			writer.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		logXml(format(writer.toString()));
	}

	public static void logDomElement(Element node) {
		// Document document = node.getOwnerDocument();
		// DOMImplementationLS domImplLS = (DOMImplementationLS) document
		// .getImplementation();
		//
		// LSSerializer serializer = domImplLS.createLSSerializer();
		// serializer.getDomConfig().setParameter("xml-declaration", false);
		// String str = serializer.writeToString(node);
		StringWriter buffer = null;
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(buffer));
			buffer.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String str = buffer.toString();
		logXml(format(str));
	}

	public static void printEntry(Entry entry) throws Exception {
		StringWriter writer = new StringWriter();
		Atom10Generator.serializeEntry(entry, writer);

		writer.close();
		String xml = writer.toString();

		logXml(xml);
	}

	// "This was giving trouble with doubel namespaces")
	public static void logXml(String unformattedXml) {
		log.info("\n" + unformattedXml);
	}

	// was this giving trouble or was the xml it was being given bad?
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
