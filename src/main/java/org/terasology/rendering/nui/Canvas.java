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

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;

/**
 * @author Immortius
 */
public interface Canvas {

    SubRegion subRegion(Rect2i region);

    Vector2i size();

    void setOffset(Vector2i offset);

    void setOffset(int x, int y);

    Vector2i getOffset();

    /**
     * Set the primary color of drawn text (can be overridden internally by color switches
     * @param color
     */
    void setTextColor(Color color);

    /**
     * Sets the horizontal alignment of text
     * @param horizontalAlignment
     */
    void setTextAlignment(HorizontalAlignment horizontalAlignment);

    /**
     * Sets the vertical alignment of text
     * @param verticalAlignment
     */
    void setTextAlignment(VerticalAlignment verticalAlignment);

    /**
     * Draws text. Text may include new lines, and will otherwise be broken up on whitespace if a line gets too long (a really long word that fills all available space will
     * be cropped).
     * @param font The font to use to draw text
     * @param text The text to draw
     */
    void drawText(Font font, String text);

    /**
     * Draws text, restricting it to the given area (within the current area context)
     * @param font
     * @param text
     * @param maxWidth
     */
    void drawText(Font font, String text, int maxWidth);

    void drawTextShadowed(Font font, String text, Color shadowColor);

    void drawTextShadowed(Font font, String text, int maxWidth, Color shadowColor);
}
