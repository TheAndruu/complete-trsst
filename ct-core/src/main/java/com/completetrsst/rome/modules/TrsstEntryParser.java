package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.*;
import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Nodes.*;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;

import java.util.Locale;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
