// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HTMLLikeParserTest {
    @Test
    public void testUnencodeUnsupportedEntities() {
        Assertions.assertThrows(IllegalArgumentException.class,
                ()-> HTMLLikeParser.unencodeHTMLLike("&invalid;"));
    }
}
