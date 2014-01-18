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
package org.terasology.rendering.nui.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.engine.Time;
import org.terasology.input.MouseInput;
import org.terasology.math.Border;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UIStyle;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Immortius
 */
public class CanvasImpl implements CanvasControl {

    private static final Logger logger = LoggerFactory.getLogger(CanvasImpl.class);

    /**
     * The maximum distance the cursor can move between clicks and still be counted as double clicking
     */
    private static final int MAX_DOUBLE_CLICK_DISTANCE = 5;
    /**
     * The maximum time (in milliseconds) between clicks that will still be counted as double clicking
     */
    private static final int DOUBLE_CLICK_TIME = 200;

    /**
     * A sufficiently large value for "unbounded" regions, without risking overflow.
     */
    private static final int LARGE_INT = Integer.MAX_VALUE / 2;

    private final NUIManager nuiManager;
    private final Time time;

    private CanvasState state;
    private Deque<LwjglSubRegion> subregionStack = Queues.newArrayDeque();

    private Mesh billboard = Assets.getMesh("engine:UIBillboard");
    private Material textureMat = Assets.getMaterial("engine:UITexture");
    private Material meshMat = Assets.getMaterial("engine:UILitMesh");

    private List<DrawOperation> drawOnTopOperations = Lists.newArrayList();

    // Text mesh caching
    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    // Interaction region handling
    private Deque<InteractionRegion> interactionRegions = Queues.newArrayDeque();
    private Set<InteractionRegion> mouseOverRegions = Sets.newLinkedHashSet();
    private InteractionRegion clickedRegion;

    // Double click handling
    private long lastClickTime;
    private MouseInput lastClickButton;
    private Vector2i lastClickPosition = new Vector2i();

    private CanvasRenderer renderer;

    public CanvasImpl(NUIManager nuiManager, Time time, CanvasRenderer renderer) {
        this.renderer = renderer;
        this.nuiManager = nuiManager;
        this.time = time;
    }

    @Override
    public void preRender() {
        interactionRegions.clear();
        Vector2i size = renderer.getTargetSize();
        state = new CanvasState(null, Rect2i.createFromMinAndSize(0, 0, size.x, size.y));
        renderer.preRender();
        crop(state.cropRegion);
    }

    @Override
    public void postRender() {
        for (DrawOperation operation : drawOnTopOperations) {
            operation.draw();
        }
        drawOnTopOperations.clear();

        if (!subregionStack.isEmpty()) {
            logger.error("UI Subregions are not being correctly ended");
            while (!subregionStack.isEmpty()) {
                subregionStack.pop().close();
            }
        }
        Iterator<Map.Entry<TextCacheKey, Map<Material, Mesh>>> textIterator = cachedText.entrySet().iterator();
        while (textIterator.hasNext()) {
            Map.Entry<TextCacheKey, Map<Material, Mesh>> entry = textIterator.next();
            if (!usedText.contains(entry.getKey())) {
                for (Mesh mesh : entry.getValue().values()) {
                    Assets.dispose(mesh);
                }
                textIterator.remove();
            }
        }
        usedText.clear();

        renderer.postRender();
    }

    @Override
    public void processMousePosition(Vector2i position) {
        if (clickedRegion != null) {
            Vector2i relPos = new Vector2i(position);
            relPos.sub(clickedRegion.region.min());
            clickedRegion.listener.onMouseDrag(relPos);
        }

        Set<InteractionRegion> newMouseOverRegions = Sets.newLinkedHashSet();
        Iterator<InteractionRegion> iter = interactionRegions.descendingIterator();
        while (iter.hasNext()) {
            InteractionRegion next = iter.next();
            if (next.region.contains(position)) {
                Vector2i relPos = new Vector2i(position);
                relPos.sub(next.region.min());
                next.listener.onMouseOver(relPos, newMouseOverRegions.isEmpty());
                newMouseOverRegions.add(next);
            }
        }

        for (InteractionRegion region : mouseOverRegions) {
            if (!newMouseOverRegions.contains(region)) {
                region.listener.onMouseLeave();
            }
        }

        if (clickedRegion != null && !interactionRegions.contains(clickedRegion)) {
            clickedRegion = null;
        }

        mouseOverRegions = newMouseOverRegions;
    }

