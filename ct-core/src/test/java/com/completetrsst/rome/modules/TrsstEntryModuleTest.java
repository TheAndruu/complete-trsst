package com.completetrsst.rome.modules;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TrsstEntryModuleTest {

    private TrsstEntryModule module;

    @Before
    public void init() {
        module = new TrsstEntryModule();
    }

    @Test
    public void testGetInterface() {
        assertEquals(EntryModule.class, module.getInterface());
    }

    @Test
    public void testCopyFrom() {
        module.setIsEncrypted(false);
        module.setIsSigned(false);
        module.setPredecessorValue("orig pred");

        assertFalse(module.isEncrypted());
        assertFalse(module.isSigned());
        assertEquals("orig pred", module.getPredecessorValue());

        TrsstEntryModule newModule = new TrsstEntryModule();
        newModule.setIsEncrypted(true);
        newModule.setIsSigned(true);
        newModule.setPredecessorValue("new pred");

        module.copyFrom(newModule);

        assertTrue(module.isEncrypted());
        assertTrue(module.isSigned());
        assertEquals("new pred", module.getPredecessorValue());
    }

    @Test
    public void testSetGetPredecessorValue() {
        module.setPredecessorValue("new guy");
        assertEquals("new guy", module.getPredecessorValue());
    }

    @Test
    public void testSetGetIsSigned() {
        module.setIsSigned(true);
        assertTrue(module.isSigned());

        module.setIsSigned(false);
        assertFalse(module.isSigned());
    }

    @Test
    public void testSetGetIsEncrypted() {
        module.setIsEncrypted(true);
        assertTrue(module.isEncrypted());

        module.setIsEncrypted(false);
        assertFalse(module.isEncrypted());
    }

}
