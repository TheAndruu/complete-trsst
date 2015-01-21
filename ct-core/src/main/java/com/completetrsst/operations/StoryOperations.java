package com.completetrsst.operations;

import javax.xml.crypto.dsig.XMLSignatureException;

public interface StoryOperations {

	public String readFeed(String publisherId);

	public String publishSignedContent(String signedXml) throws XMLSignatureException, IllegalArgumentException;
	
}
