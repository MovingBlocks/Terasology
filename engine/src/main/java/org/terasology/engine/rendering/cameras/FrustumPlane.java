// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.cameras;

/**
 * Represents a plane of a view frustum.
 *
 */
public class FrustumPlane {

    private float a;
    private float b;
    private float c;
    private float d;

    /**
     * Init. a new view frustum with the default values in place.
     */
    public FrustumPlane() {
        // Do nothing.
    }

    /**
     * Init. a new view frustum with a given plane equation.
     * ax + by + cy + d = 0
     */
    public FrustumPlane(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * Normalizes this plane.
     */
    public void normalize() {
        double t = Math.sqrt((double) a * a + (double) b * b + (double) c * c);
        a /= t;
        b /= t;
        c /= t;
        d /= t;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    void setC(float c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }
}
