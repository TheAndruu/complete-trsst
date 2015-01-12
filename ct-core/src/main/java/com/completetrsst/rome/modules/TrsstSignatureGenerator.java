package com.completetrsst.rome.modules;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.crypto.dsig.XMLSignatureException;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

// Likely get rid of all this module stuff
public class TrsstSignatureGenerator implements ModuleGenerator {
	private static final Namespace SAMPLE_NS = Namespace.getNamespace("trsst", TrsstModule.URI);

	private static final Set<Namespace> NAMESPACES;
	
	private static final Logger log = LoggerFactory.getLogger(TrsstSignatureGenerator.class);

	static {
		Set<Namespace> nss = new HashSet<Namespace>();
		nss.add(SAMPLE_NS);
		NAMESPACES = Collections.unmodifiableSet(nss);
	}

	@Override
	public String getNamespaceUri() {
		return TrsstModule.URI;
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return NAMESPACES;
	}

	@Override
	public void generate(Module module, Element element) {

		// this is not necessary, it is done to avoid the namespace definition
		// in every item.
//		Element root = element;
//		while (root.getParent() != null && root.getParent() instanceof Element) {
//			root = (Element) element.getParent();
//		}
//		root.addNamespaceDeclaration(SAMPLE_NS);

		TrsstModule fm = (TrsstModule)module;
		
		// Here can check if type is feed or entry, if necessary
		if (fm.getIsSigned()) {
			try {
	            SignatureUtil.signElement(element, fm.getKeyPair());
            } catch (XMLSignatureException e) {
	            log.error(e.getMessage(), e);
            }
		}
		
		// This is too early to sign the element -- it's got all kinds of namespaces, etc
		// Don't use modules, at least for generation.  Use rome and toDom /jDom directly
		log.info("Inside generator");
		try {
			log.info(XmlUtil.serializeJdom(element));
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		
//		SampleModule fm = (SampleModule) module;
//		if (fm.getFoo() != null) {
//
//			Element elementWhichWillLaterBeSecurity = generateSimpleElement("foo", fm.getFoo());
//			element.addContent(elementWhichWillLaterBeSecurity);
//		}
	}

	
}