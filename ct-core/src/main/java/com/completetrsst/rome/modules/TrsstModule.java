package com.completetrsst.rome.modules;

import java.security.KeyPair;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

// Get rid of the rome module - working directly with the API instead
public interface TrsstModule extends Module, CopyFrom {

	public static final String URI = "http://trsst.com/spec/0.1";

	public boolean getIsSigned();

	public void setIsSigned(boolean isSigned);

	public KeyPair getKeyPair();
	public void setKeyPair(KeyPair keyPair);
}