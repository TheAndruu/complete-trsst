package com.completetrsst.crypto.xml;

import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class KeyValueKeySelector extends KeySelector {

    @SuppressWarnings("unchecked")
    public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
            XMLCryptoContext context) throws KeySelectorException {

        if (keyInfo == null) {
            throw new KeySelectorException("Null KeyInfo object!");
        }
        SignatureMethod signatureMethod = (SignatureMethod) method;
        List<XMLStructure> xmlNodes = keyInfo.getContent();

        for (XMLStructure xmlStructure : xmlNodes) {
            if (xmlStructure instanceof KeyValue) {
                PublicKey publicKey = null;
                try {
                    publicKey = ((KeyValue) xmlStructure).getPublicKey();
                } catch (KeyException ke) {
                    throw new KeySelectorException(ke);
                }
                // make sure algorithm is compatible with method
                if (algEquals(signatureMethod.getAlgorithm(), publicKey.getAlgorithm())) {
                    return new SimpleKeySelectorResult(publicKey);
                }
            }
        }
        throw new KeySelectorException("No KeyValue element found!");
    }

    static boolean algEquals(String algURI, String algName) {
        // TODO: Remove the other types of keys since we don't plan on supporting them?
        if (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
            return true;
        } else if (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
            return true;
        } else if (algName.equalsIgnoreCase("EC") && algURI.equalsIgnoreCase(SignatureUtil.ECDSA_SHA1)) {
            return true;
        } else {
            return false;
        }
    }
}
