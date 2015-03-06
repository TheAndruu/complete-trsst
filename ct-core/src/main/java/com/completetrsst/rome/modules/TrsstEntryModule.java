package com.completetrsst.rome.modules;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class TrsstEntryModule extends ModuleImpl implements EntryModule {

    private static final long serialVersionUID = -5045125952451910984L;

    private String predecessorValue = "";
    private Boolean isSigned = false;
    private Boolean isEncrypted = false;
    private Boolean isSignatureValid = false;

    public TrsstEntryModule() {
        super(TrsstEntryModule.class, EntryModule.URI);
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return EntryModule.class;
    }

    // must do a deep copy
    @Override
    public void copyFrom(CopyFrom obj) {
        EntryModule sm = (EntryModule) obj;
        setPredecessorValue(sm.getPredecessorValue());
        setIsSigned(sm.isSigned());
        setIsEncrypted(sm.isEncrypted());
        setSignatureValid(sm.isSignatureValid());
    }

    @Override
    public String getPredecessorValue() {
        return predecessorValue;
    }

    @Override
    public void setPredecessorValue(String predecessorValue) {
        this.predecessorValue = predecessorValue;
    }

    @Override
    public Boolean isSigned() {
        return isSigned;
    }

    @Override
    public void setIsSigned(Boolean isSigned) {
        this.isSigned = isSigned;
    }

    @Override
    public Boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CopyFrom module = (CopyFrom)super.clone();
        module.copyFrom(this);
        return module;
    }

    @Override
    public void setSignatureValid(Boolean isValid) {
        this.isSignatureValid = isValid;
    }

    @Override
    public Boolean isSignatureValid() {
        return isSignatureValid;
    }

}