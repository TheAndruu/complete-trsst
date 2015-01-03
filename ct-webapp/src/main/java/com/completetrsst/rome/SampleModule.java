package com.completetrsst.rome;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface SampleModule extends Module, CopyFrom {

    public static final String URI = "http://rome.dev.java.net/module/sample/1.0";

    public Foo getFoo();

    public void setFoo(Foo foo);
}