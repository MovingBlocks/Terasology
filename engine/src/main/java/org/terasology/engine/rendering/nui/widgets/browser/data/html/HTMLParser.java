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

import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.html.basic.DefaultHTMLDocumentBuilderFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class HTMLParser {
    private HTMLDocumentBuilderFactory htmlDocumentBuilderFactory;

    public HTMLParser(HTMLFontResolver htmlFontResolver) {
        this(new DefaultHTMLDocumentBuilderFactory(htmlFontResolver));
    }

    public HTMLParser(HTMLDocumentBuilderFactory htmlDocumentBuilderFactory) {
        this.htmlDocumentBuilderFactory = htmlDocumentBuilderFactory;
    }

    public DocumentData parseHTMLDocument(String document) throws HTMLParseException {
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(false);
            saxParserFactory.setValidating(false);
            saxParserFactory.setXIncludeAware(false);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            HTMLDocumentHandler dh = new HTMLDocumentHandler(htmlDocumentBuilderFactory);
            saxParser.parse(new ByteArrayInputStream(document.getBytes("UTF-8")), dh);
            return dh.getDocument();
        } catch (ParserConfigurationException | SAXException | IOException exp) {
            throw new HTMLParseException(exp);
        }
    }
}
