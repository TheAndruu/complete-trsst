package com.completetrsst.operations;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.model.Story;
import com.completetrsst.xml.XmlUtil;

public class InMemoryStoryOps implements StoryOperations {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStoryOps.class);

    private Map<String, List<Story>> publishersToStories = new HashMap<String, List<Story>>();

    @Override
    public void create(String publisherId, Story story) {
        List<Story> existingStories = getStories(publisherId);
        story.setId(createUniqueId());
        existingStories.add(story);
        sortByDateDescending(existingStories);

        publishersToStories.put(publisherId, existingStories);
    }

    private String createUniqueId() {
        return UUID.randomUUID().toString();
    }

    private void sortByDateDescending(List<Story> existingStories) {
        Collections.sort(existingStories, new Comparator<Story>() {
            @Override
            public int compare(Story story1, Story story2) {
                return story2.getDatePublished().compareTo(story1.getDatePublished());
            }
        });
    }

    @Override
    public List<Story> getStories(String publisherId) {
        List<Story> stories = publishersToStories.get(publisherId);
        return stories == null ? new ArrayList<Story>() : stories;
    }

    // TODO: Verify the signature from the passed-in input
    // that'd be a big success
    @Override
    public String publishEntry(String publisherId, EntryType entryElement) {
        // TODO: need DOM element, and then verify it
        log.info("Got to in memory ops!");
        
        String asXml;
        try {
        	asXml = getString(entryElement);
        } catch (Exception e) {
	        log.error(e.getMessage(), e);
	        asXml = e.getMessage();
        }
        
        return asXml;
//        try {
//            result = isValidSignature(entryElement);
//        } catch (Exception e1) {
//            log.error(e1.getMessage(), e1);
//            return "problem, see log";
//        }
//        log.info("result of validation: " + result);
//        return "is valid? " + result + " " + asXml;
    }

    private static Element createEntry(List<Object> authOrCatOrContent) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("entry");
        for (Object node : authOrCatOrContent) {
            if (node instanceof org.w3c.dom.Node) {
                rootElement.appendChild((Element)node);
            }
        }
        return rootElement;
    }
    

private static String getString(FeedType entryType) throws Exception {
        
        // TODO: i think marshallers are threadsafe and can be static variables
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        JAXBContext jaxbContext = JAXBContext.newInstance(FeedType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        
        ObjectFactory obj = new ObjectFactory();
        JAXBElement <FeedType> feed = obj.createFeed(entryType);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(feed, writer);
        writer.close();
        return writer.toString();
        
        
//        
//        logNodes(doc);
//        
//        Element docElement = (Element)doc.getDocumentElement().getChildNodes().item(0);
//        
//        return XmlUtil.serializeDom(docElement);
    }

private static void logNodes(Document doc) {
	NodeList nodes = doc.getDocumentElement().getChildNodes();
	for (int i = 0; i < nodes.getLength(); i++) {
		log.info("Node: " + nodes.item(i).getNodeName());
	}
}

    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "entry")
    public static class EntryHolder {
        protected EntryType entry;

        public EntryType getEntry() {
            return entry;
        }

        public void setEntry(EntryType entryType) {
            this.entry = entryType;
        }
    }
    
private static String getString(EntryType entryType) throws Exception {
        
        // TODO: i think marshallers are threadsafe and can be static variables
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        JAXBContext jaxbContext = JAXBContext.newInstance(EntryHolder.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        ObjectFactory obj = new ObjectFactory();
        JAXBElement <EntryType> feed = obj.createEntry(entryType);
        
        marshaller.marshal(feed, doc);
        
        Element docElement = doc.getDocumentElement();
        
        boolean result = SignatureUtil.verifySignature(docElement);
        log.info("Result of verify: " + result);
        
        return XmlUtil.serializeDom(docElement);
    }
    
    private static boolean isValidSignature(EntryType entryType) throws Exception {
        
        EntryHolder holder = new EntryHolder();
        holder.setEntry(entryType);
        
        // TODO: i think marshallers are threadsafe and can be static variables
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        JAXBContext jaxbContext = JAXBContext.newInstance(EntryHolder.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        
        marshaller.marshal(holder, doc );
        
        Element docElement = (Element)doc.getDocumentElement().getChildNodes().item(0);
        log.info("num children " + docElement.getChildNodes().getLength());
        
        log.info("As dom: \n" + XmlUtil.serializeDom(docElement));
        
        boolean isValid = SignatureUtil.verifySignature(docElement);
        log.info("Is valid? "+ isValid);
        return isValid;
        
    }
    
    // TODO: Convert to Dom Element:
    private static boolean isSignatureValid(EntryType entryElement) {
        Element element;
        try {
            element = createEntry(entryElement.getAuthorOrCategoryOrContent());
        } catch (ParserConfigurationException e1) {
            log.error(e1.getMessage(), e1);
            return false;
        }

        boolean isValid;
        try {
            isValid = SignatureUtil.verifySignature(element);
        } catch (XMLSignatureException e) {
            log.error("Unable to read signature", e);
            return false;
        }
        log.info("Is valid? " + isValid);
        return isValid;
    }

    private static String marshalElement(EntryType entryElement) throws JAXBException, PropertyException, IOException {

        JAXBContext jaxbContext = JAXBContext.newInstance(EntryType.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        QName qName = new QName("entry");
        JAXBElement<EntryType> root = new JAXBElement<EntryType>(qName, EntryType.class, entryElement);
        StringWriter writer = new StringWriter();
        jaxbMarshaller.marshal(root, writer);
        writer.close();
        return writer.toString();
    }
}
