// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class HTMLLikeParserParameterizedTest {

    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeHTMLLike(String encodedText, String unencodedText) {
        String actual = HTMLLikeParser.encodeHTMLLike(unencodedText);

        assertEquals(encodedText, actual);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUnencodeHTMLLike(String encodedText, String unencodedText) {
        String actual = HTMLLikeParser.unencodeHTMLLike(encodedText);

        assertEquals(unencodedText, actual);
    }

    public static Stream<? extends Arguments> data() {
        return Stream.of(
                arguments("&amp;", "&"),
                arguments("&lt;", "<"),
                arguments("&gt;", ">"),
                arguments("&lt;&gt;&amp;", "<>&"),
                arguments("Leading text &lt;&gt;&amp;", "Leading text <>&"),
                arguments("&lt;&gt;&amp; trailing text", "<>& trailing text"),
                arguments("&lt;bold&gt;MovingBlocks &amp; Terasology&lt;/bold&gt;", "<bold>MovingBlocks & Terasology</bold>")
        );
    }
}
