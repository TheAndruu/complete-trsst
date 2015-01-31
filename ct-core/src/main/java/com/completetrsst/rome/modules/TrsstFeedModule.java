package com.completetrsst.rome.modules;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class TrsstFeedModule extends ModuleImpl implements FeedModule {

    private static final long serialVersionUID = -5045125952451910984L;

    private String signKey = "";
    private String encryptKey = "";
    private boolean isSigned;

    public TrsstFeedModule() {
        super(TrsstFeedModule.class, FeedModule.URI);
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return FeedModule.class;
    }

    // must do a deep copy
    @Override
    public void copyFrom(CopyFrom obj) {
        FeedModule sm = (FeedModule) obj;
        setSignKey(sm.getSignKey());
        setEncryptKey(sm.getEncryptKey());
        setIsSigned(sm.isSigned());
    }

    @Override
    public String getSignKey() {
        return signKey;
    }

    @Override
    public void setSignKey(String signKeyX509) {
        this.signKey = signKeyX509;
    }

    @Override
    public String getEncryptKey() {
        return encryptKey;
    }

    @Override
    public void setEncryptKey(String encryptKeyX509) {
        this.encryptKey = encryptKeyX509;
    }

    @Override
    public boolean isSigned() {
        return isSigned;
    }

    @Override
    public void setIsSigned(boolean isSigned) {
        this.isSigned = isSigned;
    }

}