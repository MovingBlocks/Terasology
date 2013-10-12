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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.lwjgl.opengl.Display;
import org.terasology.asset.Assets;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL11.glOrtho;

/**
 * @author Immortius
 */
public class LwjglCanvas implements Canvas {

    private CanvasState state;

    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    public void preRender() {
        state = new CanvasState(Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight()));
    }

    public void postRender() {
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
    public SubRegion subRegion(Rect2i region) {
        return new LwjglSubRegion(region);
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
    public void setTextAlignment(HorizontalAlignment value) {
        state.horizontalTextAlignment = value;
    }

    @Override
    public void setTextAlignment(VerticalAlignment value) {
        state.verticalTextAlignment = value;
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
        TextCacheKey key = new TextCacheKey(text, font, state.textColor, shadowColor);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        if (fontMesh == null) {
            List<String> lines = Arrays.asList(text.split("\\r?\\n"));
            fontMesh = font.createStringMesh(lines, state.textColor, shadowColor);
            cachedText.put(key, fontMesh);
        }

        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            entry.getKey().setFloat2("offset", state.offset.x, state.offset.y);
            entry.getValue().render();
        }
    }

    @Override
    public void drawTextShadowed(Font font, String text, int maxWidth, Color shadowColor) {
        TextCacheKey key = new TextCacheKey(text, font, state.textColor, shadowColor, maxWidth);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        if (fontMesh == null) {
            List<String> lines = LineBuilder.getLines(font, text, maxWidth);
            fontMesh = font.createStringMesh(lines, state.textColor, shadowColor);
            cachedText.put(key, fontMesh);
        }

        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            entry.getKey().setFloat2("offset", state.offset.x, state.offset.y);
            entry.getValue().render();
        }
    }

    private static class CanvasState {
        public Rect2i region;
        public Rect2i cropRegion;
        public Color textColor = Color.WHITE;

        public HorizontalAlignment horizontalTextAlignment = HorizontalAlignment.LEFT;
        public VerticalAlignment verticalTextAlignment = VerticalAlignment.TOP;

        public Vector2i offset = new Vector2i();

        public CanvasState(Rect2i region) {
            this(region, region);
        }

        public CanvasState(Rect2i region, Rect2i cropRegion) {
            this.region = region;
            this.cropRegion = cropRegion;
        }
    }

    private class LwjglSubRegion implements SubRegion {

        private CanvasState previousState;

        public LwjglSubRegion(Rect2i region) {
            int left = region.minX() + state.region.minX();
            int right = left + region.width();
            int top = region.minY() + state.region.minY();
            int bottom = top + region.height();
            previousState = state;
            //state = new CanvasState();
            //glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -32, 32);
        }

        @Override
        public void close() {
            state = previousState;
        }
    }

    private static class TextCacheKey {
        private String text;
        private Font font;
        private Color color;
        private Color shadowColor;
        private int width = -1;

        public TextCacheKey(String text, Font font, Color color, Color shadowColor) {
            this.text = text;
            this.font = font;
            this.color = color;
            this.shadowColor = shadowColor;
        }

        public TextCacheKey(String text, Font font, Color color, Color shadowColor, int maxWidth) {
            this.text = text;
            this.font = font;
            this.color = color;
            this.shadowColor = shadowColor;
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
                        && Objects.equals(color, other.color) && Objects.equals(shadowColor, other.shadowColor)
                        && Objects.equals(width, other.width);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, color, shadowColor, width);
        }
    }
}
