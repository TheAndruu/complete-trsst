package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface EntryModule extends Module, CopyFrom {

    public static final String URI = TRSST_XMLNS;

    public String getPredecessorValue();

    public void setPredecessorValue(String predecessor);

    public void setIsEncrypted(Boolean isEncrypted);

    public Boolean isEncrypted();

    /** Doesn't specify if the element's signature is verified, only specifies that a signature is present */
    public void setIsSigned(Boolean isSigned);

    /** Doesn't specify if the element's signature is verified, only specifies that a signature is present */
    public Boolean isSigned();
    
    /** Whether a signature is valid */
    public void setSignatureValid(Boolean isSigned);

    /** Whether the signature is valid */
    public Boolean isSignatureValid();

}