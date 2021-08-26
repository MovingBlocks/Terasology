// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link TemplateEngineImpl} class.
 */
public class TemplateEngineTest {

    @Test
    public void testSimple() {
        TemplateEngineImpl engine = new TemplateEngineImpl(text -> "bla");

        assertEquals("I like bla!", engine.transform("I like ${text}!"));
    }

    @Test
    public void testEmpty() {
        TemplateEngineImpl engine = new TemplateEngineImpl(text -> null);

        assertEquals("I like !", engine.transform("I like ${text}!"));
    }

    @Test
    public void testTwo() {
        TemplateEngineImpl engine = new TemplateEngineImpl(ImmutableMap.of("text1", "bla", "text2", "blubb")::get);

        assertEquals("I like bla, but not blubb!",
                engine.transform("I like ${text1}, but not ${text2}!"));
    }

}
