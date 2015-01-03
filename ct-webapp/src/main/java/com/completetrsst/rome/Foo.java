package com.completetrsst.rome;

import java.io.Serializable;

public class Foo implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private Bar bar;

    public Bar getBar() {
        return bar;
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }

    /** Performs a deep clone */
    @Override
    public Foo clone() {
        Foo foo = null;
        try {
            foo = (Foo) super.clone();
            foo.setBar(bar.clone());
        } catch (CloneNotSupportedException e) {
            // won't happen
        }
        return foo;
    }
}
