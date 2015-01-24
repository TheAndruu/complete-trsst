package com.completetrsst.crypto.xml.encrypt;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.Crypto;
import com.completetrsst.xml.XmlUtil;

/** Supports encryption of Atom <content> nodes contained within Atom <entry> elements */
public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    public static final String XMLNS_ENCRYPT = "http://www.w3.org/2001/04/xmlenc#";

    /**
     * Expects the Entry element in DOM form, with one content element on it. Encrypts the Content element, removes the unencrypted node, and replaces
     * it with the encrypted nodes
     */
    public void encrypt(Element domEntryElement, KeyPair encryptionKey, List<PublicKey> givenRecipientsPublicKeys) throws GeneralSecurityException {

        // Copy the list of public keys so we can add ours to it
        List<PublicKey> recipientPublicKeys = new ArrayList<PublicKey>(givenRecipientsPublicKeys);
        PublicKey encryptionPublicKey = encryptionKey.getPublic();
        if (recipientPublicKeys.contains(encryptionPublicKey)) {
            // Shouldn't usually happen, but if so, remove it so we add it to the end
            recipientPublicKeys.remove(encryptionPublicKey);
        }
        recipientPublicKeys.add(encryptionPublicKey);

        // AES256 key for encrypting -- created new every time
        byte[] contentKey = Crypto.generateAESKey();

        List<Element> nodesContainingEncryptedKeys = new ArrayList<Element>(recipientPublicKeys.size());
        // encrypt content key separately for each recipient
        for (PublicKey recipient : recipientPublicKeys) {
            byte[] encryptedBytes = Crypto.encryptKeyWithIES(contentKey, recipient, encryptionKey.getPrivate());
            // Create 'encryption data' elements
            Element encryptedDataKeyForPublicRecipient = createEncryptedData(domEntryElement, encryptedBytes);
            nodesContainingEncryptedKeys.add(encryptedDataKeyForPublicRecipient);
        }

        Node contentNode = getContentNode(domEntryElement);
        byte[] contentAsBytes = XmlUtil.serializeDom(contentNode).getBytes();
        byte[] encryptedContentNode;
        try {
            encryptedContentNode = Crypto.encryptAES(contentAsBytes, contentKey);
        } catch (InvalidCipherTextException e) {
            log.error(e.getMessage(), e);
            throw new GeneralSecurityException(e);
        }
        // This is the encrypted full <content> node from the original entry
        Element nodeContainingEncryptedContent = createEncryptedData(domEntryElement, encryptedContentNode);

        // now have nodesContainingEncryptedKeys and nodeContainingEncryptedContent
        // add these in place of the existing content element
        removeAllChildren(contentNode);
        ((Element) contentNode).setAttribute("type", "application/xenc+xml");

        nodesContainingEncryptedKeys.forEach(node -> contentNode.appendChild(node));
        contentNode.appendChild(nodeContainingEncryptedContent);
    }

    private Node getContentNode(Element domEntryElement) {
        NodeList nodeList = domEntryElement.getElementsByTagNameNS(AtomSigner.XMLNS, "content");
        Node contentNode = nodeList.item(0);
        return contentNode;
    }

    private void removeAllChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * Returns an unattached EncryptedData node for the given parent element containing the encrypted value/ This method does the encoding of the
     * 'cipherValueData' param
     * 
     * @param cipherValueData
     *            the un-encoded bytes to insert in the CipherValue
     */
    private Element createEncryptedData(Element ownerElement, byte[] cipherValueData) {
        String encoded = new Base64(0, null, true).encodeToString(cipherValueData);
        Document doc = ownerElement.getOwnerDocument();
        Element encryptedData = doc.createElementNS(XMLNS_ENCRYPT, "EncryptedData");
        Element cipherData = doc.createElementNS(XMLNS_ENCRYPT, "CipherData");
        Element cipherValue = doc.createElementNS(XMLNS_ENCRYPT, "CipherValue");
        cipherValue.setTextContent(encoded);
        cipherData.appendChild(cipherValue);
        encryptedData.appendChild(cipherData);
        return encryptedData;
    }

    /**
     * Decrypts the content of an Atom entry node, represented in DOM form and returns the Text content
     * 
     * @param entryDom
     *            Atom Entry DOM node
     * @param privateKey
     *            The private key who can decrypt the content
     * @return The text of the decrypted content
     * @throws GeneralSecurityException
     *             If the private key cannot decrypt the content of the Atom entry
     */
    public String decryptText(Element entryDom, PrivateKey privateKey) throws GeneralSecurityException {
        return decrypt(entryDom, privateKey).getTextContent();
    }

    /**
     * Decrypts the content of an Atom entry node, represented in DOM form and returns the Content which was decrypted
     * 
     * @param entryDom
     *            Atom Entry DOM node
     * @param privateKey
     *            The private key who can decrypt the content
     * @return The decrypted content node in DOM form
     * @throws GeneralSecurityException
     *             If the private key cannot decrypt the content of the Atom entry
     */
    public Element decrypt(Element entryWithEncryptedContent, PrivateKey privateKey) throws GeneralSecurityException {
        Node contentNode = getContentNode(entryWithEncryptedContent);
        NodeList encryptedElements = contentNode.getChildNodes();
        int numChildren = encryptedElements.getLength();
        // Stop at second to last child node, these are the encrypted keys
        byte[] decryptedKey = null;
        for (int i = 0; i < numChildren - 1; i++) {
            Element encryptedData = ((Element) encryptedElements.item(i));
            byte[] decodedCipherValue = getCipherValueText(encryptedData);
            try {
                decryptedKey = Crypto.decryptKeyWithIES(decodedCipherValue, privateKey);
                // If we get here, the key was decrypted, so break this loop as its job is done
                break;
            } catch (GeneralSecurityException e) {
                log.debug("Key did not fit, try next node" + e.getMessage());
            }
        }
        if (decryptedKey == null) {
            throw new GeneralSecurityException("Could not decrypt key from ElementData");
        }
        // minus 1 because numChildren is the count, not indices
        Element lastEncryptedNode = (Element) encryptedElements.item(numChildren - 1);
        // get the decoded cipher value representing the encrypted <content> node
        byte[] decodedCipherValue = getCipherValueText(lastEncryptedNode);

        byte[] decryptedContent;
        try {
            decryptedContent = Crypto.decryptAES(decodedCipherValue, decryptedKey);
        } catch (InvalidCipherTextException e) {
            log.debug("Could not decrypt the encrypted content");
            throw new GeneralSecurityException("Could not decrypt the encrypted content", e);
        }

        try {
            // Auto-detect the encoding?
            String decryptedAsString = new String(decryptedContent, "UTF-8");
            return XmlUtil.toDom(decryptedAsString);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private byte[] getCipherValueText(Element encryptedData) {
        Node cipherValue = encryptedData.getElementsByTagNameNS(XMLNS_ENCRYPT, "CipherValue").item(0);
        String cipherValueText = cipherValue.getTextContent();
        return new Base64().decode(cipherValueText);
    }
}
