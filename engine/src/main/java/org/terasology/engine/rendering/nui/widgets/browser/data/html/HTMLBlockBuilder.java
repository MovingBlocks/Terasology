// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.xml.sax.Attributes;

public interface HTMLBlockBuilder {
    /**
     * Signifies that a start tag has been encountered. This method should return <code>false</code> if an unknown tag
     * has been passed.
     *
     * @param tag
     * @param attributes
     * @return If this tag has been recognized by this HTMLBlockBuilder
     * @throws HTMLParseException
     */
    boolean startTag(String tag, Attributes attributes) throws HTMLParseException;

    /**
     * Signifies that text has been encountered.
     *
     * @param text
     * @throws HTMLParseException
     */
    void text(String text) throws HTMLParseException;

    /**
     * Signifies that an end tag has been encountered.
     *
     * @param tag
     * @throws HTMLParseException
     */
    void endTag(String tag) throws HTMLParseException;

    /**
     * Builds a resulting paragraph.
     *
     * @return
     */
    ParagraphData build();
}
