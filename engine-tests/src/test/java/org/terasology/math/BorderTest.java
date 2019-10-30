/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.math;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BorderTest {
  Border border;

  @Before
  public void initBorder() {
    border = new Border(10, 10, 10, 10);
  }

  @Test
  public void shrinkSameBorderAndRegionSize() {
    assertEquals(border.shrink(Rect2i.createFromMinAndSize(10, 10, 10, 10)), Rect2i.EMPTY);
  }

  @Test
  public void shrinkBorder() {
    assertEquals(border.shrink(Rect2i.createFromMinAndSize(30, 30, 30, 30)), Rect2i.createFromMinAndSize(40, 40, 10, 10));
  }

  @Test
  public void shrinkVector() {
    assertEquals(border.shrink(new Vector2i(10, 10)), new Vector2i(-10, -10));
  }

  @Test
  public void getTotals() {
    assertEquals(border.getTotals(), new Vector2i(20, 20));
  }

  @Test
  public void growVector() {
    assertEquals(border.grow(new Vector2i(10, 10)), new Vector2i(30, 30));
  }

  @Test
  public void growVectorMAX() {
    assertEquals(border.grow(new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE)), new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

  @Test
  public void growBorder() {
    assertEquals(border.grow(Rect2i.createFromMinAndSize(30, 30, 30, 30)), Rect2i.createFromMinAndSize(20, 20, 50, 50));
  }

  @Test
  public void growBorderMAX() {
    assertEquals(border.grow(Rect2i.createFromMinAndSize(10, 10, Integer.MAX_VALUE, Integer.MAX_VALUE)), Rect2i.createFromMinAndSize(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
  }
}