package com.completetrsst.crypto.xml.encrypt;

import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.atom.AtomEncrypter;
import com.completetrsst.atom.AtomSigner;
import com.completetrsst.crypto.Crypto;
import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.xml.TestUtil;
import com.completetrsst.xml.XmlUtil;

public class EncryptionUtilTest {

    private AtomSigner atom;
    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

    private EncryptionUtil util;
    @Before
    public void init() {
        atom = new AtomEncrypter();
        util = new EncryptionUtil(); 
    }

    @Test
    public void testEncrypt() throws Exception {
        String rawXmlWithContent = atom.newEntry("my first encrypted entry", keyPair);
        Element domEntryWithContent = XmlUtil.toDom(rawXmlWithContent);
        
        util.encrypt(domEntryWithContent, keyPair, Collections.singletonList(keyPair.getPublic()));
        
        String rawXmlWithEncryptedContent = TestUtil.format(XmlUtil.serializeDom(domEntryWithContent));
        System.out.println(rawXmlWithEncryptedContent);
        
    }
    
    @Test
    public void decrypt() {
        String encodedKey = "BI6n8JdyNjt1mhzAH9Z2SGqf5-eZnmCQnWt3KdCfwf5hoDLYJ1aT46AQ8Yp9GpO_D6St-cGnq9vx0FE4Yv_ci4uSvA6PFKYPoLGsackZUDyFe4xtdOPtBhV45nbki0clNBIL0NEM7ynQNjcz4RZ6HkqrKccPgj4DzID7dLgOXTQxmU4wBNJBka7MBOGlRk4GzA";
        String encodedContent = "f1QHUmQOgDIRh1jKBuYXBWS8O77fLnHAcTSnLYYs-KLQXEUNJ143Ibz-GKndKvb4BIWLfwCagqKVO7NOisUThSmoL80dDTk_8_FGCkW1AfWr7795LFfxv_cOaCkXLbUq";
        byte [] encodedKeyBytes = new Base64().decode(encodedKey);
        byte [] encodedContentBytes = new Base64().decode(encodedContent);
        // Can't decrypt b/c we lost track of our private key!
        // need to do it at same time as encryption happens (or save private key somewhere)
//        Crypto.decryptKeyWithIES(encodedKeyBytes, encodedContentBytes); 
        
    }

}
