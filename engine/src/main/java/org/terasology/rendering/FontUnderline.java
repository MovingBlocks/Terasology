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

package org.terasology.rendering;

import org.terasology.module.sandbox.API;

/**
 * Defines a set of special characters that mark contents of a string to be underlined
 */
@API
public final class FontUnderline {
    private static final char START_UNDERLINE = 0xF001;
    private static final char END_UNDERLINE = 0xF002;

    private FontUnderline() {
        // avoid instantiation
    }

    /**
     * @param ch the character to test
     * @return true for the start or end underline indicator characters
     */
    public static boolean isValid(char ch) {
        return ch == START_UNDERLINE || ch == END_UNDERLINE;
    }


    /**
     * Returns a string with surrounded with the start/end underline characters.
     *
     * @param str the text
     * @return the encoded string
     */
    public static String markUnderlined(String str) {
        return START_UNDERLINE + str + END_UNDERLINE;
    }

    /**
     * @return the underline start character
     */
    public static char getStart() {
        return START_UNDERLINE;
    }

    /**
     * @return the underline end character
     */
    public static char getEnd() {
        return END_UNDERLINE;
    }

    /**
     * @param text The marked up text
     * @return the same text string, but without the underline information
     */
    public static String strip(String text) {

        StringBuffer sb = new StringBuffer(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (!isValid(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
