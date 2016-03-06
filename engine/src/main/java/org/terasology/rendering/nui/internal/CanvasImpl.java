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
package org.terasology.rendering.nui.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.Time;
import org.terasology.input.InputSystem;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.Border;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UIStyle;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UITooltip;
import org.terasology.rendering.opengl.FrameBufferObject;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
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
    private final KeyboardDevice keyboard;
    private final MouseDevice mouse;

    private CanvasState state;

    private Material meshMat;
    private Texture whiteTexture;

    private List<DrawOperation> drawOnTopOperations = Lists.newArrayList();

    private boolean focusDrawn;

    // Interaction region handling
    private Deque<InteractionRegion> interactionRegions = Queues.newArrayDeque();
    private Set<InteractionRegion> mouseOverRegions = Sets.newLinkedHashSet();
    private InteractionRegion topMouseOverRegion;
    private float tooltipTime;
    private Vector2i lastTooltipPosition = new Vector2i();
    private UITooltip tooltipWidget = new UITooltip();

    private InteractionRegion clickedRegion;

    // Double click handling
    private long lastClickTime;
    private MouseInput lastClickButton;
    private Vector2i lastClickPosition = new Vector2i();

    private CanvasRenderer renderer;

    public CanvasImpl(NUIManager nuiManager, Context context, CanvasRenderer renderer) {
        this.renderer = renderer;
        this.nuiManager = nuiManager;
        this.time = context.get(Time.class);
        this.keyboard = context.get(InputSystem.class).getKeyboard();
        this.mouse = context.get(InputSystem.class).getMouseDevice();
        this.meshMat = Assets.getMaterial("engine:UILitMesh").get();
        this.whiteTexture = Assets.getTexture("engine:white").get();
    }

    @Override
    public void preRender() {
        interactionRegions.clear();
        Vector2i size = renderer.getTargetSize();
        state = new CanvasState(null, Rect2i.createFromMinAndSize(0, 0, size.x, size.y));
        renderer.preRender();
        renderer.crop(state.cropRegion);
        focusDrawn = false;
    }

    @Override
    public void postRender() {
        drawOnTopOperations.forEach(DrawOperation::draw);
        drawOnTopOperations.clear();

        if (topMouseOverRegion != null && time.getGameTime() >= tooltipTime && getSkin() != null) {
            tooltipWidget.setAttachment(topMouseOverRegion.getTooltip());
            drawWidget(tooltipWidget);
        } else {
            tooltipWidget.setAttachment(null);
        }

        renderer.postRender();
        if (!focusDrawn) {
            nuiManager.setFocus(null);
        }
    }

    @Override
    public void processMousePosition(Vector2i position) {
        if (clickedRegion != null) {
            Vector2i relPos = new Vector2i(position);
            relPos.sub(clickedRegion.offset);
            clickedRegion.listener.onMouseDrag(new NUIMouseDragEvent(mouse, keyboard, relPos));
        }

        Set<InteractionRegion> newMouseOverRegions = Sets.newLinkedHashSet();
        Iterator<InteractionRegion> iter = interactionRegions.descendingIterator();
        while (iter.hasNext()) {
            InteractionRegion next = iter.next();
            if (next.region.contains(position)) {
                Vector2i relPos = new Vector2i(position);
                relPos.sub(next.offset);
                boolean isTopMostElement = newMouseOverRegions.isEmpty();
                next.listener.onMouseOver(new NUIMouseOverEvent(mouse, keyboard, relPos, isTopMostElement));
                newMouseOverRegions.add(next);
            }
        }

        mouseOverRegions.stream().filter(region -> !newMouseOverRegions.contains(region)).forEach(region ->
                region.listener.onMouseLeave());

        if (clickedRegion != null && !interactionRegions.contains(clickedRegion)) {
            clickedRegion = null;
        }

        mouseOverRegions = newMouseOverRegions;

        if (mouseOverRegions.isEmpty()) {
            topMouseOverRegion = null;
        } else {
            InteractionRegion newTopMouseOverRegion = mouseOverRegions.iterator().next();
            if (!newTopMouseOverRegion.equals(topMouseOverRegion)) {
                topMouseOverRegion = newTopMouseOverRegion;
                tooltipTime = time.getGameTime() + newTopMouseOverRegion.element.getTooltipDelay();
                lastTooltipPosition.set(position);
            } else {
                if (lastTooltipPosition.gridDistance(position) > MAX_DOUBLE_CLICK_DISTANCE) {
                    tooltipTime = time.getGameTime() + newTopMouseOverRegion.element.getTooltipDelay();
                    lastTooltipPosition.set(position);
                }
            }

        }
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
                relPos.sub(next.offset);
                if (possibleDoubleClick && nuiManager.getFocus() == next.element) {
                    if (next.listener.onMouseDoubleClick(createDoubleClickEvent(button, relPos))) {
                        clickedRegion = next;
                        return true;
                    }
                } else if (next.listener.onMouseClick(createClickEvent(button, relPos))) {
                    clickedRegion = next;
                    nuiManager.setFocus(next.element);
                    return true;
                }
            }
        }
        return false;
    }

    private NUIMouseClickEvent createClickEvent(MouseInput button, Vector2i relPos) {
        return new NUIMouseClickEvent(mouse, keyboard, relPos, button);
    }

    private NUIMouseDoubleClickEvent createDoubleClickEvent(MouseInput button, Vector2i relPos) {
        return new NUIMouseDoubleClickEvent(mouse, keyboard, relPos, button);
    }

    @Override
    public boolean processMouseRelease(MouseInput button, Vector2i pos) {
        if (clickedRegion != null) {
            Vector2i relPos = new Vector2i(pos);
            relPos.sub(clickedRegion.region.min());
            clickedRegion.listener.onMouseRelease(new NUIMouseReleaseEvent(mouse, keyboard, relPos, button));
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
                if (next.listener.onMouseWheel(new NUIMouseWheelEvent(mouse, keyboard, relPos, wheelTurns))) {
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
        return new SubRegionImpl(region, crop);
    }

    @Override
    public SubRegion subRegionFBO(ResourceUrn uri, BaseVector2i size) {
        return new SubRegionFBOImpl(uri, size);
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
        Preconditions.checkNotNull(skin);
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
        UISkin skin = (widget.getSkin() != null) ? widget.getSkin() : state.skin;
        UIStyle elementStyle = skin.getStyleFor(family, widget.getClass(), UIWidget.BASE_PART, widget.getMode());
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
        UIStyle elementStyle = state.skin.getStyleFor(family, widget.getClass(), UIWidget.BASE_PART, widget.getMode());
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

        if (nuiManager.getFocus() == element) {
            focusDrawn = true;
        }
        String family = (element.getFamily() != null) ? element.getFamily() : state.family;
        UISkin skin = (element.getSkin() != null) ? element.getSkin() : state.skin;
        UIStyle newStyle = skin.getStyleFor(family, element.getClass(), UIWidget.BASE_PART, element.getMode());
        Rect2i regionArea;
        try (SubRegion ignored = subRegionForWidget(element, region, false)) {
            regionArea = applyStyleToSize(region, newStyle, calculateMaximumSize(element));
        }

        try (SubRegion ignored = subRegionForWidget(element, regionArea, false)) {
            if (element.isSkinAppliedByCanvas()) {
                drawBackground();
                try (SubRegion withMargin = subRegionForWidget(element, newStyle.getMargin().shrink(Rect2i.createFromMinAndSize(Vector2i.zero(), regionArea.size())), false)) {
                    drawStyledWidget(element);
                }
            } else {
                drawStyledWidget(element);
            }
        }
    }

    private void drawStyledWidget(UIWidget element) {
        if (element.getTooltip() != null) {
            // Integrated tooltip support - without this, setting a tooltip value does not make a tooltip work
            // unless an interaction listener is explicitly added by the widget.
            addInteractionRegion(new BaseInteractionListener());
        }
        element.onDraw(this);
    }

    private SubRegion subRegionForWidget(UIWidget widget, Rect2i region, boolean crop) {
        SubRegion result = subRegion(region, crop);
        state.element = widget;
        if (widget.getSkin() != null) {
            setSkin(widget.getSkin());
        }
        if (widget.getFamily() != null) {
            setFamily(widget.getFamily());
        }
        setPart(UIWidget.BASE_PART);
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
            drawTextRawShadowed(text, style.getFont(), style.getTextColor(), style.getTextShadowColor(), style.isTextUnderlined(), region, style.getHorizontalTextAlignment(),
                    style.getVerticalTextAlignment());
        } else {
            drawTextRaw(text, style.getFont(), style.getTextColor(), style.isTextUnderlined(), region, style.getHorizontalTextAlignment(), style.getVerticalTextAlignment());
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
        if (region.isEmpty()) {
            return;
        }
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
    public void drawTextRaw(String text, Font font, Color color, boolean underlined, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        drawTextRawShadowed(text, font, color, Color.TRANSPARENT, underlined, region, hAlign, vAlign);
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
        drawTextRawShadowed(text, font, color, shadowColor, false, region, hAlign, vAlign);
    }

    @Override
    public void drawTextRawShadowed(String text, Font font, Color color, Color shadowColor, boolean underline, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        Rect2i absoluteRegion = relativeToAbsolute(region);
        Rect2i cropRegion = absoluteRegion.intersect(state.cropRegion);
        if (!cropRegion.isEmpty()) {
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawTextOperation(text, font, hAlign, vAlign, absoluteRegion, cropRegion, color, shadowColor, state.getAlpha(), underline));
            } else {
                renderer.drawText(text, font, hAlign, vAlign, absoluteRegion, color, shadowColor, state.getAlpha(), underline);
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
        Rect2i absoluteRegion = relativeToAbsolute(region);
        Rect2i cropRegion = absoluteRegion.intersect(state.cropRegion);
        if (!cropRegion.isEmpty()) {
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawTextureOperation(texture, color, mode, absoluteRegion, cropRegion, ux, uy, uw, uh, state.getAlpha()));
            } else {
                renderer.drawTexture(texture, color, mode, absoluteRegion, ux, uy, uw, uh, state.getAlpha());
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
        if (!state.cropRegion.overlaps(relativeToAbsolute(region))) {
            return;
        }
        Rect2i absoluteRegion = relativeToAbsolute(region);
        Rect2i cropRegion = absoluteRegion.intersect(state.cropRegion);
        if (!cropRegion.isEmpty()) {
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawBorderedTextureOperation(texture, absoluteRegion, border, tile, cropRegion, ux, uy, uw, uh, state.getAlpha()));
            } else {
                renderer.drawTextureBordered(texture, absoluteRegion, border, tile, ux, uy, uw, uh, state.getAlpha());
            }
        }
    }

    @Override
    public void drawMaterial(Material material, Rect2i region) {
        if (material.isRenderable()) {
            Rect2i drawRegion = relativeToAbsolute(region);
            if (!state.cropRegion.overlaps(drawRegion)) {
                return;
            }
            material.setFloat("alpha", state.getAlpha());
            material.bindTextures();
            renderer.drawMaterialAt(material, drawRegion);
        }
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
        addInteractionRegion(listener, (UIWidget) null, getCurrentStyle().getMargin().grow(applyStyleToSize(getRegion())));
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, String tooltip) {
        addInteractionRegion(listener, tooltip, getCurrentStyle().getMargin().grow(applyStyleToSize(getRegion())));
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, String tooltip, Rect2i region) {
        UIWidget tooltipLabelWidget = (tooltip == null || tooltip.isEmpty()) ? null : new UILabel(tooltip);
        addInteractionRegion(listener, tooltipLabelWidget, region);
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, Rect2i region) {
        addInteractionRegion(listener, (UIWidget) null, region);
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, UIWidget tooltip) {
        addInteractionRegion(listener, tooltip, getCurrentStyle().getMargin().grow(applyStyleToSize(getRegion())));
    }

    @Override
    public void addInteractionRegion(InteractionListener listener, UIWidget tooltip, Rect2i region) {
        Vector2i offset = state.drawRegion.min();
        Rect2i finalRegion = state.cropRegion.intersect(relativeToAbsolute(region));
        if (!finalRegion.isEmpty()) {
            listener.setFocusManager(nuiManager);
            if (state.drawOnTop) {
                drawOnTopOperations.add(new DrawInteractionRegionOperation(finalRegion, offset, listener, state.element, tooltip));
            } else {
                interactionRegions.addLast(new InteractionRegion(finalRegion, offset, listener, state.element, tooltip));
            }
        }
    }

    @Override
    public void drawLine(int startX, int startY, int endX, int endY, Color color) {
        int sx = startX + state.drawRegion.minX();
        int sy = startY + state.drawRegion.minY();
        int ex = state.drawRegion.minX() + endX;
        int ey = state.drawRegion.minY() + endY;

        if (state.drawOnTop) {
            drawOnTopOperations.add(new DrawLineOperation(sx, sy, ex, ey, color));
        } else {
            renderer.drawLine(sx, sy, ex, ey, color);
        }
    }

    @Override
    public void drawFilledRectangle(Rect2i region, Color color) {
        drawTexture(whiteTexture, region, color);
    }

    private Rect2i relativeToAbsolute(Rect2i region) {
        return Rect2i.createFromMinAndSize(region.minX() + state.drawRegion.minX(), region.minY() + state.drawRegion.minY(), region.width(), region.height());
    }

    /**
     * The state of the canvas
     */
    private static class CanvasState {
        public UISkin skin = Assets.getSkin("engine:default").get();
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
            return Rect2i.createFromMinAndSize(0, 0, drawRegion.width(), drawRegion.height());
        }
    }

    /**
     * A SubRegion implementation for this canvas.
     */
    private class SubRegionImpl implements SubRegion {

        public boolean croppingRegion;
        private CanvasState previousState;
        private boolean disposed;

        public SubRegionImpl(Rect2i region, boolean crop) {
            previousState = state;

            int left = TeraMath.addClampAtMax(region.minX(), state.drawRegion.minX());
            int right = TeraMath.addClampAtMax(region.maxX(), state.drawRegion.minX());
            int top = TeraMath.addClampAtMax(region.minY(), state.drawRegion.minY());
            int bottom = TeraMath.addClampAtMax(region.maxY(), state.drawRegion.minY());
            Rect2i subRegion = Rect2i.createFromMinAndMax(left, top, right, bottom);
            if (crop) {
                Rect2i cropRegion = subRegion.intersect(state.cropRegion);
                if (cropRegion.isEmpty()) {
                    state = new CanvasState(state, subRegion, cropRegion);
                } else if (!cropRegion.equals(state.cropRegion)) {
                    state = new CanvasState(state, subRegion, cropRegion);
                    renderer.crop(cropRegion);
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
                if (croppingRegion) {
                    renderer.crop(previousState.cropRegion);
                }
                state = previousState;
            }
        }
    }

    private final class SubRegionFBOImpl implements SubRegion {
        private FrameBufferObject fbo;
        private CanvasState previousState;

        private SubRegionFBOImpl(ResourceUrn uri, BaseVector2i size) {
            previousState = state;

            fbo = renderer.getFBO(uri, size);
            state = new CanvasState(state, Rect2i.createFromMinAndSize(new Vector2i(), size));
            fbo.bindFrame();
        }

        @Override
        public void close() {
            fbo.unbindFrame();
            state = previousState;
        }
    }

    private static class InteractionRegion {
        public InteractionListener listener;
        public Rect2i region;
        public Vector2i offset;
        public UIWidget element;
        public UIWidget tooltipOverride;

        public InteractionRegion(Rect2i region, Vector2i offset, InteractionListener listener, UIWidget element, UIWidget tooltipOverride) {
            this.listener = listener;
            this.region = region;
            this.offset = offset;
            this.element = element;
            this.tooltipOverride = tooltipOverride;
        }

        public UIWidget getTooltip() {
            if (tooltipOverride == null) {
                return element.getTooltip();
            }
            return tooltipOverride;
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
            renderer.crop(cropRegion);
            renderer.drawTexture(texture, color, mode, absoluteRegion, ux, uy, uw, uh, alpha);
            renderer.crop(state.cropRegion);
        }
    }

    private final class DrawBorderedTextureOperation implements DrawOperation {

        private TextureRegion texture;
        private Border border;
        private boolean tile;
        private Rect2i absoluteRegion;
        private Rect2i cropRegion;
        private float ux;
        private float uy;
        private float uw;
        private float uh;
        private float alpha;

        private DrawBorderedTextureOperation(TextureRegion texture, Rect2i absoluteRegion, Border border, boolean tile,
                                             Rect2i cropRegion, float ux, float uy, float uw, float uh, float alpha) {
            this.texture = texture;
            this.tile = tile;
            this.absoluteRegion = absoluteRegion;
            this.border = border;
            this.cropRegion = cropRegion;
            this.ux = ux;
            this.uy = uy;
            this.uw = uw;
            this.uh = uh;
            this.alpha = alpha;
        }

        @Override
        public void draw() {
            renderer.crop(cropRegion);
            renderer.drawTextureBordered(texture, absoluteRegion, border, tile, ux, uy, uw, uh, alpha);
            renderer.crop(state.cropRegion);
        }
    }

    private final class DrawLineOperation implements DrawOperation {

        private int x0;
        private int y0;
        private int x1;
        private int y1;
        private Color color;

        private DrawLineOperation(int x0, int y0, int x1, int y1, Color color) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.color = color;
        }

        @Override
        public void draw() {
            renderer.drawLine(x0, y0, x1, y1, color);
        }
    }

    private final class DrawTextOperation implements DrawOperation {
        private final String text;
        private final Font font;
        private final Rect2i absoluteRegion;
        private final HorizontalAlign hAlign;
        private final VerticalAlign vAlign;
        private final Rect2i cropRegion;
        private final Color shadowColor;
        private final Color color;
        private final float alpha;
        private final boolean underline;

        private DrawTextOperation(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion, Rect2i cropRegion,
                                  Color color, Color shadowColor, float alpha, boolean underline) {
            this.text = text;
            this.font = font;
            this.absoluteRegion = absoluteRegion;
            this.hAlign = hAlign;
            this.vAlign = vAlign;
            this.cropRegion = cropRegion;
            this.shadowColor = shadowColor;
            this.color = color;
            this.alpha = alpha;
            this.underline = underline;
        }

        @Override
        public void draw() {
            renderer.crop(cropRegion);
            renderer.drawText(text, font, hAlign, vAlign, absoluteRegion, color, shadowColor, alpha, underline);
            renderer.crop(state.cropRegion);
        }
    }

    private final class DrawInteractionRegionOperation implements DrawOperation {

        private final Vector2i offset;
        private final Rect2i region;
        private final InteractionListener listener;
        private final UIWidget currentElement;
        private final UIWidget tooltipOverride;

        public DrawInteractionRegionOperation(Rect2i region, Vector2i offset, InteractionListener listener, UIWidget currentElement, UIWidget tooltipOverride) {
            this.region = region;
            this.listener = listener;
            this.offset = offset;
            this.currentElement = currentElement;
            this.tooltipOverride = tooltipOverride;
        }

        @Override
        public void draw() {
            interactionRegions.addLast(new InteractionRegion(region, offset, listener, currentElement, tooltipOverride));
        }
    }

}
