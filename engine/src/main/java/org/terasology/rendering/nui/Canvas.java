/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.assets.ResourceUrn;
import org.terasology.math.Border;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UIStyle;

/**
 * Canvas provides primitive drawing operations for use by the UI.
 *
 */
public interface Canvas {

    /**
     * @return The size of the drawable canvas, in pixels.
     */
    Vector2i size();

    /**
     * minX and minY of the region will always be zero.
     *
     * @return The region of drawable canvas, in pixels.
     */
    Rect2i getRegion();

    /**
     * Sets the alpha for drawing all elements.
     * This accumulates across sub regions, so the canvas is set to alpha 0.5f and then a sub region is begun and the canvas is set to alpha 0.5f,
     * elements will be drawn with an effective alpha of 0.25f
     *
     * @param value The value for alpha between 0 and 1.
     */
    void setAlpha(float value);

    /**
     * Sets the skin to use for drawing operations
     *
     * @param skin
     */
    void setSkin(UISkin skin);

    /**
     * @return The skin being used for drawing operations
     */
    UISkin getSkin();

    /**
     * Sets the family subset of the current skin to use for drawing operations
     *
     * @param familyName
     */
    void setFamily(String familyName);

    /**
     * Sets the mode of the current skin/widget/family selection to use for drawing operations
     *
     * @param mode
     */
    void setMode(String mode);

    /**
     * Sets the part of the current skin/element/family selection to use for drawing operations
     *
     * @param part
     */
    void setPart(String part);

    /**
     * @return The current style, as determined by the skin/widget/family/mode combination currently set.
     */
    UIStyle getCurrentStyle();

    /**
     * Calculates the minimum size a widget will take, given no space restrictions.
     * Skin settings are automatically taken into account, unless widget.isSkinAppliedByCanvas() returns false (in which case it
     * is up to the widget to apply any style settings it needs).
     *
     * @param widget The widget to get the size of
     * @return The preferred size of the widget - given no restrictions.
     */
    Vector2i calculatePreferredSize(UIWidget widget);

    /**
     * Calculates the minimum size a widget will take, given space restrictions. May still extend pass the restrictions if it has a minimum size, or simply cannot fit.
     * Skin settings are automatically taken into account, unless widget.isSkinAppliedByCanvas() returns false (in which case it
     * is up to the widget to apply any style settings it needs).
     *
     * @param widget           The widget to get the size of
     * @param sizeRestrictions A hint as to the available area for the drawing the widget
     * @return The restricted size of the widget
     */
    Vector2i calculateRestrictedSize(UIWidget widget, Vector2i sizeRestrictions);

    /**
     * Calcualtes the maximum size a widget can take. A dimension will be Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE) if unbounded
     *
     * @param widget The widget to get the maximum size of.
     * @return The maximum size of the widget.
     */
    Vector2i calculateMaximumSize(UIWidget widget);

    /**
     * Draws a widget to fill the current canvas. Skin settings are applied, unless widget.isSkinAppliedByCanvas() returns false (in which case it is up to the widget
     * to apply any style settings it needs).
     *
     * @param widget
     */
    void drawWidget(UIWidget widget);

    /**
     * Draws a widget to the given region of the current canvas. Skin settings are applied, unless element.isSkinAppliedByCanvas() returns false.
     * <br><br>
     * This method will update the skin settings for the given element and its current mode.  Min/max and fixed size settings will be applied, along with horizontal
     * and vertical alignment as necessary. If element.isSkinAppliedByCanvas() returns true, any background will be drawn and margin applied to remaining region to
     * determine the region provided to the element for drawing content.
     *
     * @param element
     * @param region
     */
    void drawWidget(UIWidget element, Rect2i region);

    /**
     * Draws test, using the current style.
     *
     * @param text
     */
    void drawText(String text);

    /**
     * Draws text to the given region, using the current style
     *
     * @param text
     * @param region
     */
    void drawText(String text, Rect2i region);

    /**
     * Draws a texture filling the canvas using the current style.
     *
     * @param texture The texture to draw
     */
    void drawTexture(TextureRegion texture);

    /**
     * Draws a texture filling the canvas using the current style.
     *
     * @param texture The texture to draw
     * @param color
     */
    void drawTexture(TextureRegion texture, Color color);

    /**
     * Draws a texture to the given region using the current style.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     */
    void drawTexture(TextureRegion texture, Rect2i region);

    /**
     * Draws a texture to the given region using the current style.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param color   The color modifier for the texture
     */
    void drawTexture(TextureRegion texture, Rect2i region, Color color);

    /**
     * Draws the background of the current style, filling the entire canvas.
     */
    void drawBackground();

    /**
     * Draws the background of the current style, filling the given region.
     *
     * @param region
     */
    void drawBackground(Rect2i region);

