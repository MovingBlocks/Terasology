/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.datastructures;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FrustumPlane {

    private double a, b, c, d;

    public FrustumPlane() {
        // Do nothing.
    }

    public FrustumPlane(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }


    public double getC() {
        return c;
    }

    void setC(double c) {
        this.c = c;
    }


    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public void normalize() {
        double t = Math.sqrt(a * a + b * b + c * c);
        a /= t;
        b /= t;
        c /= t;
        d /= t;
    }
}
