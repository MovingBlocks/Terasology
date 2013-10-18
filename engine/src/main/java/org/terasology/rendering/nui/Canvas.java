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

import org.lwjgl.util.vector.Quaternion;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Canvas provides primitive drawing operations for use by the UI.
 *
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
     * @param region The region to restrict to, relative to the current region, in pixels.
     * @param crop   Whether to crop elements falling outside this region.
     * @return A SubRegion, to be closed when no long needed
     */
    SubRegion subRegion(Rect2i region, boolean crop);

    /**
     * @return The size of the drawable canvas, in pixels.
     */
    Vector2i size();

    /**
     * Sets the position (top-left corner) for drawText.
     *
     * @param pos The offset in pixels
     */
    void setTextCursor(Vector2i pos);

    /**
     * Sets the position (top-left corner) for drawText.
     *
     * @param x The horizontal offset in pixels
     * @param y The vertical offset in pixels
     */
    void setTextCursor(int x, int y);

    /**
     * @return The current offset for drawing operations in pixels.
     */
    Vector2i getTextCursor();

    /**
     * Sets the alpha for drawing all elements.
     * This accumulates across sub regions, so the canvas is set to alpha 0.5f and then a sub region is begun and the canvas is set to alpha 0.5f,
     * elements will be drawn with an effective alpha of 0.25f
     * @param value The value for alpha between 0 and 1.
     */
    void setAlpha(float value);

    /**
     * Set the primary color of drawn text
     *
     * @param color The color to draw text in
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
     * Draws text. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param font      The font to use to draw text
     * @param text      The text to draw
     * @param drawWidth The maximum width in which to draw the text. The text will be wrapped to multiple lines if it exceeds this width
     */
    void drawText(Font font, String text, int drawWidth);

    /**
     * Draws text, aligned within the drawWidth.  Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param font      The font to use to draw text
     * @param text      The text to draw
     * @param drawWidth The maximum width of the text. The text will be wrapped to multiple lines if it exceeds this width
     * @param alignment The alignment or justification of the text
     */
    void drawText(Font font, String text, int drawWidth, HorizontalAlignment alignment);

    /**
     * Draws text with a shadow. Text may include new lines. This text will always be left-aligned.
     *
     * @param font        The font to use to draw text
     * @param text        The text to draw
     * @param shadowColor The color to draw the shadow
     */
    void drawTextShadowed(Font font, String text, Color shadowColor);

    /**
     * Draws text with a shadow. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param font        The font to use to draw text
     * @param text        The text to draw
     * @param drawWidth   The maximum width in which to draw the text. The text will be wrapped to multiple lines if it exceeds this width
     * @param shadowColor The color to draw the shadow
     */
    void drawTextShadowed(Font font, String text, int drawWidth, Color shadowColor);

    /**
     * Draws text with a shadow. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param font        The font to use to draw text
     * @param text        The text to draw
     * @param drawWidth   The maximum width in which to draw the text. The text will be wrapped to multiple lines if it exceeds this width
     * @param alignment   The alignment or justification of the text
     * @param shadowColor The color to draw the shadow
     */
    void drawTextShadowed(Font font, String text, int drawWidth, HorizontalAlignment alignment, Color shadowColor);

    /**
     * Draws a texture to the given area. This is the same as drawing the texture with ScaleMode.STRETCH.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     */
    void drawTexture(Texture texture, Rect2i toArea);

    /**
     * Draws a sub-region of a texture to the given area. This is the same as drawing the texture with ScaleMode.STRETCH.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param ux      The leftmost pixel of the sub-region of the texture to draw
     * @param uy      The topmost pixel of the sub-region of the texture to draw
     * @param uw      The width of the sub-region of the texture to draw, in pixels
     * @param uh      The height of the sub-region of the texture to draw, in pixels
     */
    void drawTexture(Texture texture, Rect2i toArea, int ux, int uy, int uw, int uh);

    /**
     * Draws a sub-region of a texture to the given area. This is the same as drawing the texture with ScaleMode.STRETCH.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTexture(Texture texture, Rect2i toArea, float ux, float uy, float uw, float uh);

    /**
     * Draws a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     */
    void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode);

    /**
     * Draws a sub-region of a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     * @param ux      The leftmost pixel of the sub-region of the texture to draw
     * @param uy      The topmost pixel of the sub-region of the texture to draw
     * @param uw      The width of the sub-region of the texture to draw, in pixels
     * @param uh      The height of the sub-region of the texture to draw, in pixels
     */
    void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, int ux, int uy, int uw, int uh);

    /**
     * Draws a sub-region of a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, float ux, float uy, float uw, float uh);

    /**
     * Draws a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     */
    void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile);

    /**
     * Draws a sub-region of a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     * @param ux      The leftmost pixel of the sub-region of the texture to draw
     * @param uy      The topmost pixel of the sub-region of the texture to draw
     * @param uw      The width of the sub-region of the texture to draw, in pixels
     * @param uh      The height of the sub-region of the texture to draw, in pixels
     */
    void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, int ux, int uy, int uw, int uh);

    /**
     * Draws a sub-region of a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param toArea  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, float ux, float uy, float uw, float uh);


    /**
     * Draws a material to a given area.
     *
     * Other than cropping and positioning the material relative to the current region of the canvas, it is up to the material as to how it behaves.
     * The "alpha" parameter of the material, if any, will be set to the current alpha of the canvas.
     *
     * @param material
     * @param toArea
     */
    void drawMaterial(Material material, Rect2i toArea);

    /**
     * Draws a mesh centered on the given screen position.
     * @param mesh The mesh to draw
     * @param material The material to draw the mesh with
     * @param region The screen area to draw the mesh
     * @param rotation The rotation of the mesh
     * @param offset Offset, in object space, for the mesh
     * @param scale A relative scale for drawing the mesh
     */
    void drawMesh(Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale);

    void drawMesh(Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale);
}
