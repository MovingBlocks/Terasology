// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.browser.data.html;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.FlowParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HTMLParserTest {
    private HTMLParser htmlParser = new HTMLParser((name, bold) -> null);

    @Test
    public void testParseEmptyDocument() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body></body>");
        assertEquals(0, documentData.getParagraphs().size());
    }

    @Test
    public void testParseUnfinishedBody() throws IOException, SAXException, ParserConfigurationException {
        Assertions.assertThrows(HTMLParseException.class,
                ()-> htmlParser.parseHTMLDocument("<body>"));
    }

    @Test
    public void testParseSimpleParagraph() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body><p>Text</p></body>");
        assertEquals(1, documentData.getParagraphs().size());
        ParagraphData paragraph = documentData.getParagraphs().iterator().next();
        assertTrue(paragraph instanceof FlowParagraphData);
    }

    @Test
    public void testParseTwoParagraphs() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body><p>Text</p><p>Second</p></body>");
        assertEquals(2, documentData.getParagraphs().size());
    }

    @Test
    public void testParseUnfinishedParagraph() throws IOException, SAXException, ParserConfigurationException {
        Assertions.assertThrows(HTMLParseException.class,
                ()-> htmlParser.parseHTMLDocument("<body><p>Text</body>"));
    }

    @Test
    public void testParseTextOutsideParagraph() throws IOException, SAXException, ParserConfigurationException {
        Assertions.assertThrows(HTMLParseException.class,
                ()-> htmlParser.parseHTMLDocument("<body>Text</body>"));
    }
}
