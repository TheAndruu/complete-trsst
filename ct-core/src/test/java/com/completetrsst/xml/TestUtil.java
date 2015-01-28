package com.completetrsst.xml;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestUtil {

    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

    public static final String PLAIN_ATOM_ENTRY;
    public static final String SIGNED_ATOM_ENTRY;

    public static final String FEED_VALID_ENTRY_VALID;
    public static final String FEED_VALID_ENTRY_TAMPERED;
    public static final String FEED_TAMPERED_ENTRY_VALID;
    public static final String FEED_TAMPERED_ENTRY_TAMPERED;
    public static final String FEED_TAMPERED_ENTRY_VALID_FROM_OTHER_FEED;

    private static final String sep = File.separator;
    static {
        PLAIN_ATOM_ENTRY = TestUtil.class.getResource("plainAtomEntry.xml").getPath();
        SIGNED_ATOM_ENTRY = TestUtil.class.getResource("signedAtomEntry.xml").getPath();

        FEED_VALID_ENTRY_VALID = TestUtil.class.getResource("feedValidEntryValid.xml").getPath();
        FEED_VALID_ENTRY_TAMPERED = TestUtil.class.getResource("tampered" + sep + "feedValidEntryTampered.xml").getPath();
        FEED_TAMPERED_ENTRY_VALID = TestUtil.class.getResource("tampered" + sep + "feedTamperedEntryValid.xml").getPath();
        FEED_TAMPERED_ENTRY_TAMPERED = TestUtil.class.getResource("tampered" + sep + "feedTamperedEntryTampered.xml").getPath();
        FEED_TAMPERED_ENTRY_VALID_FROM_OTHER_FEED = TestUtil.class.getResource("tampered" + sep + "feedTamperedEntryValidFromOtherFeed.xml")
                .getPath();
    }

    public static String readFile(String path) throws IOException {
        return new String(readAllBytes(get(path)));
    }

    public static org.jdom2.Element readJDomFromFile(String filePath) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document doc = builder.build(filePath);
        return doc.getRootElement();
    }

    public static Element readDomFromFile(String filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(filePath);
        return doc.getDocumentElement();
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

    public static void logFormatted(String prefix, Element domElement) {
        log.info(prefix + format(XmlUtil.serializeDom(domElement)));
    }

    public static String format(String xml) {
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
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
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static org.jdom2.Element parseToJdom(String rawXml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document doc = builder.build(new StringReader(rawXml));
        return doc.getRootElement();
    }

    /**
     * Putting this in TestUtil and not XmlUtil since any production code could probably benefit by doing other work while iterating over NodeList
     * directly, as opposed to looping once to construct this list we return, then iterating over it again to do such work.
     */
    public static List<Node> getElementsByTagName(Element node, String xmlns, String nodeName) {
        List<Node> nodes = new ArrayList<Node>();
        NodeList nodeList = node.getElementsByTagNameNS(xmlns, nodeName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

}
