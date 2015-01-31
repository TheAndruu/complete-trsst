package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.SIGNATURE_NAMESPACE;
import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Nodes.SIGNATURE;
import static com.completetrsst.constants.Nodes.TRSST_ENCRYPT;
import static com.completetrsst.constants.Nodes.TRSST_SIGN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import com.completetrsst.constants.Namespaces;
import com.rometools.rome.feed.module.Module;

public class TrsstFeedParserTest {

    private TrsstFeedParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new TrsstFeedParser();
    }

    @Test
    public void testGetNamespaceUri() {
        assertEquals(parser.getNamespaceUri(), Namespaces.TRSST_XMLNS);
    }

    @Test
    public void testNoTrsstNodesMeansNotTrsstModule() {
        Element element = mock(Element.class);
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(null);
        when(element.getChild(TRSST_SIGN, TRSST_NAMESPACE)).thenReturn(null);
        when(element.getChild(TRSST_ENCRYPT, TRSST_NAMESPACE)).thenReturn(null);

        Module module = parser.parse(element, Locale.US);
        assertNull(module);
    }

    @Test
    public void testParseDetectsEncryptValue() {
        Element element = mock(Element.class);

        // Only element on there is encrypted, should get trsst module
        Element mockContent = mock(Element.class);
        when(element.getChild(TRSST_ENCRYPT, TRSST_NAMESPACE)).thenReturn(mockContent);
        when(mockContent.getText()).thenReturn("content 6");
        TrsstFeedModule module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("content 6", module.getEncryptKey());

        // other trsst node detected, but not encrypted, so encrypted should be null
        element = mock(Element.class);
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(mock(Element.class));
        module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("", module.getEncryptKey());
    }

    @Test
    public void testParseDetectsSignValue() {
        Element element = mock(Element.class);

        // Only element on there is encrypted, should get trsst module
        Element mockContent = mock(Element.class);
        when(element.getChild(TRSST_SIGN, TRSST_NAMESPACE)).thenReturn(mockContent);
        when(mockContent.getText()).thenReturn("content 7");
        TrsstFeedModule module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("content 7", module.getSignKey());

        // other trsst node detected, but not encrypted, so encrypted should be null
        element = mock(Element.class);
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(mock(Element.class));
        module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("", module.getSignKey());
    }

    @Test
    public void testParseDetectsSignature() {
        Element element = mock(Element.class);

        // Only element on there is signed, should get trsst module
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(mock(Element.class));
        TrsstFeedModule module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertTrue(module.isSigned());

        // other trsst node detected, but not signed, so signed should be null
        element = mock(Element.class);
        Element mockContent = mock(Element.class);
        when(element.getChild(TRSST_SIGN, TRSST_NAMESPACE)).thenReturn(mockContent);
        when(mockContent.getText()).thenReturn("content 8");
        module = (TrsstFeedModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertFalse(module.isSigned());
    }

}
