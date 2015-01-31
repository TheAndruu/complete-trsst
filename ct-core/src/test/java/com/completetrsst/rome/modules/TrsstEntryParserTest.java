package com.completetrsst.rome.modules;

import static com.completetrsst.constants.Namespaces.ENCRYPT_NAMESPACE;
import static com.completetrsst.constants.Namespaces.SIGNATURE_NAMESPACE;
import static com.completetrsst.constants.Namespaces.TRSST_NAMESPACE;
import static com.completetrsst.constants.Nodes.ENCRYPTED_DATA;
import static com.completetrsst.constants.Nodes.SIGNATURE;
import static com.completetrsst.constants.Nodes.TRSST_PREDECESSOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Before;
import org.junit.Test;

import com.completetrsst.constants.Namespaces;
import com.rometools.rome.feed.module.Module;

public class TrsstEntryParserTest {

    private TrsstEntryParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new TrsstEntryParser();
    }

    @Test
    public void testGetNamespaceUri() {
        assertEquals(parser.getNamespaceUri(), Namespaces.TRSST_XMLNS);
    }

    @Test
    public void testNoTrsstNodesMeansNotTrsstModule() {
        Element element = mock(Element.class);
        when(element.getChild(ENCRYPTED_DATA, ENCRYPT_NAMESPACE)).thenReturn(null);
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(null);
        when(element.getChild(TRSST_PREDECESSOR, TRSST_NAMESPACE)).thenReturn(null);
        
        Module module = parser.parse(element, Locale.US);
        assertNull(module);
    }

    @Test
    public void testParseDetectsEncryptedData() {
        Element element = mock(Element.class);

        // Only element on there is encrypted, should get trsst module
        when(element.getChild(ENCRYPTED_DATA, ENCRYPT_NAMESPACE)).thenReturn(mock(Element.class));
        TrsstEntryModule module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertTrue(module.isEncrypted());

        // other trsst node detected, but not encrypted, so encrypted should be null
        element = mock(Element.class);
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(mock(Element.class));
        module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertFalse(module.isEncrypted());
    }

    @Test
    public void testParseDetectsSignedData() {
        Element element = mock(Element.class);

        // Only element on there is signed, should get trsst module
        when(element.getChild(SIGNATURE, SIGNATURE_NAMESPACE)).thenReturn(mock(Element.class));
        TrsstEntryModule module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertTrue(module.isSigned());

        // other trsst node detected, but not signed, so signed should be null
        element = mock(Element.class);
        when(element.getChild(ENCRYPTED_DATA, ENCRYPT_NAMESPACE)).thenReturn(mock(Element.class));
        module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertFalse(module.isSigned());
    }

    @Test
    public void testParseDetectsPredecessorNode() {
        Element element = mock(Element.class);

        // Only element on there is signed, should get trsst module
        Element mockContent = mock(Element.class);
        when(element.getChild(TRSST_PREDECESSOR, TRSST_NAMESPACE)).thenReturn(mockContent);
        when(mockContent.getText()).thenReturn("content here");
        TrsstEntryModule module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("content here", module.getPredecessorValue());

        // other trsst node detected, but not signed, so signed should be null
        element = mock(Element.class);
        when(element.getChild(ENCRYPTED_DATA, ENCRYPT_NAMESPACE)).thenReturn(mock(Element.class));
        module = (TrsstEntryModule) parser.parse(element, Locale.US);
        assertNotNull(module);
        assertEquals("", module.getPredecessorValue());
    }
}
