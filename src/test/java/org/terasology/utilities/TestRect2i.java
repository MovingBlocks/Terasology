package org.terasology.utilities;


import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;

import java.util.List;

public class TestRect2i extends junit.framework.TestCase {
    private Rect2i findAnyRectWithArea(List<Rect2i> rects, int area) {
        for(Rect2i r : rects) {
            if (r.area() == area)return r;
        }

        return null;
    }

    public void testSubtraction0() {
        Rect2i a = new Rect2i(1,2, 3,3);
        Rect2i b = new Rect2i(0,4, 3,3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(3, sub.size());

        int area = 0;
        for(Rect2i r : sub)
        {
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

    public void testSubtraction1() {
        Rect2i a = new Rect2i(1,2, 3,3);
        Rect2i b = new Rect2i(3,2, 3,3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(1, sub.size());

        int area = 0;
        for(Rect2i r : sub)
        {
            area += r.area();
        }

        assertEquals(6, area);

        Rect2i r0 = findAnyRectWithArea(sub, 6);
        assertEquals(1, r0.x);
        assertEquals(2, r0.y);
    }

    public void testSubtraction2() {
        Rect2i a = new Rect2i(3,2, 3,3);
        Rect2i b = new Rect2i(1,2, 3,3);

        List<Rect2i> sub = Rect2i.subtractEqualsSized(a, b);

        assertEquals(1, sub.size());

        int area = 0;
        for(Rect2i r : sub)
        {
            area += r.area();
        }

        assertEquals(6, area);

        Rect2i r0 = findAnyRectWithArea(sub, 6);
        assertEquals(4, r0.x);
        assertEquals(2, r0.y);
    }
}