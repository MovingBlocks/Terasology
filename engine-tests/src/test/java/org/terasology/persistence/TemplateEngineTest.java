/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.persistence;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Tests the {@link TemplateEngineImpl} class.
 */
public class TemplateEngineTest {

    @Test
    public void simpleTest() {
        TemplateEngineImpl engine = new TemplateEngineImpl(text -> "bla");

        Assert.assertEquals("I like bla!", engine.transform("I like ${text}!"));
    }

    @Test
    public void testEmpty() {
        TemplateEngineImpl engine = new TemplateEngineImpl(text -> null);

        Assert.assertEquals("I like !", engine.transform("I like ${text}!"));
    }

    @Test
    public void testTwo() {
        TemplateEngineImpl engine = new TemplateEngineImpl(ImmutableMap.of("text1", "bla", "text2", "blubb")::get);

        Assert.assertEquals("I like bla, but not blubb!",
                engine.transform("I like ${text1}, but not ${text2}!"));
    }

}
