package com.completetrsst.atom;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.crypto.xml.encrypt.EncryptionUtil;

public class AtomVerifier {
    private static final Logger log = LoggerFactory.getLogger(AtomVerifier.class);
    private static final AtomParser parser = new AtomParser();
    private static final EncryptionUtil decrypter = new EncryptionUtil();

    /**
     * Validates the signature on just a Feed element, regardless of whether the given object contains just a Feed or Feed and signed entries. Only
     * the feed is validated.
     */
    public boolean isFeedVerified(Element feedAndEntry) throws XMLSignatureException {
        List<Node> removedEntries = parser.removeEntryNodes(feedAndEntry);
        boolean isVerified = SignatureUtil.verifySignature(feedAndEntry);
        parser.addEntries(feedAndEntry, removedEntries);

        log.debug("Is feed verified? " + isVerified);
        return isVerified;
    }

    /**
     * Validates the signature on all Entry elements, whether given a feed containing entries or a standalne entry itself. Only the entires are
     * validated.
     */
    public boolean areEntriesVerified(Element feedAndEntry) throws XMLSignatureException {
        List<Node> removedEntries = parser.removeEntryNodes(feedAndEntry);

        boolean isVerified = true;
        Iterator<Node> removedEntryIterator = removedEntries.iterator();
        while (removedEntryIterator.hasNext() && isVerified) {
            Node entryNode = removedEntryIterator.next();
            isVerified = isVerified && SignatureUtil.verifySignature((Element) entryNode);
        }

        parser.addEntries(feedAndEntry, removedEntries);

        log.debug("Are all entries verified? " + isVerified);
        return isVerified;
    }

}
