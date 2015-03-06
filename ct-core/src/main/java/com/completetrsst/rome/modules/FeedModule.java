package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface FeedModule extends Module, CopyFrom {

    public static final String URI = TRSST_XMLNS;

    public String getSignKey();

    public void setSignKey(String signKeyX509);

    public String getEncryptKey();

    public void setEncryptKey(String encryptKeyX509);

    /** Doesn't specify if the element's signature is verified, only specifies that a signature is present */
    public void setIsSigned(Boolean isSigned);

    /** Doesn't specify if the element's signature is verified, only specifies that a signature is present */
    public Boolean isSigned();

}