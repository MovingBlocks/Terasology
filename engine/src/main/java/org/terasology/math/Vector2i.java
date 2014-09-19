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

package org.terasology.math;

import com.google.common.base.Preconditions;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;

/**
 * A 2-element vector represented by signed integer x,y
 * coordinates.
 *
 * @author Immortius
 */
public class Vector2i {

    public int x;
    public int y;

    /**
     * Constructs and initializes a Vector2i from the specified
     * x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs and initializes a Vector2i from the array of length 2.
     *
     * @param values the array of length 2 containing x and y in order.
     */
    public Vector2i(int[] values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkArgument(values.length == 2);
        this.x = values[0];
        this.y = values[1];
    }

    /**
     * Constructs and initializes a Vector2i from the specified Vector2i.
     *
     * @param other the Vector2i containing the initialization x and y
     *              data.
     */
    public Vector2i(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Constructs and initializes a Vector2i to (0,0).
     */
    public Vector2i() {
    }

    /**
     * @return The equivalent Vector2f
     */
    public Vector2f toVector2f() {
        return new Vector2f(x, y);
    }

    /**
     * @return The equivalent Vector2d
     */
    public Vector2d toVector2d() {
        return new Vector2d(x, y);
    }

    /**
     * @return A <b>new</b> instance of (0, 0)
     */
    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }

    public int gridDistance(Vector2i other) {
        return Math.abs(other.x - x) + Math.abs(other.y - y);
    }

    public void absolute() {
        x = TeraMath.fastAbs(x);
        y = TeraMath.fastAbs(y);
    }

    public void absolute(Vector2i other) {
        this.x = TeraMath.fastAbs(other.x);
        this.y = TeraMath.fastAbs(other.y);
    }

    public void add(Vector2i other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void add(Vector2i a, Vector2i b) {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
    }

    public void clamp(int min, int max) {
        this.x = TeraMath.clamp(this.x, min, max);
        this.y = TeraMath.clamp(this.y, min, max);
    }

    public void clamp(int min, int max, Vector2i other) {
        this.x = TeraMath.clamp(other.x, min, max);
        this.y = TeraMath.clamp(other.y, min, max);
    }

    public void clampMax(int max) {
        this.x = Math.min(this.x, max);
        this.y = Math.min(this.y, max);
    }

    public void clampMax(int max, Vector2i other) {
        this.x = Math.min(other.x, max);
        this.y = Math.min(other.y, max);
    }

    public void clampMin(int min) {
        this.x = Math.max(this.x, min);
        this.y = Math.max(this.y, min);
    }

    public void clampMin(int min, Vector2i other) {
        this.x = Math.max(other.x, min);
        this.y = Math.max(other.y, min);
    }

    public void get(int[] out) {
        Preconditions.checkNotNull(out);
        Preconditions.checkArgument(out.length == 2);
        out[0] = x;
        out[1] = y;
    }

    public void get(Vector2i other) {
        other.x = x;
        other.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void negate() {
        this.x *= -1;
        this.y *= -1;
    }

    public void negate(Vector2i other) {
        this.x = -other.x;
        this.y = -other.y;
    }

    public void scale(int s) {
        this.x *= s;
        this.y *= s;
    }

    public void scale(int s, Vector2i other) {
        this.x = s * other.x;
        this.y = s * other.y;
    }

    public void scaleAdd(int s, Vector2i add) {
        this.x = s * this.x + add.x;
        this.y = s * this.y + add.y;
    }

    public void scaleAdd(int s, Vector2i a, Vector2i b) {
        this.x = s * a.x + b.x;
        this.y = s * a.y + b.y;
    }

    public void set(int[] values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkArgument(values.length == 2);
        this.x = values[0];
        this.y = values[1];
    }

    public void set(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void set(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void sub(Vector2i other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public void sub(Vector2i a, Vector2i b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector2i) {
            Vector2i other = (Vector2i) obj;
            return this.x == other.x && this.y == other.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash += (x - Integer.MIN_VALUE) & 0xFFFF;
        hash <<= 16;
        hash += (y - Integer.MIN_VALUE) & 0xFFFF;

        return hash;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

