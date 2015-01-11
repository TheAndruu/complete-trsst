package com.completetrsst.rome.modules;

import java.security.KeyPair;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.ModuleImpl;

public class TrsstSignatureModule extends ModuleImpl implements TrsstModule {

	private static final long serialVersionUID = -5045125952451910984L;

	private boolean isSigned;
	
	private KeyPair keyPair;

	public TrsstSignatureModule() {
		super(TrsstSignatureModule.class, TrsstModule.URI);
	}

	@Override
	public Class<? extends CopyFrom> getInterface() {
		return TrsstModule.class;
	}

	// must do a deep copy
	@Override
	public void copyFrom(CopyFrom obj) {
		TrsstModule sm = (TrsstModule) obj;
		setIsSigned(sm.getIsSigned());
	}

	@Override
	public boolean getIsSigned() {
		return isSigned;
	}

	@Override
	public void setIsSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}

	@Override
	public KeyPair getKeyPair() {
		return keyPair;
	}

	@Override
	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

}