    @Override
    public boolean processMouseClick(MouseInput button, Vector2i pos) {
        boolean possibleDoubleClick = lastClickPosition.gridDistance(pos) < MAX_DOUBLE_CLICK_DISTANCE && lastClickButton == button
                && time.getGameTimeInMs() - lastClickTime < DOUBLE_CLICK_TIME;
        lastClickPosition.set(pos);
        lastClickButton = button;
        lastClickTime = time.getGameTimeInMs();
        for (InteractionRegion next : mouseOverRegions) {
            if (next.region.contains(pos)) {
                Vector2i relPos = new Vector2i(pos);
                relPos.sub(next.region.min());
                if (possibleDoubleClick && nuiManager.getFocus() == next.element) {
                    if (next.listener.onMouseDoubleClick(button, relPos)) {
                        clickedRegion = next;
                        return true;
                    }
                } else if (next.listener.onMouseClick(button, relPos)) {
                    clickedRegion = next;
                    nuiManager.setFocus(next.element);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean processMouseRelease(MouseInput button, Vector2i pos) {
        if (clickedRegion != null) {
            Vector2i relPos = new Vector2i(pos);
            relPos.sub(clickedRegion.region.min());
            clickedRegion.listener.onMouseRelease(button, relPos);
            clickedRegion = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean processMouseWheel(int wheelTurns, Vector2i pos) {
        for (InteractionRegion next : mouseOverRegions) {
            if (next.region.contains(pos)) {
                Vector2i relPos = new Vector2i(pos);
                relPos.sub(next.region.min());
                if (next.listener.onMouseWheel(wheelTurns, relPos)) {
                    clickedRegion = next;
                    nuiManager.setFocus(next.element);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public SubRegion subRegion(Rect2i region, boolean crop) {
        return new LwjglSubRegion(region, crop);
    }

    @Override
    public void setDrawOnTop(boolean drawOnTop) {
        this.state.drawOnTop = drawOnTop;
    }

    @Override
    public Vector2i size() {
        return new Vector2i(state.drawRegion.width(), state.drawRegion.height());
    }

    @Override
    public Rect2i getRegion() {
        return Rect2i.createFromMinAndSize(0, 0, state.drawRegion.width(), state.drawRegion.height());
    }

    @Override
    public void setAlpha(float value) {
        state.alpha = value;
    }

    @Override
    public void setSkin(UISkin skin) {
        state.skin = skin;
    }

    @Override
    public UISkin getSkin() {
        return state.skin;
    }

    @Override
    public void setFamily(String familyName) {
        state.family = familyName;
    }

    @Override
    public void setMode(String mode) {
        state.mode = mode;
    }

    @Override
    public void setPart(String part) {
        state.part = part;
    }

    @Override
    public UIStyle getCurrentStyle() {
        return state.getCurrentStyle();
    }

    @Override
    public Vector2i calculatePreferredSize(UIWidget widget) {
        return calculateRestrictedSize(widget, new Vector2i(LARGE_INT, LARGE_INT));
    }

    @Override
    public Vector2i calculateRestrictedSize(UIWidget widget, Vector2i sizeRestrictions) {
        if (widget == null) {
            return sizeRestrictions;
        }

        String family = (widget.getFamily() != null) ? widget.getFamily() : state.family;
        UIStyle elementStyle = state.skin.getStyleFor(family, widget.getClass(), widget.getMode());
        Rect2i region = applyStyleToSize(Rect2i.createFromMinAndSize(Vector2i.zero(), sizeRestrictions), elementStyle);
        try (SubRegion ignored = subRegionForWidget(widget, region, false)) {
            Vector2i preferredSize = widget.getPreferredContentSize(this, elementStyle.getMargin().shrink(sizeRestrictions));
            preferredSize = elementStyle.getMargin().grow(preferredSize);
            return applyStyleToSize(preferredSize, elementStyle);
        }
    }

    @Override
    public Vector2i calculateMaximumSize(UIWidget widget) {
        if (widget == null) {
            return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        String family = (widget.getFamily() != null) ? widget.getFamily() : state.family;
        UIStyle elementStyle = state.skin.getStyleFor(family, widget.getClass(), widget.getMode());
        try (SubRegion ignored = subRegionForWidget(widget, getRegion(), false)) {
            return applyStyleToSize(elementStyle.getMargin().grow(widget.getMaxContentSize(this)), elementStyle);
        }
    }

    @Override
    public void drawWidget(UIWidget widget) {
        drawWidget(widget, getRegion());
    }

    @Override
    public void drawWidget(UIWidget element, Rect2i region) {
        if (element == null || !element.isVisible()) {
            return;
        }

        String family = (element.getFamily() != null) ? element.getFamily() : state.family;
        UIStyle newStyle = state.skin.getStyleFor(family, element.getClass(), element.getMode());
        Rect2i regionArea;
        try (SubRegion ignored = subRegionForWidget(element, region, false)) {
            regionArea = applyStyleToSize(region, newStyle, element.getMaxContentSize(this));
        }

        try (SubRegion ignored = subRegionForWidget(element, regionArea, false)) {
            if (element.isSkinAppliedByCanvas()) {
                drawBackground();
                try (SubRegion withMargin = subRegionForWidget(element, newStyle.getMargin().shrink(Rect2i.createFromMinAndSize(Vector2i.zero(), regionArea.size())), false)) {
                    element.onDraw(this);
                }
            } else {
                element.onDraw(this);
            }
        }
    }

    private SubRegion subRegionForWidget(UIWidget widget, Rect2i region, boolean crop) {
        SubRegion result = subRegion(region, crop);
        state.element = widget;
        if (widget.getFamily() != null) {
            setFamily(widget.getFamily());
        }
        setPart("");
        setMode(widget.getMode());
        return result;
    }

    @Override
    public void drawText(String text) {
        drawText(text, state.getRelativeRegion());
    }

    @Override
    public void drawText(String text, Rect2i region) {
        UIStyle style = getCurrentStyle();
        if (style.isTextShadowed()) {
            drawTextRawShadowed(text, style.getFont(), style.getTextColor(), style.getTextShadowColor(), region, style.getHorizontalTextAlignment(),
                    style.getVerticalTextAlignment());
        } else {
            drawTextRaw(text, style.getFont(), style.getTextColor(), region, style.getHorizontalTextAlignment(), style.getVerticalTextAlignment());
        }
    }

    @Override
    public void drawTexture(TextureRegion texture) {
        drawTexture(texture, state.getRelativeRegion());
    }

    @Override
    public void drawTexture(TextureRegion texture, Color color) {
        drawTexture(texture, state.getRelativeRegion(), color);
    }

    @Override
    public void drawTexture(TextureRegion texture, Rect2i region) {
        drawTextureRaw(texture, region, getCurrentStyle().getTextureScaleMode());
    }

    @Override
    public void drawTexture(TextureRegion texture, Rect2i region, Color color) {
        drawTextureRaw(texture, region, color, getCurrentStyle().getTextureScaleMode());
    }

    @Override
    public void drawBackground() {
        Rect2i region = applyStyleToSize(getRegion());
        drawBackground(region);
    }

    private Rect2i applyStyleToSize(Rect2i region) {
        return applyStyleToSize(region, getCurrentStyle());
    }

    private Rect2i applyStyleToSize(Rect2i region, UIStyle style, Vector2i maxSize) {
        if (region.isEmpty()) {
            return region;
        }
        Vector2i size = applyStyleToSize(region.size(), style);
        size.x = Math.min(size.x, maxSize.x);
        size.y = Math.min(size.y, maxSize.y);

        int minX = region.minX() + style.getHorizontalAlignment().getOffset(size.x, region.width());
        int minY = region.minY() + style.getVerticalAlignment().getOffset(size.y, region.height());

        return Rect2i.createFromMinAndSize(minX, minY, size.x, size.y);
    }

    private Vector2i applyStyleToSize(Vector2i size, UIStyle style) {
        Vector2i result = new Vector2i(size);
        if (style.getFixedWidth() != 0) {
            result.x = style.getFixedWidth();
        } else {
            result.x = TeraMath.clamp(result.x, style.getMinWidth(), style.getMaxWidth());
        }

        if (style.getFixedHeight() != 0) {
            result.y = style.getFixedHeight();
        } else {
            result.y = TeraMath.clamp(result.y, style.getMinHeight(), style.getMaxHeight());
        }

        return result;
    }

    private Rect2i applyStyleToSize(Rect2i region, UIStyle style) {
        if (region.isEmpty()) {
            return region;
        }
        Vector2i size = applyStyleToSize(region.size(), style);

        int minX = region.minX() + style.getHorizontalAlignment().getOffset(size.x, region.width());
        int minY = region.minY() + style.getVerticalAlignment().getOffset(size.y, region.height());

        return Rect2i.createFromMinAndSize(minX, minY, size.x, size.y);
    }

    @Override
    public void drawBackground(Rect2i region) {
        UIStyle style = getCurrentStyle();
        if (style.getBackground() != null) {
            if (style.getBackgroundBorder().isEmpty()) {
                drawTextureRaw(style.getBackground(), region, style.getBackgroundScaleMode());
            } else {
                drawTextureRawBordered(style.getBackground(), region, style.getBackgroundBorder(), style.getBackgroundScaleMode() == ScaleMode.TILED);
            }
        }
    }

    @Override
    public void drawTextRaw(String text, Font font, Color color) {
        drawTextRawShadowed(text, font, color, Color.TRANSPARENT);
    }

    @Override
    public void drawTextRaw(String text, Font font, Color color, Rect2i region) {
        drawTextRawShadowed(text, font, color, Color.TRANSPARENT, region);
    }

    @Override
    public void drawTextRaw(String text, Font font, Color color, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        drawTextRawShadowed(text, font, color, Color.TRANSPARENT, region, hAlign, vAlign);
    }

    @Override
    public void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor) {
        drawTextRawShadowed(text, font, color, shadowColor, state.drawRegion);
    }

    @Override
    public void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region) {
        drawTextRawShadowed(text, font, color, shadowColor, region, HorizontalAlign.LEFT, VerticalAlign.TOP);
    }

    @Override
    public void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        Rect2i absoluteRegion = relativeToAbsolute(region);
        Rect2i cropRegion = absoluteRegion.intersect(state.cropRegion);
        if (!cropRegion.isEmpty()) {
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawTextOperation(text, font, hAlign, vAlign, absoluteRegion, cropRegion, color, shadowColor, state.getAlpha()));
            } else {
                drawTextInternal(text, font, hAlign, vAlign, absoluteRegion, cropRegion, color, shadowColor, state.getAlpha());
            }
        }
    }

    @Override
    public void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode) {
        drawTextureRaw(texture, region, mode, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureRaw(TextureRegion texture, Rect2i region, Color color, ScaleMode mode) {
        drawTextureRaw(texture, region, color, mode, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode, int ux, int uy, int uw, int uh) {
        drawTextureRaw(texture, region, mode,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTextureRaw(TextureRegion texture, Rect2i region, ScaleMode mode, float ux, float uy, float uw, float uh) {
        drawTextureRaw(texture, region, Color.WHITE, mode, ux, uy, uw, uh);
    }

    @Override
    public void drawTextureRaw(TextureRegion texture, Rect2i region, Color color, ScaleMode mode, float ux, float uy, float uw, float uh) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(region))) {
            return;
        }
        if (mode == ScaleMode.TILED) {
            drawTextureRawTiled(texture, region, ux, uy, uw, uh);
        } else {
            Rect2i absoluteRegion = relativeToAbsolute(region);
            Rect2i cropRegion = absoluteRegion.intersect(state.cropRegion);
            if (!cropRegion.isEmpty()) {
                if (state.drawOnTop) {
                    drawOnTopOperations.add(new DrawTextureOperation(texture, color, mode, absoluteRegion, cropRegion, ux, uy, uw, uh, state.getAlpha()));
                } else {
                    drawTextureInternal(texture, color, mode, absoluteRegion, cropRegion, ux, uy, uw, uh, state.getAlpha());
                }
            }
        }
    }

    private void drawTextureRawTiled(TextureRegion texture, Rect2i toArea, float ux, float uy, float uw, float uh) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(toArea))) {
            return;
        }
        int tileW = (int) (uw * texture.getWidth());
        int tileH = (int) (uh * texture.getHeight());
        if (tileW != 0 && tileH != 0) {
            int horizTiles = TeraMath.fastAbs((toArea.width() - 1) / tileW) + 1;
            int vertTiles = TeraMath.fastAbs((toArea.height() - 1) / tileH) + 1;

            int offsetX = toArea.width() - horizTiles * tileW;
            int offsetY = toArea.height() - vertTiles * tileH;

            try (SubRegion ignored = subRegion(toArea, true)) {
                for (int tileY = 0; tileY < vertTiles; tileY++) {
                    for (int tileX = 0; tileX < horizTiles; tileX++) {
                        Rect2i tileArea = Rect2i.createFromMinAndSize(toArea.minX() + offsetX + tileW * tileX, toArea.minY() + offsetY + tileH * tileY, tileW, tileH);
                        drawTextureRaw(texture, tileArea, ScaleMode.STRETCH, ux, uy, uw, uh);
                    }
                }
            }
        }
    }

    @Override
    public void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile) {
        drawTextureRawBordered(texture, region, border, tile, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile, int ux, int uy, int uw, int uh) {
        drawTextureRawBordered(texture, region, border, tile,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTextureRawBordered(TextureRegion texture, Rect2i region, Border border, boolean tile, float ux, float uy, float uw, float uh) {
        float top = (float) border.getTop() / texture.getHeight();
        float left = (float) border.getLeft() / texture.getWidth();
        float bottom = (float) border.getBottom() / texture.getHeight();
        float right = (float) border.getRight() / texture.getWidth();
        int centerHoriz = region.width() - border.getLeft() - border.getRight();
        int centerVert = region.height() - border.getTop() - border.getBottom();

        if (border.getTop() != 0) {
            // TOP-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTextureRaw(texture, Rect2i.createFromMinAndSize(region.minX(), region.minY(), border.getLeft(), border.getTop()), ScaleMode.STRETCH,
                        ux, uy, left, top);
            }
            // TOP BORDER
            Rect2i topArea = Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY(), centerHoriz, border.getTop());
            if (tile) {
                drawTextureRawTiled(texture, topArea, ux + left, uy, uw - left - right, top);
            } else {
                drawTextureRaw(texture, topArea, ScaleMode.STRETCH, ux + left, uy, uw - left - right, top);
            }
            // TOP-RIGHT CORNER
            if (border.getRight() != 0) {
                Rect2i area = Rect2i.createFromMinAndSize(region.maxX() - border.getRight() + 1, region.minY(), border.getRight(), border.getTop());
                drawTextureRaw(texture, area, ScaleMode.STRETCH, ux + uw - right, uy, right, top);
            }
        }
        // LEFT BORDER
        if (border.getLeft() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(region.minX(), region.minY() + border.getTop(), border.getLeft(), centerVert);
            if (tile) {
                drawTextureRawTiled(texture, area, ux, uy + top, left, uh - top - bottom);
            } else {
                drawTextureRaw(texture, area, ScaleMode.STRETCH, ux, uy + top, left, uh - top - bottom);
            }
        }
        // CENTER
        if (tile) {
            drawTextureRawTiled(texture, Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY() + border.getTop(), centerHoriz, centerVert),
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        } else {
            drawTextureRaw(texture, Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY() + border.getTop(), centerHoriz, centerVert), ScaleMode.STRETCH,
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        }

        // RIGHT BORDER
        if (border.getRight() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(region.maxX() - border.getRight() + 1, region.minY() + border.getTop(), border.getRight(), centerVert);
            if (tile) {
                drawTextureRawTiled(texture, area, ux + uw - right, uy + top, right, uh - top - bottom);
            } else {
                drawTextureRaw(texture, area, ScaleMode.STRETCH, ux + uw - right, uy + top, right, uh - top - bottom);
            }
        }
        if (border.getBottom() != 0) {
            // BOTTOM-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTextureRaw(texture, Rect2i.createFromMinAndSize(region.minX(), region.maxY() - border.getBottom() + 1, border.getLeft(), border.getBottom()),
                        ScaleMode.STRETCH, ux, uy + uw - bottom, left, bottom);
            }
            // BOTTOM BORDER
            Rect2i bottomArea = Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.maxY() - border.getBottom() + 1, centerHoriz, border.getBottom());
            if (tile) {
                drawTextureRawTiled(texture, bottomArea, ux + left, uy + uw - bottom, uw - left - right, bottom);
            } else {
                drawTextureRaw(texture, bottomArea, ScaleMode.STRETCH, ux + left, uy + uw - bottom, uw - left - right, bottom);
            }
            // BOTTOM-RIGHT CORNER
            if (border.getRight() != 0) {
                drawTextureRaw(texture, Rect2i.createFromMinAndSize(region.maxX() - border.getRight() + 1, region.maxY() - border.getBottom() + 1, border.getRight(),
                        border.getBottom()), ScaleMode.STRETCH, ux + uw - right, uy + uw - bottom, right, bottom);
            }
        }
    }

