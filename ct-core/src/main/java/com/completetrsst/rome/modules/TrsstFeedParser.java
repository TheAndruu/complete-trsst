package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Nodes.TRSST_ENCRYPT;
import static com.completetrsst.constants.Nodes.TRSST_SIGN;

import java.util.Locale;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

public class TrsstFeedParser implements ModuleParser {

    private static final Logger log = LoggerFactory.getLogger(TrsstFeedParser.class);

    @Override
    public String getNamespaceUri() {
        return FeedModule.URI;
    }

    @Override
    public Module parse(Element dcRoot, Locale locale) {
        String signKey = extractSignKey(dcRoot);
        String encryptKey = extractEncryptKey(dcRoot);

        // If feed had either a trsst sign or trsst encrypt node, consider it a trsst feed
        if (signKey != null || encryptKey != null) {
            log.debug("Found Trsst feed element using TrsstFeedParser");
            FeedModule fm = new TrsstFeedModule();
            fm.setSignKey(signKey);
            fm.setEncryptKey(encryptKey);
            return fm;
        }

        return null;
    }

    private String extractSignKey(Element dcRoot) {
        Element signNode = dcRoot.getChild(TRSST_SIGN, TRSST_NAMESPACE);
        return signNode == null ? null : signNode.getText();
    }

    private String extractEncryptKey(Element dcRoot) {
        Element encryptNode = dcRoot.getChild(TRSST_ENCRYPT, TRSST_NAMESPACE);
        return encryptNode == null ? null : encryptNode.getText();
    }
}
