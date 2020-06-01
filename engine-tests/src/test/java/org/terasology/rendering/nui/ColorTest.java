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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.nui.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorTest {
    private Color color;

    @BeforeEach
    public void setUp() {
        color = new Color(1, 10, 60, 255);
    }

    @Test
    public void testColorToHash() {
        assertEquals("010A3CFF", color.toHex());
    }

    @Test
    public void testGetRed() {
        assertEquals(1, color.r());
    }

    @Test
    public void testAlterRed() {
        color = color.alterRed(72);
        assertEquals(72, color.r());
    }

    @Test
    public void testAlterRedThrowsWhenColorLessThanLowerBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterRed(-1));
    }

    @Test
    public void testAlterRedThrowsWhenColorLargerThanUpperBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterRed(256));
    }

    @Test
    public void testGetGreen() {
        assertEquals(10, color.g());
    }

    @Test
    public void testAlterGreen() {
        color = color.alterGreen(72);
        assertEquals(72, color.g());
    }

    @Test
    public void testAlterGreenThrowsWhenColorLessThanLowerBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterGreen(-1));
    }

    @Test
    public void testAlterGreenThrowsWhenColorLargerThanUpperBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterGreen(256));
    }

    @Test
    public void testGetBlue() {
        assertEquals(60, color.b());
    }

    @Test
    public void testAlterBlue() {
        color = color.alterBlue(72);
        assertEquals(72, color.b());
    }

    @Test
    public void testAlterBlueThrowsWhenColorLessThanLowerBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterBlue(-1));
    }

    @Test
    public void testAlterBlueThrowsWhenColorLargerThanUpperBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterBlue(256));
    }

    @Test
    public void testGetAlpha() {
        assertEquals(255, color.a());
    }

    @Test
    public void testAlterAlpha() {
        color = color.alterAlpha(72);
        assertEquals(72, color.a());
    }

    @Test
    public void testAlterAlphaThrowsWhenColorLessThanLowerBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> color.alterAlpha(-1));
    }

    @Test
    public void testAlterAlphaThrowsWhenColorLargerThanUpperBound() {
        Assertions.assertThrows(IllegalArgumentException.class,
                ()->color.alterAlpha(256));
    }
}
