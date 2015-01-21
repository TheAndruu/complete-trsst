package com.completetrsst.operations;

import javax.xml.crypto.dsig.XMLSignatureException;

public interface TrsstOperations {

	public String readFeed(String publisherId);

	public String publishSignedContent(String signedXml) throws XMLSignatureException, IllegalArgumentException;

	public String searchEntries(String searchString);
}
