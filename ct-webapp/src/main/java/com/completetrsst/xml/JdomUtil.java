package com.completetrsst.xml;

import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class JdomUtil {

	public static Element readJDomFromFile(String filePath) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(filePath);
		return doc.getRootElement();
	}
}
