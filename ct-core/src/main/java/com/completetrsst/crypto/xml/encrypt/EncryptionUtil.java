package com.completetrsst.crypto.xml.encrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
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
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.XmlUtil;

// https://svn.apache.org/repos/asf/santuario/xml-security-java/trunk/samples/org/apache/xml/security/samples/encryption/
public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String XMLNS_ENCRYPT = "http://www.w3.org/2001/04/xmlenc#";

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
     * Decrypts the 'content' node of an Entry element in dom form and returns the decrypted content node as a DOM element
     * 
     * @param entryWithEncryptedContent
     * @param privateKey
     * @throws GeneralSecurityException
     */
    public Element decryptText(Element entryWithEncryptedContent, PrivateKey privateKey) throws GeneralSecurityException {
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

    private void workingExample(Element entryAsDomElement) throws Exception {

        // These encryption keys will be read from storage / used by the user for their feed
        KeyPair encryptionKeys = new EllipticCurveKeyCreator().createKeyPair();

        // AES256 key for encrypting -- created new every time
        byte[] contentKey = Crypto.generateAESKey();

        // use said key to encrypt the content (before hashing-- whatever that means)
        // TODO: The 'content to encrypt' will come from the <Content> node of the parameter DOM
        // TODO: This might also need to happen in a loop for each content child
        Element contentElementToEncrypt = extractContentElement(entryAsDomElement);
        // This will likely need to return the node of the content first, then
        // get the content of that node, meaning everything will need to go in a loop
        // and also remove the content at that time, so below we just need to worry about appending them
        byte[] currentContent = extractContentData(contentElementToEncrypt);
        // byte[] currentContent = "the entry's content to encrypt".getBytes(); // = options.getContentData()[part];
        currentContent = Crypto.encryptAES(currentContent, contentKey);
        // content-type is present, but doesn't get encrypted

        // These can come in as parameters-- clients will need to fetch / supply the pub keys as x509 or whatnot
        // using something like keys.add(Common.toPublicKeyFromX509(e.getText()));
        List<PublicKey> recipientPublicKeys = new LinkedList<PublicKey>();
        // so TODO: add the public keys to this list

        // add our public key to the end of the list
        recipientPublicKeys.add(encryptionKeys.getPublic());

        // ---- ---- ----
        // ---- now actually encrypt the generated Content Key we're using for encryption
        // ---- for inclusion in the xml
        for (PublicKey recipient : recipientPublicKeys) {
            byte[] bytes = Crypto.encryptKeyWithIES(contentKey, recipient, encryptionKeys.getPrivate());
            String encoded = new Base64(0, null, true).encodeToString(bytes);
            // and write this key as bytes under EncryptedData, CipherData, and CipherValue

            // See Apache Xml Sanctuario if it has a way to add this to a DOM element
            // otherwise, construct w/ DOM entry element this way:
            Document doc = entryAsDomElement.getOwnerDocument();
            Element encryptedData = doc.createElementNS(XMLNS_ENCRYPT, "EncryptedData");
            Element cipherData = doc.createElementNS(XMLNS_ENCRYPT, "CipherData");
            Element cipherValue = doc.createElementNS(XMLNS_ENCRYPT, "CipherValue");
            cipherValue.setTextContent(encoded);
            cipherData.appendChild(cipherValue);
            encryptedData.appendChild(cipherData);

            // TODO: Remove all children above when we extract the content in the first place above
            contentElementToEncrypt.appendChild(encryptedData);
        }// done for... loop encrypting keys

        // TODO: Now encrypt the whole Content itself
        // TODO: Ensur ethe contentElementToEncrypt is only serializing the node itself
        byte[] contentAsBytes = XmlUtil.serializeDom(contentElementToEncrypt).getBytes();
        byte[] contentAsEncryptedBytes = Crypto.encryptAES(contentAsBytes, contentKey);
        String encoded = new Base64(0, null, true).encodeToString(contentAsEncryptedBytes);
        Document doc = entryAsDomElement.getOwnerDocument();
        Element encryptedData = doc.createElementNS(XMLNS_ENCRYPT, "EncryptedData");
        Element cipherData = doc.createElementNS(XMLNS_ENCRYPT, "CipherData");
        Element cipherValue = doc.createElementNS(XMLNS_ENCRYPT, "CipherValue");
        cipherValue.setTextContent(encoded);
        cipherData.appendChild(cipherValue);
        encryptedData.appendChild(cipherData);

        // TODO: now add this to the correct node and remove all other nodes as appropriate
        contentElementToEncrypt.appendChild(encryptedData);
    }

    private byte[] extractContentData(Element contentElementToEncrypt) {
        // TODO Auto-generated method stub
        return null;
    }

    private Element extractContentElement(Element entryAsDomElement) {
        // TODO Auto-generated method stub
        return null;
    }

    private void howDone() {

        // create 'content key' - AES 256 used to encrypt the content

        // get all public keys who can decrypt the messages we sign
        getPublicKeysWhoCanDecryptMessages();

        // then append our own key to the end of the public key list
        // then encrypt the content KEY for each recipient, incl our own pub key
        // -- this results in 2 nodes minimum, -- 1 for recipient and 1 for author
        encryptContentKEYSeparatelyForEachPublicKey();

        // then encrypt the payload itself with said Content KEY
        // -- this results in 1 node, containing the encrypted content
        encryptPayloadWithContentKey();

        // then sign the entry using the standard AtomSigner methods
    }

    private void getPublicKeysWhoCanDecryptMessages() {
        // TODO: Don't pull in the API code, have it be given teh public keys as parameters
        // so new method in RestController to get public key?
        // could query for Feed directly, return the content, and parse the key then return it
        // without needing to return entries-- new /feed/meta endpoint?

        List<PublicKey> keys = new LinkedList<PublicKey>();
        // Public keys can be obtained from pulling the feed of intended
        // recipients and extracting their <encrypt> or <sign> node
        // just use <sign>? is encrypt necessary to be added?

        // for (String id : options.recipientIds) {
        // // for each recipient
        // Feed recipientFeed = pull(id);
        // if (recipientFeed != null) {
        // // fetch encryption key
        // Element e = recipientFeed.getExtension(new QName(Common.NS_URI, Common.ENCRYPT));
        // if (e == null) {
        // // fall back to signing key
        // e = recipientFeed.getExtension(new QName(Common.NS_URI, Common.SIGN));
        // }
        // keys.add(Common.toPublicKeyFromX509(e.getText()));
        // }
        // }
    }

    private void encryptContentKEYSeparatelyForEachPublicKey() {
        // for (PublicKey recipient : keys) {
        // byte[] bytes = Crypto.encryptKeyWithIES(contentKey,
        // feed.getUpdated().getTime(), recipient,
        // encryptionKeys.getPrivate());
        // String encoded = new Base64(0, null, true)
        // .encodeToString(bytes);
        // writer.startElement("EncryptedData",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.startElement("CipherData",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.startElement("CipherValue",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.writeElementText(encoded);
        // writer.endElement();
        // writer.endElement();
        // writer.endElement();
        // }
    }

    private void encryptPayloadWithContentKey() {
        // now: encrypt the payload with content key
        // byte[] bytes = encryptElementAES(entry, contentKey);
        // String encoded = new Base64(0, null, true)
        // .encodeToString(bytes);
        // writer.startElement("EncryptedData",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.startElement("CipherData",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.startElement("CipherValue",
        // "http://www.w3.org/2001/04/xmlenc#");
        // writer.writeElementText(encoded);
        // writer.endElement();
        // writer.endElement();
        // writer.endElement();
    }
}
