package com.completetrsst.atom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.crypto.xml.SignatureUtil;

public class SignedAtomVerifier {
    private static final Logger log = LoggerFactory.getLogger(SignedAtomVerifier.class);

    /**
     * Validates the signature on just a Feed element, regardless of whether the
     * given object contains just a Feed or Feed and signed entries. Only the
     * feed is validated.
     */
    public boolean isFeedVerified(Element feedAndEntry) throws XMLSignatureException {
        List<Node> removedEntries = removeEntryNodes(feedAndEntry);
        boolean isVerified = SignatureUtil.verifySignature(feedAndEntry);
        addEntries(feedAndEntry, removedEntries);

        log.debug("Is feed verified? " + isVerified);
        return isVerified;
    }

    /**
     * Validates the signature on all Entry elements, whether given a feed
     * containing entries or a standalne entry itself. Only the entires are
     * validated.
     */
    public boolean areEntriesVerified(Element feedAndEntry) throws XMLSignatureException {
        List<Node> removedEntries = removeEntryNodes(feedAndEntry);

        boolean isVerified = true;
        for (Node entryNode : removedEntries) {
            isVerified = isVerified && SignatureUtil.verifySignature((Element) entryNode);
        }

        addEntries(feedAndEntry, removedEntries);

        log.debug("Are all entries verified? " + isVerified);
        return isVerified;
    }

    /**
     * Removes all Atom Entry nodes from the given DOM feed element and returns
     * them as a new list. DOM Feed element is updated in place. All removed
     * nodes are placed into their own document.
     */
    List<Node> removeEntryNodes(Element domContainingEntries) {
        NodeList nodeList = domContainingEntries.getElementsByTagNameNS(AtomSigner.XMLNS, "entry");
        // we know there's only one entry on this feed
        List<Node> removedNodes = new ArrayList<Node>();
        int numEntryNodes = nodeList.getLength();
        for (int i = 0; i < numEntryNodes; i++) {
            Node node = nodeList.item(i);
            removedNodes.add(node);
        }
        // Need to do the actual removal in a 2nd loop, else as we remove by
        // index,
        // above, the removals will shift down the location of the next node
        // thereby not actually removing all the nodes we want
        removedNodes.forEach(node -> {
            domContainingEntries.removeChild(node);
            putInOwnDoc(node);
        });
        return removedNodes;
    }

    /**
     * Modifies a node to be in its own standalone, namespace aware (important!)
     * document. Nodes modified in place
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

    /**
     * Adds the given entities to the feed, including associating them with the
     * new document.
     */
    void addEntries(Element destinationElement, List<Node> entityNodesToAdd) {
        entityNodesToAdd.forEach(entryNode -> {
            destinationElement.getOwnerDocument().adoptNode(entryNode);
            destinationElement.appendChild(entryNode);
        });
    }
}
