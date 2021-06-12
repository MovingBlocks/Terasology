// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering;

import org.junit.jupiter.api.Test;
import org.terasology.nui.FontUnderline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FontUnderlineTest {

    private static final char START_UNDERLINE = 0xF001;
    private static final char END_UNDERLINE = 0xF002;

    @Test
    public void testStartUnderline() {
        assertTrue(FontUnderline.isValid(START_UNDERLINE));
    }

    @Test
    public void testEndUnderline() {
        assertTrue(FontUnderline.isValid(END_UNDERLINE));
    }

    @Test
    public void testInvalidUnderline() {
        char invalidUnderline = 0xF003;
        assertFalse(FontUnderline.isValid(invalidUnderline));
    }

    @Test
    public void testMarkUnderlined() {
        String testString = "string";
        assertTrue(FontUnderline.markUnderlined(testString).equals(START_UNDERLINE + testString + END_UNDERLINE));
    }
}
