package com.completetrsst.rome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;

public class SampleModuleParser implements ModuleParser {

    private static final Namespace SAMPLE_NS = Namespace.getNamespace("sample", SampleModule.URI);

    @Override
    public String getNamespaceUri() {
        return SampleModule.URI;
    }

    @Override
    public Module parse(Element dcRoot, Locale locale) {
        boolean foundSomething = false;
        SampleModule fm = new SampleModuleImpl();

        Element e = dcRoot.getChild("foo", SAMPLE_NS);
        if (e != null) {
            foundSomething = true;
            fm.setFoo(copyFoo(e));
        }

        return (foundSomething) ? fm : null;
    }

    private Foo copyFoo(Element e) {
        Foo foo = new Foo();
        Element barContent = (Element) e.getContent();
        Text itemText = (Text) barContent.getContent();
        Bar newBar = new Bar();
        newBar.setItem(itemText.getText());
        foo.setBar(newBar);
        return foo;
    }

    private List<String> parseFoos(List<Element> eList) {
        List<String> foos = new ArrayList<String>();
        for (int i = 0; i < eList.size(); i++) {
            Element e = (Element) eList.get(i);
            foos.add(e.getText());
        }
        return foos;
    }

}
