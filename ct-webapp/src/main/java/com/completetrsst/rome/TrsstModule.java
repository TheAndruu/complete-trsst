package com.completetrsst.rome;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface TrsstModule extends Module, CopyFrom {

	public static final String URI = "http://trsst.com/spec/0.1";

	public boolean getIsSigned();
	public void setIsSigned(boolean isSigned);
}