    /**
     * Allocates a sub region for drawing, until that SubRegion is closed. The top-left corner of the SubRegion
     * becomes the new offset (0,0), and the value size() is the width/height of the SubRegion. All canvas state is specific
     * to a region, so a new sub-region will have offset/text color and other options returned to default. When a sub-region
     * ends the previous canvas settings are restored.
     * <br><br>
     * SubRegions allow UI elements to be draw in isolation without having to know about their location on the screen.
     * SubRegions can be marked as cropped, in which case any drawing that falls outside of the region
     * will not appear.
     * <br><br>
     * SubRegions are an AutoClosable, so ideally are used as a resource in a try-block, to ensure they are closed
     * when no longer needed.
     * <pre>
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
     * Allocates a sub region for drawing to a target texture, until that SubRegion is closed.
     * <br><br>
     * For each (texture) uri a FrameBufferObject and a target texture is created.
     * Notice, the resulting texture is flipped. To draw it in the right order use:
     * <pre>
     * try (SubRegion ignored = canvas.subRegionFBO(uri, size) {
     *    //.. draw within SubRegion.
     * }
     * Texture texture = Assets.get(uri, Texture.class);
     * canvas.drawTextureRaw(texture, screenRegion, ScaleMode.SCALE_FIT, 0, 1f, 1f, -1f);
     * </pre>
     * <br><br>
     *
     * @param uri  The URI to access the texture
     * @param size the size of the texture.
     * @return A SubRegion, to be closed when no long needed
     */
    SubRegion subRegionFBO(ResourceUrn uri, BaseVector2i size);

    /**
     * When drawOnTop is set to true, subsequent drawing will be on top of everything else.
     * This ceases when drawOnTop is set to false or the current subRegion ends.
     *
     * @param focused
     */
    void setDrawOnTop(boolean focused);

    /**
     * Draws text without using the current style. Text may include new lines. This text will always be left-aligned.
     *
     * @param text  The text to draw
     * @param font  The font to use to draw text
     * @param color The color of to draw the text
     */
    void drawTextRaw(String text, Font font, Color color);

    /**
     * Draws text without using the current style. Text may include new lines.
     * Additionally new lines will be added to prevent any given line exceeding the width of the region.
     * If an individual word is longer than the width of the region, it will be split mid-word.
     *
     * @param text   The text to draw
     * @param font   The font to use to draw text
     * @param color  The color of to draw the text
     * @param region The region in which to draw the text
     */
    void drawTextRaw(String text, Font font, Color color, Rect2i region);

    /**
     * Draws text without using the current style, aligned within the drawWidth.  Text may include new lines.
     * Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param text   The text to draw
     * @param font   The font to use to draw text
     * @param color  The color of to draw the text
     * @param region The region within which to of the text. The text will be wrapped to multiple lines if it exceeds this width
     * @param hAlign The horizontal alignment or justification of the text
     * @param vAlign The vertical alignment of the text
     */
    void drawTextRaw(String text, Font font, Color color, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign);

    /**
     * Draws text without using the current style, aligned within the drawWidth.  Text may include new lines.
     * Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param text       The text to draw
     * @param font       The font to use to draw text
     * @param color      The color of to draw the text
     * @param underlined Whether the text should be underlined
     * @param region     The region within which to of the text. The text will be wrapped to multiple lines if it exceeds this width
     * @param hAlign     The horizontal alignment or justification of the text
     * @param vAlign     The vertical alignment of the text
     */
    void drawTextRaw(String text, Font font, Color color, boolean underlined, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign);

