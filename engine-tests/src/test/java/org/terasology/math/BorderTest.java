// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.joml.Rectanglei;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.nui.Border;
import org.terasology.nui.util.RectUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BorderTest {
    Border border;

    @BeforeEach
    public void initBorder() {
        border = new Border(10, 10, 10, 10);
    }

    @Test
    public void shrinkSameBorderAndRegionSize() {
        assertEquals(border.shrink(JomlUtil.from(Rect2i.createFromMinAndSize(10, 10, 10, 10))), JomlUtil.from(Rect2i.EMPTY));
    }

    @Test
    public void shrinkBorder() {
        assertEquals(border.shrink(JomlUtil.from(Rect2i.createFromMinAndSize(30, 30, 30, 30))),
                JomlUtil.from(Rect2i.createFromMinAndSize(40, 40, 10, 10)));
    }

    @Test
    public void shrinkVector() {
        assertEquals(border.shrink(JomlUtil.from(new Vector2i(10, 10))), JomlUtil.from(new Vector2i(-10, -10)));
    }

    @Test
    public void getTotals() {
        assertEquals(border.getTotals(), JomlUtil.from(new Vector2i(20, 20)));
    }

    @Test
    public void growVector() {
        assertEquals(border.grow(JomlUtil.from(new Vector2i(10, 10))), JomlUtil.from(new Vector2i(30, 30)));
    }

    @Test
    public void growVectorMax() {
        assertEquals(border.grow(JomlUtil.from(new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE))), JomlUtil.from(new Vector2i(
            Integer.MAX_VALUE, Integer.MAX_VALUE)));
    }

    @Test
    public void growBorder() {
        assertEquals(border.grow(JomlUtil.from(Rect2i.createFromMinAndSize(30, 30, 30, 30))),
                JomlUtil.from(Rect2i.createFromMinAndSize(20, 20, 50, 50)));
    }

    @Test
    public void growBorderMax() {
        assertEquals(
                RectUtility.createFromMinAndSize(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE),
                border.grow(RectUtility.createFromMinAndSize(10, 10, Integer.MAX_VALUE, Integer.MAX_VALUE)),
                "Growing border should be capped at Integer.MAX_VALUE");
    }
}
