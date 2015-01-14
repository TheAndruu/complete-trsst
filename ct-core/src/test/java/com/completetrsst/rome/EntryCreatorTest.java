package com.completetrsst.rome;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.xml.SignatureUtil;
import com.completetrsst.xml.XmlUtil;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

public class EntryCreatorTest {
    private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
    private Entry entry;
    
    private static final String ENTRY_TITLE = "my title";
    
    @Before
    public void init() {
        entry = EntryCreator.create(ENTRY_TITLE);
    }

    @Test
    public void testNewEntryId() {
        String id = EntryCreator.newEntryId();
        assertTrue(id.startsWith(EntryCreator.ENTRY_ID_PREFIX));
        String justUuid = id.substring(EntryCreator.ENTRY_ID_PREFIX.length());
        // Type 4 UUID
        assertTrue(justUuid.substring(13, 15).equals("-4"));
        assertTrue(justUuid.substring(18, 19).equals("-"));
        String y = justUuid.substring(19, 20);
        List<String> allowedY = Arrays.asList("8", "9", "A", "B", "a", "b");
        assertTrue(allowedY.contains(y));
    }

    @Test
    public void createEntry() {
        assertEquals(ENTRY_TITLE, entry.getTitle());
        assertTrue(entry.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
        assertTrue(entry.getId().startsWith(EntryCreator.ENTRY_ID_PREFIX));
    }
    
    @Test
	public void testSigningElementByItself() throws Exception {
	    org.jdom2.Element jdom = EntryCreator.toJdom(entry);
	    Element dom = XmlUtil.toDom(jdom);
	    SignatureUtil.signElement(dom, keyPair);
	    // Twice to ensure nothing is detached improperly from the operation
	    assertTrue(SignatureUtil.verifySignature(dom));
	    assertTrue(SignatureUtil.verifySignature(dom));
	    
	    // convert to string and back
	    dom = XmlUtil.toDom(XmlUtil.serializeDom(dom));
	    assertTrue(SignatureUtil.verifySignature(dom));
	}
    
    @Test
    public void testEditedSignedEntryFailsValidation() throws Exception {
        
        org.jdom2.Element jdom = EntryCreator.toJdom(entry);
        Element dom = XmlUtil.toDom(jdom);
        SignatureUtil.signElement(dom, keyPair);
        
        // Ensure it works originally
        assertTrue(SignatureUtil.verifySignature(dom));
        
        Node titleNode = dom.getElementsByTagName("title").item(0);
        assertEquals(ENTRY_TITLE, titleNode.getTextContent());
        titleNode.setTextContent("new title");
        // ensure the set operation impacted our DOM element
        assertEquals("new title", dom.getElementsByTagName("title").item(0).getTextContent());
        // and verify it fails this time
        assertFalse(SignatureUtil.verifySignature(dom));
    }
    
    
    // TODO: Look into this guy:
    // http://stackoverflow.com/questions/12678647/reference-uri-error-creating-digital-signature-for-a-specific-xml-element-node
    
    // TODO: Looks like entries have to be signed independently BEFORE added to any feed
    // and then tested separately (as their own document?) later
    
    @Test
    public void testSigningElementInsideFeed() throws Exception {
        Feed feed = FeedCreator.createFor(keyPair);

        
        
        org.jdom2.Element jdom = EntryCreator.toJdom(entry);
        Element dom = XmlUtil.toDom(jdom);
        SignatureUtil.signElement(dom, keyPair);
        // Twice to ensure nothing is detached improperly from the operation
        assertTrue(SignatureUtil.verifySignature(dom));
        assertTrue(SignatureUtil.verifySignature(dom));
        
        // convert to string and back
        dom = XmlUtil.toDom(XmlUtil.serializeDom(dom));
        assertTrue(SignatureUtil.verifySignature(dom));
    }
}
