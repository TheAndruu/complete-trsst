package com.completetrsst.rome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;

public class SampleModuleGenerator implements ModuleGenerator {
	private static final Namespace SAMPLE_NS = Namespace.getNamespace("sample",
			SampleModule.URI);

	private static final Set<Namespace> NAMESPACES;

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

		// TODO: This Element 'element' is what we want to encrypt-- a jdom2
		// element, representing the 'entry' in atom
		
		SampleModule fm = (SampleModule) module;
		if (fm.getFoo() != null) {
			
			Element elementWhichWillLaterBeSecurity = generateSimpleElement("foo",
					fm.getFoo());
			element.addContent(elementWhichWillLaterBeSecurity);
		}
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