package com.completetrsst.crypto;

import java.security.Key;

import javax.xml.crypto.KeySelectorResult;

public class SimpleKeySelectorResult implements KeySelectorResult {
	private Key key;

	public SimpleKeySelectorResult(Key key) {
		this.key = key;
	}

	@Override
	public Key getKey() {
		return key;
	}
}