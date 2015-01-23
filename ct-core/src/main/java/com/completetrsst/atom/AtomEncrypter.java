package com.completetrsst.atom;

import java.util.Collections;

import org.w3c.dom.Element;

import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;

public class AtomEncrypter extends AtomSigner {

    public static final String ENCRYPTED_TITLE = "Encrypted content";

    /**
     * Creates an encrypted entry which sets the title to the literal words "Encrypted content" and adds the given title text as a plaintext Content
     * entry for later encryption
     */
    @Override
    protected Entry createEntry(String title) {
        Entry entry = super.createEntry(title);
        entry.setTitle(ENCRYPTED_TITLE);
        Content content = new Content();
        // Set as plain text here, during encryption it will change to application/xenc+xml
        content.setType(Content.TEXT);
        content.setValue(title);
        entry.setContents(Collections.singletonList(content));
        return entry;
    }
    
    /** Prior to signing the entry element, perform the encryption here */
    @Override
    protected void finalizeBeforeSigning(Element domEntry) {
        // TODO Encrypt the content elements
//        EncryptionUtil.encryptDom(domEntry);
    }
}
