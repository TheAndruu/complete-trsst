package com.completetrsst.atom;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.completetrsst.xml.TestUtil;

public class AtomVerifierTest {

    private static AtomVerifier verifier;

    @Before
    public void init() {
        verifier = new AtomVerifier();
    }
    
    @Test 
    public void isFeedVerified() throws Exception {
    	Element dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_VALID);
    	assertTrue(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
    	assertTrue(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_VALID);
    	assertFalse(verifier.isFeedVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_TAMPERED);
    	assertFalse(verifier.isFeedVerified(dom));
    	
    	
    }
    @Test 
    public void areEntriesVerified() throws Exception {
    	Element dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_VALID);
    	assertTrue(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_VALID_ENTRY_TAMPERED);
    	assertFalse(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_VALID);
    	assertTrue(verifier.areEntriesVerified(dom));
    	
    	dom = TestUtil.readDomFromFile(TestUtil.FEED_TAMPERED_ENTRY_TAMPERED);
    	assertFalse(verifier.areEntriesVerified(dom));
    }
}
