package com.completetrsst.rome;

import java.security.KeyPair;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

public interface TrsstModule extends Module, CopyFrom {

	public static final String URI = "http://trsst.com/spec/0.1";

	public boolean getIsSigned();

	public void setIsSigned(boolean isSigned);

	// TODO: Do these belong in the module or elsewhere?
	public KeyPair getKeyPair();
	void setKeyPair(KeyPair keyPair);
}