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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;

import javax.vecmath.Vector2f;
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

    public void preRender() {
        state = new CanvasState(Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight()));
        glScissor(0, 0, Display.getWidth(), Display.getHeight());
        glEnable(GL_SCISSOR_TEST);
    }

    public void postRender() {
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
    public void drawTextShadowed(Font font, String text, Color shadowColor) {
        drawTextShadowed(font, text, Integer.MAX_VALUE, shadowColor);
    }

    @Override
    public void drawTextShadowed(Font font, String text, int maxWidth, Color shadowColor) {
        TextCacheKey key = new TextCacheKey(text, font, maxWidth);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        if (fontMesh == null) {
            List<String> lines = LineBuilder.getLines(font, text, maxWidth);
            fontMesh = font.createTextMesh(lines);
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
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, Vector2f subTopLeft, Vector2f subBottomRight) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, ScaleMode mode, Border border) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, ScaleMode mode, Border border, Vector2f subTopLeft, Vector2f subBottomRight) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class CanvasState {
        public Rect2i region;
        public Rect2i cropRegion;
        public boolean cropped;
        public Color textColor = Color.WHITE;

        public Vector2i offset = new Vector2i();

        public CanvasState(Rect2i region) {
            this(region, region);
        }

        public CanvasState(Rect2i region, Rect2i cropRegion) {
            this.region = region;
            this.cropRegion = cropRegion;
            this.cropped = true;
        }

        public int getAbsoluteOffsetX() {
            return offset.x + region.minX();
        }

        public int getAbsoluteOffsetY() {
            return offset.y + region.minY();
        }
    }

    private class LwjglSubRegion implements SubRegion {

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
                int cropLeft = Math.max(left, state.cropRegion.minX());
                int cropRight = Math.min(right, state.cropRegion.maxX());
                int cropTop = Math.max(top, state.cropRegion.minY());
                int cropBottom = Math.min(bottom, state.cropRegion.maxY());
                state = new CanvasState(subRegion, Rect2i.createFromMinAndMax(cropLeft, cropTop, cropRight, cropBottom));
                glScissor(cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop);
            } else {
                state = new CanvasState(subRegion);
            }
        }

        @Override
        public void close() {
            if (!disposed) {
                disposed = true;
                LwjglSubRegion region = subregionStack.pop();
                while (!region.equals(this)) {
                    logger.error("UI Subregions being closed in an incorrect order");
                    region.close();
                    region = subregionStack.pop();
                }
                if (state.cropped) {
                    glScissor(previousState.region.minX(), previousState.region.minY(), previousState.region.width(), previousState.region.height());
                }
                state = previousState;
            }
        }
    }

    private static class TextCacheKey {
        private String text;
        private Font font;
        private int width = -1;

        public TextCacheKey(String text, Font font) {
            this.text = text;
            this.font = font;
        }

        public TextCacheKey(String text, Font font, int maxWidth) {
            this.text = text;
            this.font = font;
            this.width = maxWidth;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TextCacheKey) {
                TextCacheKey other = (TextCacheKey) obj;
                return Objects.equals(text, other.text) && Objects.equals(font, other.font)
                        && Objects.equals(width, other.width);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, width);
        }
    }
}
