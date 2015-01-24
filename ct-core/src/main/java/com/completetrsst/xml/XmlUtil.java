package com.completetrsst.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
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
        try {
            String xmlString = serializeJdom(element);

            byte[] xmlBytes = xmlString.getBytes();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document domDoc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
            return domDoc.getDocumentElement();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            String message = "Exception" + e.getMessage();
            log.debug(message);
            throw new IOException(message, e);
        }
    }

    public static org.w3c.dom.Element toDom(String xmlString) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.debug(e.getMessage());
            throw new IOException(e);
        }
        return doc.getDocumentElement();
    }

    public static String serializeJdom(org.jdom2.Element element) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter();
        StringWriter writer = new StringWriter();
        xmlOutputter.output(element, writer);
        String xmlString = writer.toString();
        writer.close();
        return xmlString;
    }

    public static String serializeDom(Node domElement) {
        Document document = domElement.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        // by default its true, so set it to omit printing xml-declaration
        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer.writeToString(domElement);
    }

}
