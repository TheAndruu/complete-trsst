package com.completetrsst.crypto.xml.encrypt;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.Crypto;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.XmlUtil;

// https://svn.apache.org/repos/asf/santuario/xml-security-java/trunk/samples/org/apache/xml/security/samples/encryption/
public class EncryptionUtil {
    
    private static final String XMLNS_ENCRYPT  = "http://www.w3.org/2001/04/xmlenc#";

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
        byte [] currentContent = extractContentData(contentElementToEncrypt);
//        byte[] currentContent = "the entry's content to encrypt".getBytes(); // = options.getContentData()[part];
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
        byte [] contentAsBytes = XmlUtil.serializeDom(contentElementToEncrypt).getBytes();
        byte [] contentAsEncryptedBytes  = Crypto.encryptAES(contentAsBytes, contentKey);
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
