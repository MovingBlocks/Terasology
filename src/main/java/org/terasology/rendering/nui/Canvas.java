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
 * Canvas provides primitive drawing operations for use by the UI.
 * @author Immortius
 */
public interface Canvas {

    /**
     * Allocates a sub region for drawing, until that SubRegion is closed. The top-left corner of the SubRegion
     * becomes the new offset (0,0), and the value size() is the width/height of the SubRegion. All canvas state is specific
     * to a region, so a new sub-region will have offset/text color and other options returned to default. When a sub-region
     * ends the previous canvas settings are restored.
     * <p/>
     * SubRegions allow UI elements to be draw in isolation without having to know about their location on the screen.
     * SubRegions can be marked as cropped, in which case any drawing that falls outside of the region
     * will not appear.
     * <p/>
     * SubRegions are an AutoClosable, so ideally are used as a resource in a try-block, to ensure they are closed
     * when no longer needed.
     * <pre>
     * {@code
     * try (SubRegion ignored = canvas.subRegion(region, true) {
     *    //.. draw within SubRegion.
     * }
     * </pre>
     * They may be closed manually as well, in which case it is important they are closed in the reverse order of creation.
     *
     * @param region The region to restrict to, relative to the current region.
     * @param crop   Whether to crop elements falling outside this region.
     * @return A subregion, to be closed when no long needed
     */
    SubRegion subRegion(Rect2i region, boolean crop);

    /**
     * @return The size of the drawable canvas.
     */
    Vector2i size();

    /**
     * Sets the offset (top-left corner) for drawing operations, such as drawText.
     *
     * @param offset
     */
    void setOffset(Vector2i offset);

    /**
     * Sets the offset (top-left corner) for drawing operations, such as drawText.
     *
     * @param x
     * @param y
     */
    void setOffset(int x, int y);

    /**
     * @return The current offset for drawing operations.
     */
    Vector2i getOffset();

    /**
     * Set the primary color of drawn text (can be overridden internally by color switches
     *
     * @param color
     */
    void setTextColor(Color color);

    /**
     * Draws text. Text may include new lines. This text will always be left-aligned.
     *
     * @param font The font to use to draw text
     * @param text The text to draw
     */
    void drawText(Font font, String text);

    /**
     * Draws text. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding maxWidth.
     * If an individual word is longer than the maxWidth, it will be split mid-word.
     *
     * @param font
     * @param text
     * @param maxWidth
     */
    void drawText(Font font, String text, int maxWidth);

    /**
     * Draws text with a shadow. Text may include new lines. This text will always be left-aligned.
     *
     * @param font The font to use to draw text
     * @param text The text to draw
     */
    void drawTextShadowed(Font font, String text, Color shadowColor);

    /**
     * Draws text with a shadow. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding maxWidth.
     * If an individual word is longer than the maxWidth, it will be split mid-word.
     *
     * @param font
     * @param text
     * @param maxWidth
     */
    void drawTextShadowed(Font font, String text, int maxWidth, Color shadowColor);


}
