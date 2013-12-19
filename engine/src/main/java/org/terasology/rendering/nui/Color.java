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

import com.google.common.base.Preconditions;
import org.terasology.engine.API;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Locale;
import java.util.Objects;

/**
 * Color is a representation of a RGBA color. Color components can be set and accessed via floats ranging from 0-1, or ints ranging from 0-255.
 * Color is immutable and thread safe.
 * <p/>
 * There are a plethora of Color classes, but none that are quite suitable IMO:
 * <ul>
 * <li>vecmaths - doesn't access with r/g/b/a, separation by representation is awkward, feature bland.</li>
 * <li>Slick2D - ideally will lose dependency on slick utils. Also ties to lwjgl</li>
 * <li>Lwjgl - don't want to be graphics implementation dependant</li>
 * <li>javafx - ew</li>
 * <li>com.sun.prism - double ew. Shouldn't use com.sun classes at all</li>
 * <li>awt - tempting, certainly feature-rich. Has some strange awt-specific functionality though (createContext) and native links</li>
 * </ul>
 *
 * @author Immortius
 */
@API
public class Color {

    public static final Color BLACK = new Color(0x000000FF);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color BLUE = new Color(0x0000FFFF);
    public static final Color GREEN = new Color(0x00FF00FF);
    public static final Color RED = new Color(0xFF0000FF);
    public static final Color GREY = new Color(0x888888FF);
    public static final Color TRANSPARENT = new Color(0x00000000);
    public static final Color YELLOW = new Color(0xFFFF00FF);

    private static final int MAX = 255;
    private static final int RED_OFFSET = 24;
    private static final int GREEN_OFFSET = 16;
    private static final int BLUE_OFFSET = 8;
    private static final int RED_FILTER = 0x00FFFFFF;
    private static final int GREEN_FILTER = 0xFF00FFFF;
    private static final int BLUE_FILTER = 0xFFFF00FF;
    private static final int ALPHA_FILTER = 0xFFFFFF00;

    private final int representation;

    /**
     * Creates a color that is black with full alpha.
     */
    public Color() {
        representation = 0x000000FF;
    }

    public Color(int representation) {
        this.representation = representation;
    }

    /**
     * Create a color with the given red/green/blue values. Alpha is initialised as max.
     * @param r
     * @param g
     * @param b
     */
    public Color(float r, float g, float b) {
        this((int) (r * MAX), (int) (g * MAX), (int) (b * MAX));
    }

    /**
     * Creates a color with the given red/green/blue/alpha values.
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public Color(float r, float g, float b, float a) {
        this((int) (r * MAX), (int) (g * MAX), (int) (b * MAX), (int) (a * MAX));
    }

    /**
     * Creates a color with the given red/green/blue values. Alpha is initialised as max.
     * @param r
     * @param g
     * @param b
     */
    public Color(int r, int g, int b) {
        this(r, g, b, 0xFF);
    }

    /**
     * Creates a color with the given red/green/blue/alpha values.
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public Color(int r, int g, int b, int a) {
        Preconditions.checkArgument(r >= 0 && r <= MAX, "Color values must be in range 0-255");
        Preconditions.checkArgument(g >= 0 && g <= MAX, "Color values must be in range 0-255");
        Preconditions.checkArgument(b >= 0 && b <= MAX, "Color values must be in range 0-255");
        Preconditions.checkArgument(a >= 0 && a <= MAX, "Color values must be in range 0-255");
        representation = r << RED_OFFSET | g << GREEN_OFFSET | b << BLUE_OFFSET | a;
    }

    /**
     * @return The red component, between 0 and 255
     */
    public int r() {
        return (representation >> RED_OFFSET) & MAX;
    }

    /**
     * @return The green component, between 0 and 255
     */
    public int g() {
        return (representation >> GREEN_OFFSET) & MAX;
    }

    /**
     * @return The blue component, between 0 and 255
     */
    public int b() {
        return (representation >> BLUE_OFFSET) & MAX;
    }

    /**
     * @return The alpha component, between 0 and 255
     */
    public int a() {
        return representation & MAX;
    }

    public float rf() {
        return r() / 255.f;
    }

    public float bf() {
        return b() / 255.f;
    }

    public float gf() {
        return g() / 255.f;
    }

    public float af() {
        return a() / 255.f;
    }

    public Color alterRed(int value) {
        Preconditions.checkArgument(value >= 0 || value <= MAX, "Color values must be in range 0-255");
        return new Color(value << RED_OFFSET | (representation & RED_FILTER));
    }

    public Color alterBlue(int value) {
        Preconditions.checkArgument(value >= 0 || value <= MAX, "Color values must be in range 0-255");
        return new Color(value << BLUE_OFFSET | (representation & BLUE_FILTER));
    }

    public Color alterGreen(int value) {
        Preconditions.checkArgument(value >= 0 || value <= MAX, "Color values must be in range 0-255");
        return new Color(value << GREEN_OFFSET | (representation & GREEN_FILTER));
    }

    public Color alterAlpha(int value) {
        Preconditions.checkArgument(value >= 0 || value <= MAX, "Color values must be in range 0-255");
        return new Color(value | (representation & ALPHA_FILTER));
    }

    public int rgba() {
        return representation;
    }

    public Vector4f toVector4f() {
        return new Vector4f(rf(), gf(), bf(), af());
    }

    public Vector3f toVector3f() {
        return new Vector3f(rf(), gf(), bf());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Color) {
            Color other = (Color) obj;
            return representation == other.representation;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(representation);
    }

    public String toHex() {
        StringBuilder builder = new StringBuilder();
        String hexString = Integer.toHexString(representation);
        for (int i = 0; i < 8 - hexString.length(); ++i) {
            builder.append('0');
        }
        builder.append(hexString.toUpperCase(Locale.ENGLISH));
        return builder.toString();
    }

    @Override
    public String toString() {
        return toHex();
    }
}
