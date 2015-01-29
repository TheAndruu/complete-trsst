package com.completetrsst.rome.modules;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class TrsstEntryModule extends ModuleImpl implements EntryModule {

    private static final long serialVersionUID = -5045125952451910984L;

    private String predecessorValue = "";

    public TrsstEntryModule() {
        super(TrsstEntryModule.class, EntryModule.URI);
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return EntryModule.class;
    }

    // TODO: Test this method
    // must do a deep copy
    @Override
    public void copyFrom(CopyFrom obj) {
        EntryModule sm = (EntryModule) obj;
        setPredecessorValue(sm.getPredecessorValue());
    }

    @Override
    public String getPredecessorValue() {
        return predecessorValue;
    }

    @Override
    public void setPredecessorValue(String predecessorValue) {
        this.predecessorValue = predecessorValue;
    }

}