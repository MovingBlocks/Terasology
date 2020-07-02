/*
 * The MIT License
 *
 * Copyright © 2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.terasology.engine.splash.graphics;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * This class represents a RGBA color.
 *
 * @author Heiko Brumme
 */
public final class Color {

    public static final Color WHITE = new Color(1f, 1f, 1f);
    public static final Color BLACK = new Color(0f, 0f, 0f);
    public static final Color RED = new Color(1f, 0f, 0f);
    public static final Color GREEN = new Color(0f, 1f, 0f);
    public static final Color BLUE = new Color(0f, 0f, 1f);

    /**
     * This value specifies the red component.
     */
    private float red;

    /**
     * This value specifies the green component.
     */
    private float green;

    /**
     * This value specifies the blue component.
     */
    private float blue;

    /**
     * This value specifies the transparency.
     */
    private float alpha;

    /**
     * The default color is black.
     */
    public Color() {
        this(0f, 0f, 0f);
    }

    /**
     * Creates a RGB-Color with an alpha value of 1.
     *
     * @param red The red component. Range from 0f to 1f.
     * @param green The green component. Range from 0f to 1f.
     * @param blue The blue component. Range from 0f to 1f.
     */
    public Color(float red, float green, float blue) {
        this(red, green, blue, 1f);
    }

    /**
     * Creates a RGBA-Color.
     *
     * @param red The red component. Range from 0f to 1f.
     * @param green The green component. Range from 0f to 1f.
     * @param blue The blue component. Range from 0f to 1f.
     * @param alpha The transparency. Range from 0f to 1f.
     */
    public Color(float red, float green, float blue, float alpha) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setAlpha(alpha);
    }

    /**
     * Creates a RGB-Color with an alpha value of 1.
     *
     * @param red The red component. Range from 0 to 255.
     * @param green The green component. Range from 0 to 255.
     * @param blue The blue component. Range from 0 to 255.
     */
    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    /**
     * Creates a RGBA-Color.
     *
     * @param red The red component. Range from 0 to 255.
     * @param green The green component. Range from 0 to 255.
     * @param blue The blue component. Range from 0 to 255.
     * @param alpha The transparency. Range from 0 to 255.
     */
    public Color(int red, int green, int blue, int alpha) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setAlpha(alpha);
    }

    /**
     * Returns the red component.
     *
     * @return The red component.
     */
    public float getRed() {
        return red;
    }

    /**
     * Sets the red component.
     *
     * @param red The red component. Range from 0f to 1f.
     */
    public void setRed(float red) {
        if (red < 0f) {
            red = 0f;
        }
        if (red > 1f) {
            red = 1f;
        }
        this.red = red;
    }

    /**
     * Sets the red component.
     *
     * @param red The red component. Range from 0 to 255.
     */
    public void setRed(int red) {
        setRed(red / 255f);
    }

    /**
     * Returns the green component.
     *
     * @return The green component.
     */
    public float getGreen() {
        return green;
    }

    /**
     * Sets the green component.
     *
     * @param green The green component. Range from 0f to 1f.
     */
    public void setGreen(float green) {
        if (green < 0f) {
            green = 0f;
        }
        if (green > 1f) {
            green = 1f;
        }
        this.green = green;
    }

    /**
     * Sets the green component.
     *
     * @param green The green component. Range from 0 to 255.
     */
    public void setGreen(int green) {
        setGreen(green / 255f);
    }

    /**
     * Returns the blue component.
     *
     * @return The blue component.
     */
    public float getBlue() {
        return blue;
    }

    /**
     * Sets the blue component.
     *
     * @param blue The blue component. Range from 0f to 1f.
     */
    public void setBlue(float blue) {
        if (blue < 0f) {
            blue = 0f;
        }
        if (blue > 1f) {
            blue = 1f;
        }
        this.blue = blue;
    }

    /**
     * Sets the blue component.
     *
     * @param blue The blue component. Range from 0 to 255.
     */
    public void setBlue(int blue) {
        setBlue(blue / 255f);
    }

    /**
     * Returns the transparency.
     *
     * @return The transparency.
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the transparency.
     *
     * @param alpha The transparency. Range from 0f to 1f.
     */
    public void setAlpha(float alpha) {
        if (alpha < 0f) {
            alpha = 0f;
        }
        if (alpha > 1f) {
            alpha = 1f;
        }
        this.alpha = alpha;
    }

    /**
     * Sets the transparency.
     *
     * @param alpha The transparency. Range from 0 to 255.
     */
    public void setAlpha(int alpha) {
        setAlpha(alpha / 255f);
    }

    /**
     * Returns the color as a (x,y,z)-Vector.
     *
     * @return The color as vec3.
     */
    public Vector3f toVector3f() {
        return new Vector3f(red, green, blue);
    }

    /**
     * Returns the color as a (x,y,z,w)-Vector.
     *
     * @return The color as vec4.
     */
    public Vector4f toVector4f() {
        return new Vector4f(red, green, blue, alpha);
    }

}
