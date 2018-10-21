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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class HTMLLikeParserParameterizedTest {
    private final String encodedText;
    private final String unencodedText;

    public HTMLLikeParserParameterizedTest(String encodedText, String unencodedText) {
        this.encodedText = encodedText;
        this.unencodedText = unencodedText;
    }

    @Test
    public void testEncodeHTMLLike() {
        String actual = HTMLLikeParser.encodeHTMLLike(unencodedText);

        assertEquals(encodedText, actual);
    }

    @Test
    public void testUnencodeHTMLLike() {
        String actual = HTMLLikeParser.unencodeHTMLLike(encodedText);

        assertEquals(unencodedText, actual);
    }

    @Parameters
    public static Collection data() {
        Object[][] data = new Object[][]{
                {"&amp;", "&"},
                {"&lt;", "<"},
                {"&gt;", ">"},
                {"&lt;&gt;&amp;", "<>&"},
                {"Leading text &lt;&gt;&amp;", "Leading text <>&"},
                {"&lt;&gt;&amp; trailing text", "<>& trailing text"},
                {"&lt;bold&gt;MovingBlocks &amp; Terasology&lt;/bold&gt;", "<bold>MovingBlocks & Terasology</bold>"}
        };

        return Arrays.asList(data);
    }
}
