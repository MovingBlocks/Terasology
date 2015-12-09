/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class ColorTest {

    @Test
    public void colorToHash() {
        assertEquals("010A3CFF", new Color(1, 10, 60, 255).toHex());
    }

    @Test
    public void getSetRed() {
        Color color = new Color(1, 10, 60, 255);
        assertEquals(1, color.r());
        color = color.alterRed(72);
        assertEquals(72, color.r());
    }

    @Test
    public void getSetGreen() {
        Color color = new Color(1, 10, 60, 255);
        assertEquals(10, color.g());
        color = color.alterGreen(72);
        assertEquals(72, color.g());
    }

    @Test
    public void getSetBlue() {
        Color color = new Color(1, 10, 60, 255);
        assertEquals(60, color.b());
        color = color.alterBlue(72);
        assertEquals(72, color.b());
    }

    @Test
    public void getSetAlpha() {
        Color color = new Color(1, 10, 60, 255);
        assertEquals(255, color.a());
        color = color.alterAlpha(72);
        assertEquals(72, color.a());
    }
}
