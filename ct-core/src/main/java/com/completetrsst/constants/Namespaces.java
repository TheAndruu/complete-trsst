package com.completetrsst.constants;

import javax.xml.crypto.dsig.XMLSignature;

import org.jdom2.Namespace;

public class Namespaces {

    public static final String ENCRYPT_XMLNS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String SIGNATURE_XMLNS = XMLSignature.XMLNS;
    public static final Namespace SIGNATURE_NAMESPACE = Namespace.getNamespace(XMLSignature.XMLNS);
    
    public static final String ATOM_XMLNS = "http://www.w3.org/2005/Atom";
    
    public static final String TRSST_XMLNS = "http://trsst.com/spec/0.1";
    public static final Namespace TRSST_NAMESPACE = Namespace.getNamespace(TRSST_XMLNS);

}
