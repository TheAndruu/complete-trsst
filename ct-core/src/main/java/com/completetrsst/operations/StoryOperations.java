package com.completetrsst.operations;

import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureException;

import com.completetrsst.model.SignedEntry;

public interface StoryOperations {

    public void create(String publisherId, SignedEntry story);

    public List<String> getStories(String publisherId);

    public String publishSignedEntry(String publisherId, String signedXml) throws XMLSignatureException, IllegalArgumentException;
}
