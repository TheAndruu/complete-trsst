package com.completetrsst.rome;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class SampleModuleImpl extends ModuleImpl implements SampleModule {
    private static final long serialVersionUID = 1L;

    private Foo foo;

    public SampleModuleImpl() {
        super(SampleModuleImpl.class, SampleModule.URI);
    }

    public Foo getFoo() {
        return foo;
    }

    public void setFoo(Foo foo) {
        this.foo = foo;
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return SampleModule.class;
    }

    // must do a deep copy
    @Override
    public void copyFrom(CopyFrom obj) {
        SampleModule sm = (SampleModule) obj;
        Foo otherFoo = sm.getFoo();
        // This clone performs a deep copy, so this is good
        setFoo(otherFoo.clone());
    }

}