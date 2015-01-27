package com.completetrsst.atom;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.w3c.dom.Element;

import com.completetrsst.crypto.xml.encrypt.EncryptionUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;

public class AtomEncrypter {

    public static final String ENCRYPTED_TITLE = "Encrypted content";

    private final static EncryptionUtil util = new EncryptionUtil();

    private final static AtomSigner signer = new AtomSigner();

    /**
     * Creates an entry which sets the title to the literal words "Encrypted content" and adds the given titleas a plaintext Content entry for later
     * encryption
     */
    Entry createEntryTitleInContent(String entryTitle) {
        // default scope for testing
        Entry entry = signer.createEntry(entryTitle);
        entry.setTitle(ENCRYPTED_TITLE);
        Content content = new Content();
        // Set as plain text here, during encryption it will change to application/xenc+xml
        content.setType(Content.TEXT);
        content.setValue(entryTitle);
        entry.setContents(Collections.singletonList(content));
        return entry;
    }

    /**
     * Creates a new signed Atom entry inside a feed with given title, wrapped inside an individually-signed Atom feed element. The Content node of
     * the Atom Entry will be encrypted and only readable by the possessors of any private keys which match the given public keys.
     * 
     * @param entryTitle
     *            Title for the entry
     * @param keyPair
     *            The public/private key pair used to sign this entry
     * @param recipientKeys
     *            The public keys of who shall decrypt the encrypted content
     * @return DOM element containing a signed Feed node and independently-signed Entry node
     */
    public Element createEncryptedEntryAsDom(String entryTitle, KeyPair signingKeys, KeyPair encryptionKeys, List<PublicKey> recipientKeys) throws IOException,
            GeneralSecurityException, XMLSignatureException {
        // Construct the feed and entry
        Element domEntry = signer.toDom(createEntryTitleInContent(entryTitle));
        Element domFeed = signer.toDom(signer.createFeed(signingKeys.getPublic(), encryptionKeys.getPublic()));

        // Encrypt entry prior to signing
        util.encrypt(domEntry, encryptionKeys, recipientKeys);

        // Sign and build feed
        signer.signAndBuildFeed(signingKeys, domEntry, domFeed);

        return domFeed;
    }

    /**
     * Creates a new signed Atom entry inside a feed with given title, wrapped inside an individually-signed Atom feed element. The Content node of
     * the Atom Entry will be encrypted and only readable by the possessors of any private keys which match the given public keys.
     * 
     * @param entryTitle
     *            Title for the entry
     * @param keyPair
     *            The public/private key pair used to sign this entry
     * @param recipientKeys
     *            The public keys of who shall decrypt the encrypted content
     * @return DOM element containing a signed Feed node and independently-signed Entry node
     */
    public String createEncryptedEntry(String entryTitle, KeyPair signingKeys, KeyPair encryptionKeys, List<PublicKey> recipientKeys) throws IOException,
            GeneralSecurityException, XMLSignatureException {
        return XmlUtil.serializeDom(createEncryptedEntryAsDom(entryTitle, signingKeys, encryptionKeys, recipientKeys));
    }
}
