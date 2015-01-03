package com.completetrsst.rome;

import java.io.Serializable;

public class Bar implements Serializable, Cloneable {

    private static final long serialVersionUID = -4797497348596936855L;

    private String item;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    /** Performs a deep clone */
    @Override
    public Bar clone() {
        Bar bar = null;
        try {
            bar = (Bar) super.clone();
            bar.setItem(getItem());
        } catch (CloneNotSupportedException e) {
            // won't happen
        }
        return bar;
    }
}
