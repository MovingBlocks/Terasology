// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.DefaultHTMLDocumentBuilderFactory;
import org.terasology.engine.rendering.nui.widgets.browser.data.DocumentData;
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
