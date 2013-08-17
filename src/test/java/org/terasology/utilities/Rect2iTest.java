/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.utilities;


import org.junit.Test;
import org.terasology.math.Rect2i;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Rect2iTest {

    private Rect2i findAnyRectWithArea(List<Rect2i> rects, int area) {
        for (Rect2i r : rects) {
            if (r.area() == area) {
                return r;
            }
        }

        return null;
    }

    @Test
    public void testSubtraction0() {
        Rect2i a = new Rect2i(1, 2, 3, 3);
        Rect2i b = new Rect2i(0, 4, 3, 3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(3, sub.size());

        int area = 0;
        for (Rect2i r : sub) {
            area += r.area();
        }

        assertEquals(7, area);

        Rect2i r0 = findAnyRectWithArea(sub, 4);
        assertEquals(1, r0.x);
        assertEquals(2, r0.y);

        Rect2i r1 = findAnyRectWithArea(sub, 2);
        assertEquals(3, r1.x);
        assertEquals(2, r1.y);

        Rect2i r2 = findAnyRectWithArea(sub, 1);
        assertEquals(3, r2.x);
        assertEquals(4, r2.y);
    }

    @Test
    public void testSubtraction1() {
        Rect2i a = new Rect2i(1, 2, 3, 3);
        Rect2i b = new Rect2i(3, 2, 3, 3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(1, sub.size());

        int area = 0;
        for (Rect2i r : sub) {
            area += r.area();
        }

        assertEquals(6, area);

        Rect2i r0 = findAnyRectWithArea(sub, 6);
        assertEquals(1, r0.x);
        assertEquals(2, r0.y);
    }

    @Test
    public void testSubtraction2() {
        Rect2i a = new Rect2i(3, 2, 3, 3);
        Rect2i b = new Rect2i(1, 2, 3, 3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(1, sub.size());

        int area = 0;
        for (Rect2i r : sub) {
            area += r.area();
        }

        assertEquals(6, area);

        Rect2i r0 = findAnyRectWithArea(sub, 6);
        assertEquals(4, r0.x);
        assertEquals(2, r0.y);
    }
}
