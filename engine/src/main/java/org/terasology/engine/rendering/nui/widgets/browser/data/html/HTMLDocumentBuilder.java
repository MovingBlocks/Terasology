// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.xml.sax.Attributes;

public interface HTMLDocumentBuilder {
    /**
     * Signifies that a start tag has been encountered. Returns a HTMLParagraphBuilder that should be receiving all
     * events.
     *
     * @param tag
     * @param attributes
     * @return
     * @throws HTMLParseException
     */
    HTMLBlockBuilder startTag(String tag, Attributes attributes) throws HTMLParseException;

    HTMLDocument createDocument();
}