    /**
     * Draws shadowed text without using the current style. Text may include new lines. This text will always be left-aligned.
     *
     * @param text        The text to draw
     * @param font        The font to use to draw text
     * @param color       The color of to draw the text
     * @param shadowColor The color to draw the shadow
     */
    void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor);

    /**
     * raws shadowed text without using the current style. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param text        The text to draw
     * @param font        The font to use to draw text
     * @param color       The color of to draw the text
     * @param shadowColor The color to draw the shadow
     * @param region      The region within which to draw this text. The text will be wrapped to new lines if it exceeds this width.
     */
    void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region);

    /**
     * Draws shadowed text without using the current style. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param text        The text to draw
     * @param font        The font to use to draw text
     * @param color       The color of to draw the text
     * @param shadowColor The color to draw the shadow
     * @param region      The region within which to draw this text. The text will be wrapped to new lines if it exceeds this width.
     * @param hAlign      The horizontal alignment or justification of the text
     * @param vAlign      The vertical alignment of the text
     */
    void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign);

    /**
     * Draws shadowed text without using the current style. Text may include new lines. Additionally new lines will be added to prevent any given line exceeding drawWidth.
     * If an individual word is longer than the drawWidth, it will be split mid-word.
     *
     * @param text        The text to draw
     * @param font        The font to use to draw text
     * @param color       The color of to draw the text
     * @param shadowColor The color to draw the shadow
     * @param underline   Whether the text should be underlined (by default, underlines may also be added by underline unicode)
     * @param region      The region within which to draw this text. The text will be wrapped to new lines if it exceeds this width.
     * @param hAlign      The horizontal alignment or justification of the text
     * @param vAlign      The vertical alignment of the text
     */
    void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, boolean underline, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign);

    /**
     * Draws a texture to the given area without using the current style. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     */
    void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode);

    /**
     * Draws a texture to the given area without using the current style. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param color   The color modifier for drawing the texture
     * @param mode    The method for adapting this texture to the region
     */
    void drawTextureRaw(TextureRegion texture, Rect2i region, Color color, ScaleMode mode);

    /**
     * Draws a sub-region of a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     * @param ux      The leftmost pixel of the sub-region of the texture to draw
     * @param uy      The topmost pixel of the sub-region of the texture to draw
     * @param uw      The width of the sub-region of the texture to draw, in pixels
     * @param uh      The height of the sub-region of the texture to draw, in pixels
     */
    void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode, int ux, int uy, int uw, int uh);

    /**
     * Draws a sub-region of a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param mode    The method for adapting this texture to the region
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode, float ux, float uy, float uw, float uh);

    /**
     * Draws a sub-region of a texture to the given area. If the texture is a different size to the area, it will be adapted according to the ScaleMode.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param color   The color modifier for drawing the texture
     * @param mode    The method for adapting this texture to the region
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTextureRaw(TextureRegion texture, Rect2i region, Color color, ScaleMode mode, float ux, float uy, float uw, float uh);

    /**
     * Draws a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     */
    void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile);

    /**
     * Draws a sub-region of a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     * @param ux      The leftmost pixel of the sub-region of the texture to draw
     * @param uy      The topmost pixel of the sub-region of the texture to draw
     * @param uw      The width of the sub-region of the texture to draw, in pixels
     * @param uh      The height of the sub-region of the texture to draw, in pixels
     */
    void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile, int ux, int uy, int uw, int uh);

    /**
     * Draws a sub-region of a texture with a border - allows the drawing of a texture to a wider area without distorting the edge of the texture.
     *
     * @param texture The texture to draw
     * @param region  The area to draw the texture in, in pixels
     * @param border  The size of the border.
     * @param tile    Whether to tile the center and edges, or just stretch them
     * @param ux      The leftmost point of the sub-region of the texture to draw, between 0 and 1
     * @param uy      The topmost pixel of the sub-region of the texture to draw, between 0 and 1
     * @param uw      The width of the sub-region of the texture to draw, relative to the texture size
     * @param uh      The height of the sub-region of the texture to draw, relative to the texture size
     */
    void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile, float ux, float uy, float uw, float uh);


    /**
     * Draws a material to a given area.
     * <br><br>
     * Other than cropping and positioning the material relative to the current region of the canvas, it is up to the material as to how it behaves.
     * The "alpha" parameter of the material, if any, will be set to the current alpha of the canvas.
     *
     * @param material
     * @param region
     */
    void drawMaterial(Material material, Rect2i region);

    /**
     * Draws a mesh centered on the given screen position.
     *
     * @param mesh     The mesh to draw
     * @param material The material to draw the mesh with
     * @param region   The screen area to draw the mesh
     * @param rotation The rotation of the mesh
     * @param offset   Offset, in object space, for the mesh
     * @param scale    A relative scale for drawing the mesh
     */
    void drawMesh(Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale);

    void drawMesh(Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale);

    /**
     * Adds an interaction region filling the region used to draw the current widget. The widget's margin is used to expand the interaction region to fill the
     * full area of the widget.
     *
     * @param listener
     */
    void addInteractionRegion(InteractionListener listener);

    /**
     * Adds an interaction region filling the desired region.
     *
     * @param listener
     * @param region
     */
    void addInteractionRegion(InteractionListener listener, Rect2i region);

    /**
     * Adds an interaction region filling the region used to draw the current widget. The widget's margin is used to expand the interaction region to fill the
     * full area of the widget.
     *
     * @param listener
     * @param tooltip
     */
    void addInteractionRegion(InteractionListener listener, UIWidget tooltip);

    /**
     * Adds an interaction region filling the desired region.
     *
     * @param listener
     * @param tooltip
     * @param region
     */
    void addInteractionRegion(InteractionListener listener, UIWidget tooltip, Rect2i region);

    /**
     * Adds an interaction region filling the region used to draw the current widget. The widget's margin is used to expand the interaction region to fill the
     * full area of the widget.
     *
     * @param listener
     * @param tooltip
     */
    void addInteractionRegion(InteractionListener listener, String tooltip);

    /**
     * Adds an interaction region filling the desired region.
     *
     * @param listener
     * @param tooltip
     * @param region
     */
    void addInteractionRegion(InteractionListener listener, String tooltip, Rect2i region);

    void drawLine(int startX, int startY, int endX, int endY, Color color);

    void drawFilledRectangle(Rect2i region, Color color);
}
