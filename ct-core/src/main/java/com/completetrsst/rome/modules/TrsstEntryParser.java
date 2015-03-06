package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.ENCRYPT_NAMESPACE;
import static com.completetrsst.constants.Namespaces.SIGNATURE_NAMESPACE;
import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Nodes.ENCRYPTED_DATA;
import static com.completetrsst.constants.Nodes.SIGNATURE;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;

import java.io.IOException;
import java.util.Locale;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.constants.Namespaces;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

public class TrsstEntryParser implements ModuleParser {

    private static final Logger log = LoggerFactory.getLogger(TrsstEntryParser.class);

    @Override
    public String getNamespaceUri() {
        return EntryModule.URI;
    }

    @Override
    public Module parse(Element dcRoot, Locale locale) {
        String predecessorValue = extractPredecssor(dcRoot);
        boolean isSigned = isSigned(dcRoot);
        boolean isEncrypted = isEncrypted(dcRoot);
        
        // TODO: Add other checks here for determining if its a trsst entry
        // If entry had either a trsst sign or trsst encrypt node, consider it a trsst feed
        if (!predecessorValue.isEmpty() || isSigned || isEncrypted) {
            log.debug("Found Trsst entry element using TrsstEntryParser");
            EntryModule fm = new TrsstEntryModule();
            fm.setPredecessorValue(predecessorValue);
            fm.setIsSigned(isSigned);
            if (isSigned) {
                try {
                    org.w3c.dom.Element domEntry = XmlUtil.toDom(dcRoot);
                    boolean isValid = SignatureUtil.verifySignature(domEntry);
                    fm.setSignatureValid(isValid);
                } catch (XMLSignatureException | IOException e) {
                    log.warn("Signed entry couldn't be verified: " + e.getMessage());
                }    
            }
            
            fm.setIsEncrypted(isEncrypted);
            return fm;
        }

        return null;
    }

    private boolean isEncrypted(Element dcRoot) {
        Element node = dcRoot.getChild(ENCRYPTED_DATA, ENCRYPT_NAMESPACE);
        return node != null;
    }

    private String extractPredecssor(Element dcRoot) {
        Element signNode = dcRoot.getChild(TRSST_PREDECESSOR, TRSST_NAMESPACE);
        return signNode == null ? "" : signNode.getText();
    }

    private boolean isSigned(Element dcRoot) {
        Element signNode = dcRoot.getChild(SIGNATURE, SIGNATURE_NAMESPACE);
        return signNode != null;
    }
}
