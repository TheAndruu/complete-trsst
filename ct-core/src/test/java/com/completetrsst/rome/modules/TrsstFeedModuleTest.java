package com.completetrsst.rome.modules;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TrsstFeedModuleTest {

    private TrsstFeedModule module;

    @Before
    public void init() {
        module = new TrsstFeedModule();
    }

    @Test
    public void testGetInterface() {
        assertEquals(FeedModule.class, module.getInterface());
    }

    @Test
    public void testCopyFrom() {
        module.setEncryptKey("asdf");
        module.setIsSigned(false);
        module.setSignKey("fdsa");

        assertFalse(module.isSigned());
        assertEquals("asdf", module.getEncryptKey());
        assertEquals("fdsa", module.getSignKey());

        TrsstFeedModule newModule = new TrsstFeedModule();
        newModule.setIsSigned(true);
        newModule.setEncryptKey("gggg");
        newModule.setSignKey("hhhh");

        module.copyFrom(newModule);

        assertTrue(module.isSigned());
        assertEquals("gggg", module.getEncryptKey());
        assertEquals("hhhh", module.getSignKey());
    }

    @Test
    public void testGetSetSignKey() {
        module.setSignKey("new guy2");
        assertEquals("new guy2", module.getSignKey());
    }

    @Test
    public void testGetSetEncryptKey() {
        module.setEncryptKey("new guy3");
        assertEquals("new guy3", module.getEncryptKey());
    }

    @Test
    public void testGetSetIsSigned() {
        module.setIsSigned(true);
        assertTrue(module.isSigned());

        module.setIsSigned(false);
        assertFalse(module.isSigned());
    }

}
