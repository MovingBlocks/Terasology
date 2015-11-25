/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.widgets.browser.data.html;

import org.junit.Test;
import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.FlowParagraphData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HTMLParserTest {
    private HTMLParser htmlParser = new HTMLParser((name, bold, italic) -> null);

    @Test
    public void parseEmptyDocument() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body></body>");
        assertEquals(0, documentData.getParagraphs().size());
    }

    @Test(expected = HTMLParseException.class)
    public void parseUnfinishedBody() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body>");
    }

    @Test
    public void parseSimpleParagraph() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body><p>Text</p></body>");
        assertEquals(1, documentData.getParagraphs().size());
        ParagraphData paragraph = documentData.getParagraphs().iterator().next();
        assertTrue(paragraph instanceof FlowParagraphData);
    }

    @Test
    public void parseTwoParagraphs() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body><p>Text</p><p>Second</p></body>");
        assertEquals(2, documentData.getParagraphs().size());
    }

    @Test(expected = HTMLParseException.class)
    public void parseUnfinishedParagraph() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body><p>Text</body>");
    }

    @Test(expected = HTMLParseException.class)
    public void parseTextOutsideParagraph() throws IOException, SAXException, ParserConfigurationException {
        DocumentData documentData = htmlParser.parseHTMLDocument("<body>Text</body>");
    }
}
