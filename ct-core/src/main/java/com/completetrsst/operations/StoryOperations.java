package com.completetrsst.operations;

import javax.xml.crypto.dsig.XMLSignatureException;

import com.completetrsst.model.SignedEntry;

public interface StoryOperations {

	public String readFeed(String publisherId);

	public String publishSignedContent(String signedXml) throws XMLSignatureException, IllegalArgumentException;
}
