package com.completetrsst.rome;

import java.util.Locale;

import javax.xml.crypto.dsig.XMLSignature;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

public class TrsstSignatureParser implements ModuleParser {

	private static final Namespace TRSST_NS = Namespace.getNamespace("trsst", TrsstModule.URI);

	@Override
	public String getNamespaceUri() {
		return TrsstModule.URI;
	}

	@Override
	public Module parse(Element dcRoot, Locale locale) {
		TrsstModule fm = null;

		// TODO: Here detect if the given element is encrypted or not
		// look for an element by node or ns, whatever is used in the publishing
		// ex: <content type="application/xenc+xml">
		// need to do encryption publishing first to figure out what to use

		Element e = dcRoot.getChild("Signature", Namespace.getNamespace(XMLSignature.XMLNS));
		if (e != null) {
			fm = new TrsstSignatureModule();
			fm.setIsSigned(true);
		}

		return fm;
	}
	//
	// private Foo copyFoo(Element e) {
	// Foo foo = new Foo();
	// Element barContent = (Element) e.getContent();
	// Text itemText = (Text) barContent.getContent();
	// Bar newBar = new Bar();
	// newBar.setItem(itemText.getText());
	// foo.setBar(newBar);
	// return foo;
	// }
	//
	// private List<String> parseFoos(List<Element> eList) {
	// List<String> foos = new ArrayList<String>();
	// for (int i = 0; i < eList.size(); i++) {
	// Element e = (Element) eList.get(i);
	// foos.add(e.getText());
	// }
	// return foos;
	// }

}
