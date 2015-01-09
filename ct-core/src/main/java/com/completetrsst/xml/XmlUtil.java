package com.completetrsst.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
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
    
    // TODO: Test me
    public static org.w3c.dom.Element toDom(String xmlString) throws IOException {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc;
        try {
        	DocumentBuilder builder = factory.newDocumentBuilder();
	        doc = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
	        log.error(e.getMessage(), e);
	        throw new IOException(e);
        }
        return doc.getDocumentElement();
    }

    private static String serializeJdom(org.jdom2.Element element) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter();
        StringWriter writer = new StringWriter();
        xmlOutputter.output(element, writer);
        String xmlString = writer.toString();
        writer.close();
        return xmlString;
    }

    public static String serializeDom(Element domElement) throws TransformerFactoryConfigurationError {
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
}
