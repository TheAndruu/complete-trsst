package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.TRSST_XMLNS;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface EntryModule extends Module, CopyFrom {

    public static final String URI = TRSST_XMLNS;

    // TODO: Add signature element

    public String getPredecessorValue();
    public void setPredecessorValue(String predecessor);

}