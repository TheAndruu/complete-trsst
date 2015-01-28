package com.completetrsst.xml;

import static com.completetrsst.constants.Namespaces.ATOM_XMLNS;
import static com.completetrsst.constants.Nodes.ATOM_TITLE;
import static com.completetrsst.xml.TestUtil.PLAIN_ATOM_ENTRY;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TestUtilTest {

    @Test
    public void getElementsByTagName() throws Exception {
        Element entryElement = TestUtil.readDomFromFile(PLAIN_ATOM_ENTRY);
        Node test1 = entryElement.getOwnerDocument().createElementNS(ATOM_XMLNS, "test");
        Text firstGuy = entryElement.getOwnerDocument().createTextNode("first guy");
        Node test2 = entryElement.getOwnerDocument().createElementNS(ATOM_XMLNS, "test");
        Node secondGuy = entryElement.getOwnerDocument().createTextNode("second guy");
        test1.appendChild(firstGuy);
        test2.appendChild(secondGuy);
        entryElement.appendChild(test1);
        entryElement.appendChild(test2);

        List<Node> titleNodes = TestUtil.getElementsByTagName(entryElement, ATOM_XMLNS, ATOM_TITLE);
        assertEquals(1, titleNodes.size());

        List<Node> testNodes = TestUtil.getElementsByTagName(entryElement, ATOM_XMLNS, "test");
        assertEquals(2, testNodes.size());
        assertEquals("first guy", testNodes.get(0).getTextContent());
        assertEquals("second guy", testNodes.get(1).getTextContent());
    }
}
