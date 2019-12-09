/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.widgets.browser.data.basic;

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
