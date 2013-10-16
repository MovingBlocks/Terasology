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

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.assets.texture.Texture;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glScissor;

/**
 * @author Immortius
 */
public class LwjglCanvas implements Canvas {

    private static final Logger logger = LoggerFactory.getLogger(LwjglCanvas.class);

    private CanvasState state;

    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    private Deque<LwjglSubRegion> subregionStack = Queues.newArrayDeque();

    private Mesh simpleMesh;
    private Material textureMat = Assets.getMaterial("engine:uiTexture");

    public LwjglCanvas() {
        // TODO: Make an asset for this.
        simpleMesh = new MeshBuilder().addPoly(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(1, 1, 0), new Vector3f(0, 1, 0))
                .addTexCoord(0, 0)
                .addTexCoord(1, 0)
                .addTexCoord(1, 1)
                .addTexCoord(0, 1).build();
    }

    public void preRender() {
        state = new CanvasState(Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight()));
        glScissor(0, 0, Display.getWidth(), Display.getHeight());
        glEnable(GL_SCISSOR_TEST);
    }

    public void postRender() {
        Util.checkGLError();
        glDisable(GL_SCISSOR_TEST);
        if (!subregionStack.isEmpty()) {
            logger.error("Subregions are not being correctly ended");
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
    }

    @Override
    public SubRegion subRegion(Rect2i region, boolean crop) {
        return new LwjglSubRegion(region, crop);
    }

    @Override
    public Vector2i size() {
        return new Vector2i(state.region.width(), state.region.height());
    }

    @Override
    public void setOffset(Vector2i offset) {
        state.offset.set(offset);
    }

    @Override
    public void setOffset(int x, int y) {
        state.offset.set(x, y);
    }

    @Override
    public Vector2i getOffset() {
        return new Vector2i(state.offset);
    }

    @Override
    public void setTextColor(Color color) {
        state.textColor = color;
    }

    @Override
    public void drawText(Font font, String text) {
        drawTextShadowed(font, text, null);
    }

    @Override
    public void drawText(Font font, String text, int maxWidth) {
        drawTextShadowed(font, text, maxWidth, null);
    }

    @Override
    public void drawText(Font font, String text, int maxWidth, HorizontalAlignment alignment) {
        drawTextShadowed(font, text, maxWidth, alignment, null);
    }

    @Override
    public void drawTextShadowed(Font font, String text, Color shadowColor) {
        drawTextShadowed(font, text, Integer.MAX_VALUE, shadowColor);
    }

    @Override
    public void drawTextShadowed(Font font, String text, int maxWidth, Color shadowColor) {
        drawTextShadowed(font, text, maxWidth, HorizontalAlignment.LEFT, shadowColor);
    }

    @Override
    public void drawTextShadowed(Font font, String text, int maxWidth, HorizontalAlignment alignment, Color shadowColor) {
        TextCacheKey key = new TextCacheKey(text, font, maxWidth, alignment);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = LineBuilder.getLines(font, text, maxWidth);
        if (fontMesh == null) {
            fontMesh = font.createTextMesh(lines, maxWidth, alignment);
            cachedText.put(key, fontMesh);
        }

        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            if (shadowColor != null) {
                entry.getKey().setFloat2("offset", state.getAbsoluteOffsetX() + 1, state.getAbsoluteOffsetY() + 1);
                entry.getKey().setFloat4("color", shadowColor.toVector4f());
                entry.getValue().render();
            }

            entry.getKey().setFloat2("offset", state.getAbsoluteOffsetX(), state.getAbsoluteOffsetY());
            entry.getKey().setFloat4("color", state.textColor.toVector4f());
            entry.getValue().render();
        }

        state.offset.y += lines.size() * font.getLineHeight();
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode) {
        drawTexture(texture, toArea, mode, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, int ux, int uy, int uw, int uh) {
        drawTexture(texture, toArea, mode,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, float ux, float uy, float uw, float uh) {
        Util.checkGLError();
        Vector2f scale = mode.scaleForRegion(toArea, texture.getWidth(), texture.getHeight());
        textureMat.setFloat2("scale", scale);
        textureMat.setFloat2("offset",
                state.region.minX() + toArea.minX() + 0.5f * (toArea.width() - scale.x),
                state.region.minY() + toArea.minY() + 0.5f * (toArea.height() - scale.y));
        textureMat.setFloat2("texOffset", ux, uy);
        textureMat.setFloat2("texSize", uw, uh);
        textureMat.setTexture("texture", texture);
        textureMat.bindTextures();
        if (mode == ScaleMode.SCALE_FILL) {
            Rect2i cropRegion = Rect2i.createFromMinAndSize(toArea.minX() + state.region.minX(), toArea.minY() + state.region.minY(), toArea.width(), toArea.height());
            if (!cropRegion.equals(state.cropRegion)) {
                crop(cropRegion);
                simpleMesh.render();
                crop(state.cropRegion);
            } else {
                simpleMesh.render();
            }
        } else {
            simpleMesh.render();
        }
        Util.checkGLError();
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile) {
        drawTextureBordered(texture, toArea, border, tile, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, int ux, int uy, int uw, int uh) {
        drawTextureBordered(texture, toArea, border, tile,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, float ux, float uy, float uw, float uh) {
        float top = (float) border.getTop() / texture.getHeight();
        float left = (float) border.getLeft() / texture.getWidth();
        float bottom = (float) border.getBottom() / texture.getHeight();
        float right = (float) border.getRight() / texture.getWidth();
        int centerHoriz = toArea.width() - border.getLeft() - border.getRight();
        int centerVert = toArea.height() - border.getTop() - border.getBottom();

        if (border.getTop() != 0) {
            // TOP-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX(), toArea.minY(), border.getLeft(), border.getTop()), ScaleMode.STRETCH,
                        ux, uy, left, top);
            }
            // TOP BORDER
            drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.minY(), centerHoriz, border.getTop()), ScaleMode.STRETCH,
                    ux + left, uy, uw - left - right, top);
            // TOP-RIGHT CORNER
            if (border.getRight() != 0) {
                Rect2i area = Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.minY(), border.getRight(), border.getTop());
                drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy, right, top);
            }
        }
        // LEFT BORDER
        if (border.getLeft() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(toArea.minX(), toArea.minY() + border.getTop(), border.getLeft(), centerVert);
            drawTexture(texture, area, ScaleMode.STRETCH, ux, uy + top, left, uh - top - bottom);
        }
        // CENTER
        drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.minY() + border.getTop(), centerHoriz, centerVert), ScaleMode.STRETCH,
                ux + left, uy + top, uw - left - right, uh - top - bottom);

        // RIGHT BORDER
        if (border.getRight() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.minY() + border.getTop(), border.getRight(), centerVert);
            drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy + top, right, uh - top - bottom);
        }
        if (border.getBottom() != 0) {
            // BOTTOM-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX(), toArea.maxY() - border.getBottom(), border.getLeft(), border.getBottom()), ScaleMode.STRETCH,
                        ux, uy + uw - bottom, left, bottom);
            }
            // BOTTOM BORDER
            drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.maxY() - border.getBottom(), centerHoriz, border.getBottom()),
                    ScaleMode.STRETCH, ux + left, uy + uw - bottom, uw - left - right, bottom);
            // BOTTOM-RIGHT CORNER
            if (border.getRight() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.maxY() - border.getBottom(), border.getRight(), border.getBottom()),
                        ScaleMode.STRETCH, ux + uw - right, uy + uw - bottom, right, bottom);
            }
        }
    }

    private void crop(Rect2i region) {
        crop(region.minX(), region.minY(), region.width(), region.height());
    }

    private void crop(int x, int y, int w, int h) {
        glScissor(x, Display.getHeight() - y - h, w, h);
    }

    private static class CanvasState {
        public Rect2i region;
        public Rect2i cropRegion;
        public Color textColor = Color.WHITE;

        public Vector2i offset = new Vector2i();

        public CanvasState(Rect2i region) {
            this(region, region);
        }

        public CanvasState(Rect2i region, Rect2i cropRegion) {
            this.region = region;
            this.cropRegion = cropRegion;
        }

        public int getAbsoluteOffsetX() {
            return offset.x + region.minX();
        }

        public int getAbsoluteOffsetY() {
            return offset.y + region.minY();
        }
    }

    private class LwjglSubRegion implements SubRegion {

        public boolean croppingRegion;
        private CanvasState previousState;
        private boolean disposed;

        public LwjglSubRegion(Rect2i region, boolean crop) {
            previousState = state;
            subregionStack.push(this);

            int left = region.minX() + state.region.minX();
            int right = region.maxX() + state.region.minX();
            int top = region.minY() + state.region.minY();
            int bottom = region.maxY() + state.region.minY();
            Rect2i subRegion = Rect2i.createFromMinAndMax(left, top, right, bottom);
            if (crop) {
                Rect2i cropRegion = subRegion.intersect(state.cropRegion);
                if (cropRegion.isEmpty()) {
                    // TODO: disable drawing
                } else if (!cropRegion.equals(state.cropRegion)) {
                    state = new CanvasState(subRegion, cropRegion);
                    crop(cropRegion);
                    croppingRegion = true;
                } else {
                    state = new CanvasState(subRegion);
                }
            } else {
                state = new CanvasState(subRegion);
            }
        }

        @Override
        public void close() {
            if (!disposed) {
                Util.checkGLError();
                disposed = true;
                LwjglSubRegion region = subregionStack.pop();
                while (!region.equals(this)) {
                    logger.error("UI Subregions being closed in an incorrect order");
                    region.close();
                    region = subregionStack.pop();
                }
                if (croppingRegion) {
                    glScissor(previousState.region.minX(), previousState.region.minY(), previousState.region.width(), previousState.region.height());
                }
                state = previousState;
            }
        }
    }

    private static class TextCacheKey {
        private String text;
        private Font font;
        private int width;
        private HorizontalAlignment alignment;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlignment alignment) {
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
}
