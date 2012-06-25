package org.terasology.math;

import java.util.ArrayList;
import java.util.List;

/**
 * 2D Rectangle
 */
public class Rect2i {
    // position
    public int x;
    public int y;

    // size
    public int w;
    public int h;

    public Rect2i(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;

        this.w = w;
        this.h = h;
    }

    // a - b
    // @pre a and b have the same size
    // @return list of disjunct rects building the subtraction result (eg. L-shape)
    public static List<Rect2i> subtractEqualsSized(Rect2i a, Rect2i b) {
        ArrayList<Rect2i> result = new ArrayList<Rect2i>();

        boolean overlap = a.overlaps(b);
        boolean equal = (a.x == b.x) && (a.y == b.y);

        if (equal) {
            // empty list
            return result;
        }

        if (!overlap) {
            // a does not change
            result.add(a);
            return result;
        }

        // invariant: equal size and overlapping and not equal position

        int splitCenterX = (a.x < b.x) ? b.minX() : b.maxX();
        int splitCenterY = (a.y < b.y) ? b.minY() : b.maxY();

        subtractEqualsSizedHelper(a.minX(), a.minY(), b, splitCenterX, splitCenterY, result);
        subtractEqualsSizedHelper(a.maxX(), a.minY(), b, splitCenterX, splitCenterY, result);
        subtractEqualsSizedHelper(a.minX(), a.maxY(), b, splitCenterX, splitCenterY, result);
        subtractEqualsSizedHelper(a.maxX(), a.maxY(), b, splitCenterX, splitCenterY, result);

        return result;
    }

    public String toString() {
        return String.format("x=%d y=%d w=%d h=%d", x, y, w, h);
    }

    private static void subtractEqualsSizedHelper(int x, int y, Rect2i b, int splitCenterX, int splitCenterY, ArrayList<Rect2i> result) {
        if (!b.contains(x, y) && x != splitCenterX && y != splitCenterY) {
            Rect2i candidate = createRectSpanning2Points(x, y, splitCenterX, splitCenterY);

            if (candidate.w * candidate.h > 0) {
                result.add(candidate);
            }
        }
    }

    // @return minimum rect that contains both points
    public static Rect2i createRectSpanning2Points(int x0, int y0, int x1, int y1) {
        int x = Math.min(x0, x1);
        int y = Math.min(y0, y1);

        int w = Math.max(x0 - x, x1 - x);
        int h = Math.max(y0 - y, y1 - y);

        return new Rect2i(x, y, w, h);
    }

    /**
     * Returns true if the Rect contains the given point.
     *
     * @param x The x part of point to check for inclusion
     * @param y The y part of point to check for inclusion
     * @return True if containing
     */
    public boolean contains(int x, int y) {
        return !(maxX() < x || minX() > x) &&
                !(maxY() < y || minY() > y);
    }

    /**
     * Returns true if this Rect overlaps the given Rect.
     *
     * @param b The Rect to check for overlapping
     * @return True if overlapping
     */
    public boolean overlaps(Rect2i b) {
        return !(maxX() < b.minX() || minX() > b.maxX()) &&
                !(maxY() < b.minY() || minY() > b.maxY());
    }

    public int maxX() {
        return x + w;
    }

    public int minX() {
        return x;
    }


    public int maxY() {
        return y + h;
    }

    public int minY() {
        return y;
    }

    public int area() {
        return w * h;
    }
}
