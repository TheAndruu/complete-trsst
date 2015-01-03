package com.completetrsst.rome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.crypto.dsig.XMLSignature;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.crypto.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

public class SampleModuleGenerator implements ModuleGenerator {
	private static final Namespace SAMPLE_NS = Namespace.getNamespace("sample", SampleModule.URI);

	private static final Set<Namespace> NAMESPACES;
	
	private static final Logger log = LoggerFactory.getLogger(SampleModuleGenerator.class);

	static {
		Set<Namespace> nss = new HashSet<Namespace>();
		nss.add(SAMPLE_NS);
		NAMESPACES = Collections.unmodifiableSet(nss);
	}

	@Override
	public String getNamespaceUri() {
		return SampleModule.URI;
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return NAMESPACES;
	}

	@Override
	public void generate(Module module, Element element) {

		// this is not necessary, it is done to avoid the namespace definition
		// in every item.
		Element root = element;
		while (root.getParent() != null && root.getParent() instanceof Element) {
			root = (Element) element.getParent();
		}
		root.addNamespaceDeclaration(SAMPLE_NS);

		
		log.info("Logging the original element to work with:");
		XmlUtil.logJdomElement(element);

		// TODO: This Element 'element' is what we want to encrypt-- a jdom2
		// element, representing the 'entry' in atom
		org.w3c.dom.Element signedDomElement = null;
		try {
			signedDomElement = XmlUtil.convertToDOM(element);
		} catch (JDOMException e1) {
			throw new RuntimeException(e1);
		}
		XmlUtil.logDomElement(signedDomElement);

		try {
			SignatureUtil.attachSignature(signedDomElement);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element newJdomWithSignature = XmlUtil.toJdom(signedDomElement);

		XmlUtil.logJdomElement(newJdomWithSignature);
		
		// TODO: Now take the signature from the above jdom element and move it to the param version
		log.info("new jdom content size: " + newJdomWithSignature.getContentSize());

		// grab the signature element:
		Element signatureElement = newJdomWithSignature.getChild("Signature", Namespace.getNamespace(XMLSignature.XMLNS));
		XmlUtil.logJdomElement(signatureElement);
		
		// attach to existing jdom element
		signatureElement.detach();
		element.addContent(signatureElement);
		
		try {
			//log.info("Element valid? " + ElementUtils.verifySignature(ElementUtils.toDom(element)));
//			log.info("new jdom valid? " + ElementUtils.verifySignature(ElementUtils.toDom(newJdomWithSignature)));
//			log.info("Original signed dom valid? " + ElementUtils.verifySignature(signedDomElement));
	        
        } catch (Exception e) {
	        log.error(e.getMessage());
        }
		// TODO: What to add to our new module? a boolean for do encrypt? for do
		// sign?
//		SampleModule fm = (SampleModule) module;
//		if (fm.getFoo() != null) {
//
//			Element elementWhichWillLaterBeSecurity = generateSimpleElement("foo", fm.getFoo());
//			element.addContent(elementWhichWillLaterBeSecurity);
//		}
	}

	protected Element generateSimpleElement(String name, Foo foo) {
		// TODO: Move these into a new Class (Foo extends Element, etc) with
		// hardcoded NS, etc
		// and even deep cloning
		Element fooElement = new Element(name, SAMPLE_NS);
		Element barElement = new Element("bar", SAMPLE_NS);
		Content itemContent = new Text(foo.getBar().getItem());
		barElement.setContent(itemContent);
		fooElement.setContent(barElement);

		return fooElement;
	}

}