/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering;

import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.Color;

/**
 * Defines a set of special characters that manipulate the font color of a rendered text string.
 * Use {@link #toChar(int, int, int)}# to get such a char and append it to the text string at the desired position.
 * Use {@link #getReset()} to reset the color back to the default.
 * <br> 
 * <b>Note:</b> The resolution is only 4 bit per channel (not 8 as usual). 
 */
@API
public final class FontColor {
    private static final char FIRST_COLOR = 0xE000; // Unicode 6 specs: "Private Use Area: U+E000 -- U+F8FF"
    private static final char LAST_COLOR  = 0xEFFF; 

    private static final char RESET_COLOR  = 0xF000;
    
    private FontColor() {
        // avoid instantiation
    }

    /**
     * @param ch the character to test
     * @return true for all color chars and the reset char
     */
    public static boolean isValid(char ch) {
        return (ch == RESET_COLOR) || (ch >= FIRST_COLOR && ch <= LAST_COLOR);
    }
    
    /**
     * @param ch the character to convert
     * @return the actual color
     */
    public static Color toColor(char ch) {
        
        int rgb = ch - FIRST_COLOR;
        int r = (rgb >> 8) & 0xF;
        int g = (rgb >> 4) & 0xF;
        int b = (rgb >> 0) & 0xF;
        
        return new Color(r << 4, g << 4, b << 4);
    }
    
    /**
     * Stores the color with (4 + 4 + 4) = 12 bit accuracy as Unicode char
     * @param color the color
     * @return the unicode char
     * @deprecated use {@link FontColor#getColored(String, Color)} instead
     */
    @Deprecated
    public static char toChar(org.terasology.rendering.nui.Color color) {
        return toChar(color.r(), color.g(), color.b());
    }
    
    /**
     * Stores the color with (4 + 4 + 4) = 12 bit accuracy as Unicode char
     * @param r red in [0..255]
     * @param g green in [0..255]
     * @param b blue in [0..255]
     * @return the unicode char
     * @deprecated use {@link FontColor#getColored(String, Color)} instead
     */
    @Deprecated
    public static char toChar(int r, int g, int b) {
        int rr = (r >> 4);
        int rg = (g >> 4);
        int rb = (b >> 4);
        
        int ch = (rr << 8) | (rg << 4) | (rb << 0);
        return (char) (FIRST_COLOR + ch);
    }
    
    /**
     * Returns a string with encoded color information
     * @param str the text
     * @param color the color
     * @return the encoded string
     */
    public static String getColored(String str, org.terasology.rendering.nui.Color color) {
        return toChar(color) + str + getReset();
    }
    
    /**
     * @return the color reset char
     */
    public static char getReset() {
        return RESET_COLOR;
    }

    /**
     * @param text The colored text
     * @return the same text string, but without the color information
     */
    public static String stripColor(String text) {
        
        StringBuffer sb = new StringBuffer(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
        
            if (!isValid(c)) {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}
