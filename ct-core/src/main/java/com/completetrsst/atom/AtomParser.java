package com.completetrsst.atom;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Namespaces.SIGNATURE_XMLNS;
import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;
import static com.completetrsst.constants.Nodes.ATOM_ENTRY;
import static com.completetrsst.constants.Nodes.ATOM_TITLE;
import static com.completetrsst.constants.Nodes.SIGNATURE_VALUE;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.crypto.xml.SignatureUtil;

/**
 * Helps with parsing Atom content.
 */
public class AtomParser {
    private static final Logger log = LoggerFactory.getLogger(AtomParser.class);

    /**
     * Adds the given entities as children to the destinationElement, including associating them with the new destination's owner document.
     */
    public void addEntries(Element destinationElement, List<Node> nodesToAdd) {
        nodesToAdd.forEach(node -> {
            destinationElement.getOwnerDocument().adoptNode(node);
            destinationElement.appendChild(node);
        });
    }

    /**
     * Removes all direct children Atom Entry nodes from the given DOM element and returns them as a new list. DOM element parameter is updated in
     * place to be without these removed nodes. All removed nodes are placed into their own document.
     */
    public List<Node> removeEntryNodes(Element domContainingEntries) {
        NodeList nodeList = domContainingEntries.getElementsByTagNameNS(ATOM_XMLNS, ATOM_ENTRY);

        List<Node> removedNodes = new ArrayList<Node>();
        int numEntryNodes = nodeList.getLength();
        for (int i = 0; i < numEntryNodes; i++) {
            Node node = nodeList.item(i);
            removedNodes.add(node);
        }
        // Need to do the actual removal in a 2nd loop, else as we remove by
        // index, above, the nodes will shift down the location
        // thereby not actually removing all the nodes we want
        removedNodes.forEach(node -> {
            domContainingEntries.removeChild(node);
            putInOwnDoc(node);
        });
        return removedNodes;
    }

    /**
     * Modifies a node to be in its own standalone, namespace aware (important!) document. Nodes modified in place
     */
    private static void putInOwnDoc(Node domNode) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc;
        try {
            doc = dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            log.error("Couldn't build new document, should never happen!", e);
            throw new RuntimeException(e);
        }
        doc.adoptNode(domNode);
        doc.appendChild(domNode);
    }

    /** Returns the first child element with specified XMLNS / name match, or null if none exists */
    public Node getFirstNode(Element node, String xmlns, String nodeName) {
        NodeList nodeList = node.getElementsByTagNameNS(xmlns, nodeName);
        return nodeList.item(0);
    }

    /**
     * Returns the text content of the first child of the given node of type 'id'
     * 
     * @throws IllegalArgumentException
     *             if no such node exists
     */
    public String getId(Element domElement) {
        NodeList nl = domElement.getElementsByTagNameNS(ATOM_XMLNS, "id");
        if (nl.getLength() == 0) {
            log.debug("Atom entries must have an <id> element");
            throw new IllegalArgumentException("Atom entries must have a <id> element");
        } else {
            return nl.item(0).getTextContent();
        }
    }

    /**
     * Returns the text content of the first child of the given node of type 'title'
     * 
     * @throws IllegalArgumentException
     *             if no such node exists
     */
    public String getTitle(Element domElement) {
        NodeList nl = domElement.getElementsByTagNameNS(ATOM_XMLNS, ATOM_TITLE);
        if (nl.getLength() == 0) {
            log.debug("Atom entries must have an <title> element");
            throw new IllegalArgumentException("Atom entries must have a <title> element");
        } else {
            return nl.item(0).getTextContent();
        }
    }

    /**
     * Returns the first <predecessor> node that appears under the given element, or null if no such node exists Since <feed> elements don't have a
     * Trsst <predecessor> node, this returns the first Entry predecessor value regardless of whether given a feed or entry root
     */
    public Element getFirstPredecessorNode(Element domElement) {
        return (Element) getFirstNode(domElement, TRSST_XMLNS, TRSST_PREDECESSOR);
    }

    /**
     * Returns the content of the "SignatureValue" node of the first entry element on the given DOM elements.
     * 
     * If given a Feed element, removes <entry> nodes from the feed and then checks the first entry element
     * 
     * @throws IllegalArgumentException
     *             if no such node exists
     */
    public String getEntrySignatureValue(Element domElement) {

        List<Node> removedEntries = removeEntryNodes(domElement);

        Element firstEntry;
        // If we had to remove them from the feed, set it to the first, otherwise expect we were given the entry itself
        if (removedEntries.size() == 0) {
            firstEntry = domElement;
        } else {
            firstEntry = (Element) removedEntries.get(0);
        }

        Node signatureNode = getFirstNode(firstEntry, SIGNATURE_XMLNS, SIGNATURE_VALUE);
        if (signatureNode == null) {
            log.debug("Expected to find xml signature on entry node");
            throw new IllegalArgumentException("Expected to find xml signature on entry node");
        }
        String signatureValue = signatureNode.getTextContent();

        if (removedEntries.size() > 0) {
            addEntries(domElement, removedEntries);
        }

        return signatureValue;
    }

    public boolean doEntriesMatchFeedId(String feedId, List<Node> detachedEntries) throws XMLSignatureException {
        boolean doMatch = false;
        for (Node entry : detachedEntries) {
            // Extract each signed entry's public key
            PublicKey key = extractPublicKey(entry);
            // Compute the hash which should match this feed's public key
            String entryKeyHash = TrsstKeyFunctions.toFeedId(key);
            doMatch = feedId.equals(entryKeyHash);
            if (!doMatch) {
                return false;
            }
        }
        return doMatch;
    }

    private PublicKey extractPublicKey(Node entry) throws XMLSignatureException {
        DOMValidateContext valContext = SignatureUtil.extractValidationContext((Element) entry);
        XMLSignature signature = SignatureUtil.extractSignature(valContext);
        KeyInfo keyInfo = signature.getKeyInfo();
        SignedInfo signedInfo = signature.getSignedInfo();
        SignatureMethod method = signedInfo.getSignatureMethod();
        KeySelectorResult result;
        try {
            result = valContext.getKeySelector().select(keyInfo, Purpose.VERIFY, method, valContext);
        } catch (KeySelectorException e) {
            log.error(e.getMessage(), e);
            throw new XMLSignatureException("Couldn't find the public key in the signature!");
        }
        PublicKey key = (PublicKey) result.getKey();
        return key;
    }
}