    @Override
    public void drawMaterial(Material material, Rect2i region) {
        Rect2i drawRegion = relativeToAbsolute(region);
        if (!state.cropRegion.overlaps(drawRegion)) {
            return;
        }
        material.setFloat("alpha", state.getAlpha());
        material.bindTextures();
        renderer.drawMaterialAt(material, drawRegion);
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        if (material == null) {
            logger.warn("Attempted to draw with nonexistent material");
            return;
        }
        if (mesh == null) {
            logger.warn("Attempted to draw nonexistent mesh");
            return;
        }

        Rect2i drawRegion = relativeToAbsolute(region);
        if (!state.cropRegion.overlaps(drawRegion)) {
            return;
        }

        renderer.drawMesh(mesh, material, drawRegion, drawRegion.intersect(state.cropRegion), rotation, offset, scale, state.getAlpha());
    }

    @Override
    public void drawMesh(Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        meshMat.setTexture("texture", texture);
        drawMesh(mesh, meshMat, region, rotation, offset, scale);
    }

    @Override
    public void addInteractionRegion(InteractionListener listener) {
        addInteractionRegion(listener, getCurrentStyle().getMargin().grow(applyStyleToSize(getRegion())));
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, Rect2i region) {
        Rect2i finalRegion = state.cropRegion.intersect(relativeToAbsolute(region));
        if (!finalRegion.isEmpty()) {
            listener.setFocusManager(nuiManager);
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawInteractionRegionOperation(finalRegion, listener, state.element));
            } else {
                interactionRegions.addLast(new InteractionRegion(finalRegion, listener, state.element));
            }
        }
    }

    private void crop(Rect2i cropRegion) {
        textureMat.setFloat4("croppingBoundaries", cropRegion.minX(), cropRegion.maxX() + 1, cropRegion.minY(), cropRegion.maxY() + 1);
    }

    private Rect2i relativeToAbsolute(Rect2i region) {
        return Rect2i.createFromMinAndSize(region.minX() + state.drawRegion.minX(), region.minY() + state.drawRegion.minY(), region.width(), region.height());
    }

    private void drawTextureInternal(TextureRegion texture, Color color, ScaleMode mode, Rect2i absoluteRegion, Rect2i cropRegion,
                                     float ux, float uy, float uw, float uh, float alpha) {
        Vector2f scale = mode.scaleForRegion(absoluteRegion, texture.getWidth(), texture.getHeight());
        Rect2f textureArea = texture.getRegion();
        textureMat.setFloat2("scale", scale);
        textureMat.setFloat2("offset",
                absoluteRegion.minX() + 0.5f * (absoluteRegion.width() - scale.x),
                absoluteRegion.minY() + 0.5f * (absoluteRegion.height() - scale.y));
        textureMat.setFloat2("texOffset", textureArea.minX() + ux * textureArea.width(), textureArea.minY() + uy * textureArea.height());
        textureMat.setFloat2("texSize", uw * textureArea.width(), uh * textureArea.height());
        textureMat.setTexture("texture", texture.getTexture());
        textureMat.setFloat4("color", color.rf(), color.gf(), color.bf(), color.af() * alpha);
        textureMat.bindTextures();
        if (mode == ScaleMode.SCALE_FILL) {
            if (!cropRegion.equals(state.cropRegion)) {
                crop(cropRegion);
                billboard.render();
                crop(state.cropRegion);
            } else {
                billboard.render();
            }
        } else {
            billboard.render();
        }
    }

    private void drawTextInternal(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion, Rect2i cropRegion,
                                  Color color, Color shadowColor, float alpha) {
        TextCacheKey key = new TextCacheKey(text, font, absoluteRegion.width(), hAlign);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = TextLineBuilder.getLines(font, text, absoluteRegion.width());
        if (fontMesh == null) {
            fontMesh = font.createTextMesh(lines, absoluteRegion.width(), hAlign);
            cachedText.put(key, fontMesh);
        }

        Vector2i offset = new Vector2i(absoluteRegion.minX(), absoluteRegion.minY());
        offset.y += vAlign.getOffset(lines.size() * font.getLineHeight(), absoluteRegion.height());


        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            entry.getKey().setFloat4("croppingBoundaries", cropRegion.minX(), cropRegion.maxX() + 1, cropRegion.minY(), cropRegion.maxY() + 1);
            if (shadowColor.a() != 0) {
                entry.getKey().setFloat2("offset", offset.x + 1, offset.y + 1);
                Vector4f shadowValues = shadowColor.toVector4f();
                shadowValues.w *= alpha;
                entry.getKey().setFloat4("color", shadowValues);
                entry.getValue().render();
            }

            entry.getKey().setFloat2("offset", offset.x, offset.y);
            Vector4f colorValues = color.toVector4f();
            colorValues.w *= alpha;
            entry.getKey().setFloat4("color", colorValues);
            entry.getValue().render();
        }
    }

    /**
     * The state of the canvas
     */
    private static class CanvasState {
        public UISkin skin;
        public String family = "";
        public UIWidget element;
        public String part = "";
        public String mode = "";

        public Rect2i drawRegion;
        public Rect2i cropRegion;

        private float alpha = 1.0f;
        private float baseAlpha = 1.0f;

        private boolean drawOnTop;

        public CanvasState(CanvasState previous, Rect2i drawRegion) {
            this(previous, drawRegion, (previous != null) ? previous.cropRegion : drawRegion);
        }

        public CanvasState(CanvasState previous, Rect2i drawRegion, Rect2i cropRegion) {
            if (previous != null) {
                this.skin = previous.skin;
                this.family = previous.family;
                this.element = previous.element;
                this.part = previous.part;
                this.mode = previous.mode;
                this.drawOnTop = previous.drawOnTop;
                baseAlpha = previous.getAlpha();
            }
            this.drawRegion = drawRegion;
            this.cropRegion = cropRegion;
        }

        public float getAlpha() {
            return alpha * baseAlpha;
        }

        public UIStyle getCurrentStyle() {
            return skin.getStyleFor(family, element.getClass(), part, mode);
        }

        public Rect2i getRelativeRegion() {
            return Rect2i.createFromMinAndMax(0, 0, drawRegion.width(), drawRegion.height());
        }
    }

    /**
     * A SubRegion implementation for this canvas.
     */
    private class LwjglSubRegion implements SubRegion {

        public boolean croppingRegion;
        private CanvasState previousState;
        private boolean disposed;

        public LwjglSubRegion(Rect2i region, boolean crop) {
            previousState = state;
            subregionStack.push(this);

            int left = region.minX() + state.drawRegion.minX();
            int right = region.maxX() + state.drawRegion.minX();
            int top = region.minY() + state.drawRegion.minY();
            int bottom = region.maxY() + state.drawRegion.minY();
            Rect2i subRegion = Rect2i.createFromMinAndMax(left, top, right, bottom);
            if (crop) {
                Rect2i cropRegion = subRegion.intersect(state.cropRegion);
                if (cropRegion.isEmpty()) {
                    state = new CanvasState(state, subRegion, cropRegion);
                } else if (!cropRegion.equals(state.cropRegion)) {
                    state = new CanvasState(state, subRegion, cropRegion);
                    crop(cropRegion);
                    croppingRegion = true;
                } else {
                    state = new CanvasState(state, subRegion);
                }
            } else {
                state = new CanvasState(state, subRegion);
            }
        }

        @Override
        public void close() {
            if (!disposed) {
                disposed = true;
                LwjglSubRegion region = subregionStack.pop();
                while (!region.equals(this)) {
                    logger.error("UI SubRegions being closed in an incorrect order");
                    region.close();
                    region = subregionStack.pop();
                }
                if (croppingRegion) {
                    crop(previousState.cropRegion);
                }
                state = previousState;
            }
        }
    }

    /**
     * A key that identifies an entry in the text cache. It contains the elements that affect the generation of mesh for text rendering.
     */
    private static class TextCacheKey {
        private String text;
        private Font font;
        private int width;
        private HorizontalAlign alignment;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlign alignment) {
            this.text = text;
            this.font = font;
            this.width = maxWidth;
            this.alignment = alignment;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TextCacheKey) {
                TextCacheKey other = (TextCacheKey) obj;
                return Objects.equals(text, other.text) && Objects.equals(font, other.font)
                        && Objects.equals(width, other.width) && Objects.equals(alignment, other.alignment);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, width, alignment);
        }
    }

    private static class InteractionRegion {
        public InteractionListener listener;
        public Rect2i region;
        public UIWidget element;

        public InteractionRegion(Rect2i region, InteractionListener listener, UIWidget element) {
            this.listener = listener;
            this.region = region;
            this.element = element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InteractionRegion) {
                InteractionRegion other = (InteractionRegion) obj;
                return Objects.equals(other.listener, listener);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    private interface DrawOperation {
        void draw();
    }

    private final class DrawTextureOperation implements DrawOperation {

        private Color color;
        private ScaleMode mode;
        private TextureRegion texture;
        private Rect2i absoluteRegion;
        private Rect2i cropRegion;
        private float ux;
        private float uy;
        private float uw;
        private float uh;
        private float alpha;

        private DrawTextureOperation(TextureRegion texture, Color color, ScaleMode mode, Rect2i absoluteRegion,
                                     Rect2i cropRegion, float ux, float uy, float uw, float uh, float alpha) {
            this.color = color;
            this.mode = mode;
            this.texture = texture;
            this.absoluteRegion = absoluteRegion;
            this.cropRegion = cropRegion;
            this.ux = ux;
            this.uy = uy;
            this.uw = uw;
            this.uh = uh;
            this.alpha = alpha;
        }

        @Override
        public void draw() {
            drawTextureInternal(texture, color, mode, absoluteRegion, cropRegion, ux, uy, uw, uh, alpha);
        }
    }

    private final class DrawTextOperation implements DrawOperation {
        private String text;
        private Font font;
        private Rect2i absoluteRegion;
        private HorizontalAlign hAlign;
        private VerticalAlign vAlign;
        private Rect2i cropRegion;
        private Color shadowColor;
        private Color color;
        private float alpha;

        private DrawTextOperation(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion, Rect2i cropRegion,
                                  Color color, Color shadowColor, float alpha) {
            this.text = text;
            this.font = font;
            this.absoluteRegion = absoluteRegion;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
            this.cropRegion = cropRegion;
            this.shadowColor = shadowColor;
            this.color = color;
            this.alpha = alpha;
        }

        @Override
        public void draw() {
            drawTextInternal(text, font, hAlign, vAlign, absoluteRegion, cropRegion, color, shadowColor, alpha);
        }
    }

    private final class DrawInteractionRegionOperation implements DrawOperation {

        private final Rect2i region;
        private final InteractionListener listener;
        private final UIWidget currentElement;

        public DrawInteractionRegionOperation(Rect2i region, InteractionListener listener, UIWidget currentElement) {
            this.region = region;
            this.listener = listener;
            this.currentElement = currentElement;
        }

        @Override
        public void draw() {
            interactionRegions.addLast(new InteractionRegion(region, listener, currentElement));
        }
    }

}
