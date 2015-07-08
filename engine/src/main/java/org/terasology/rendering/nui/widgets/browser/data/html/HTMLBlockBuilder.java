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

import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
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
