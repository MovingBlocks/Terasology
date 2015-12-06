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
package org.terasology.rendering.assets.font;

import com.google.common.collect.Maps;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.FontUnderline;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 */
public class FontMeshBuilder {

    private static final float SHADOW_DEPTH = -2;
    private static final int SHADOW_HORIZONTAL_OFFSET = 1;
    private static final int SHADOW_VERTICAL_OFFSET = 1;
    private static final int UNKNOWN = -1;

    private final Material underlineMaterial;

    public FontMeshBuilder(Material underlineMaterial) {
        this.underlineMaterial = underlineMaterial;
    }

    public Map<Material, Mesh> createTextMesh(Font font, List<String> lines, int width, HorizontalAlign alignment, Color baseColor, Color shadowColor, boolean underline) {
        return new Builder(font, lines, width, alignment, baseColor, shadowColor, underline).invoke();
    }

    private class Builder {
        private Font font;
        private List<String> lines;
        private int width;
        private HorizontalAlign alignment;
        private Color shadowColor;
        private boolean baseUnderline;

        private Map<Material, MeshBuilder> meshBuilders = Maps.newLinkedHashMap();

        private int x;
        private int y;
        private boolean currentUnderline;
        private int underlineStart = UNKNOWN;
        private int underlineEnd = UNKNOWN;
        private Deque<Color> previousColors = new ArrayDeque<>();
        private Color currentColor;

        public Builder(Font font, List<String> lines, int width, HorizontalAlign alignment, Color baseColor, Color shadowColor, boolean baseUnderline) {
            this.font = font;
            this.lines = lines;
            this.width = width;
            this.alignment = alignment;
            this.shadowColor = shadowColor;
            this.baseUnderline = baseUnderline;
            this.currentUnderline = baseUnderline;
            this.currentColor = baseColor;
        }

        public Map<Material, Mesh> invoke() {

            processLines();

            return generateResult();
        }

        private Map<Material, Mesh> generateResult() {
            Map<Material, Mesh> result = Maps.newLinkedHashMap();
            for (Map.Entry<Material, MeshBuilder> entry : meshBuilders.entrySet()) {
                result.put(entry.getKey(), entry.getValue().build());
            }
            return result;
        }

        private MeshBuilder getBuilderFor(Material material) {
            MeshBuilder builder = meshBuilders.get(material);
            if (builder == null) {
                builder = new MeshBuilder();
                meshBuilders.put(material, builder);
            }
            return builder;
        }

        private void processLines() {
            for (String line : lines) {
                int w = font.getWidth(line);
                x = alignment.getOffset(w, width);

                for (char c : line.toCharArray()) {
                    FontCharacter character = font.getCharacterData(c);
                    if (character != null && character.getPage() != null) {
                        MeshBuilder builder = getBuilderFor(character.getPageMat());

                        if (shadowColor.a() != 0) {
                            addCharacter(builder, character, shadowColor, SHADOW_HORIZONTAL_OFFSET, SHADOW_VERTICAL_OFFSET, SHADOW_DEPTH);
                        }
                        addCharacter(builder, character, currentColor, 0, 0, 0);
                        updateUnderline(c, character);

                        x += character.getxAdvance();
                    } else if (FontColor.isValid(c)) {
                        applyUnderline();
                        processColorCode(c);
                    } else if (FontUnderline.isValid(c)) {
                        processUnderlineCode(c);
                    }
                }
                applyUnderline();
                y += font.getLineHeight();
            }
        }

        private void processUnderlineCode(char c) {
            if (!baseUnderline) {
                if (c == FontUnderline.getStart() && !currentUnderline) {
                    currentUnderline = true;
                } else if (currentUnderline) {
                    applyUnderline();
                    currentUnderline = false;
                }
            }
        }

        private void processColorCode(char c) {
            if (c == FontColor.getReset()) {
                if (!previousColors.isEmpty()) {
                    currentColor = previousColors.removeLast();

                }
            } else {
                previousColors.addLast(currentColor);
                currentColor = FontColor.toColor(c);
            }
        }

        private void applyUnderline() {
            if (currentUnderline && underlineStart != UNKNOWN) {
                MeshBuilder builder = getBuilderFor(underlineMaterial);
                if (shadowColor.a() != 0) {
                    addUnderline(builder, underlineStart + SHADOW_HORIZONTAL_OFFSET, underlineEnd + SHADOW_HORIZONTAL_OFFSET,
                            y + font.getBaseHeight() + SHADOW_VERTICAL_OFFSET + font.getUnderlineOffset(), font.getUnderlineThickness(), shadowColor, SHADOW_DEPTH);
                }
                addUnderline(builder, underlineStart, underlineEnd, y + font.getBaseHeight() + font.getUnderlineOffset(), font.getUnderlineThickness(), currentColor, 0);
            }
            underlineStart = UNKNOWN;
            underlineEnd = UNKNOWN;
        }

        private void updateUnderline(char c, FontCharacter character) {
            if (currentUnderline && !Character.isWhitespace(c)) {
                if (underlineStart == UNKNOWN) {
                    underlineStart = x + character.getxOffset();
                }
                underlineEnd = x + character.getxOffset() + character.getWidth();
            }
        }

        private void addUnderline(MeshBuilder builder, int xStart, int xEnd, int underlineTop, int underlineThickness, Color color, float depth) {
            float bottom = (float) underlineTop + underlineThickness;

            Vector3f v1 = new Vector3f(xStart, underlineTop, depth);
            Vector3f v2 = new Vector3f(xEnd, underlineTop, depth);
            Vector3f v3 = new Vector3f(xEnd, bottom, depth);
            Vector3f v4 = new Vector3f(xStart, bottom, depth);
            builder.addPoly(v1, v2, v3, v4);
            builder.addColor(color, color, color, color);
            builder.addTexCoord(0, 0);
            builder.addTexCoord(1, 0);
            builder.addTexCoord(1, 1);
            builder.addTexCoord(0, 1);
        }

        private void addCharacter(MeshBuilder builder, FontCharacter character, Color color, float xOffset, float yOffset, float depth) {
            float top = y + character.getyOffset() + yOffset;
            float bottom = top + character.getHeight() + yOffset;
            float left = x + character.getxOffset() + xOffset;
            float right = left + character.getWidth() + xOffset;
            float texTop = character.getY();
            float texBottom = texTop + character.getTexHeight();
            float texLeft = character.getX();
            float texRight = texLeft + character.getTexWidth();

            Vector3f v1 = new Vector3f(left, top, depth);
            Vector3f v2 = new Vector3f(right, top, depth);
            Vector3f v3 = new Vector3f(right, bottom, depth);
            Vector3f v4 = new Vector3f(left, bottom, depth);
            builder.addPoly(v1, v2, v3, v4);
            builder.addColor(color, color, color, color);
            builder.addTexCoord(texLeft, texTop);
            builder.addTexCoord(texRight, texTop);
            builder.addTexCoord(texRight, texBottom);
            builder.addTexCoord(texLeft, texBottom);
        }
    }
}
