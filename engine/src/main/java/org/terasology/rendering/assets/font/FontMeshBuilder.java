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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.terasology.rendering.FontColor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;

import com.google.common.collect.Maps;

/**
 * @author Immortius
 */
public class FontMeshBuilder {

    private static final float SHADOW_DEPTH = -1;

    private Font font;
    private Color defaultColor;

    public FontMeshBuilder(Font font) {
        this.font = font;
    }

    public Map<Material, Mesh> createTextMesh(List<String> lines, int width, HorizontalAlign alignment, Color baseColor, Color shadowColor) {
        this.defaultColor = baseColor;
        Map<Material, MeshBuilder> meshBuilders = Maps.newLinkedHashMap();
        addLinesToMesh(lines, meshBuilders, width, alignment, shadowColor);

        Map<Material, Mesh> result = Maps.newLinkedHashMap();
        for (Map.Entry<Material, MeshBuilder> entry : meshBuilders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().build());
        }
        return result;
    }

    private void addLinesToMesh(List<String> lines, Map<Material, MeshBuilder> meshBuilders, int maxWidth, HorizontalAlign alignment, Color shadowColor) {
        int y = 0;
        Deque<Color> prevColors = new ArrayDeque<>();
        Color currentColor = defaultColor;
        
        for (String line : lines) {
            int w = font.getWidth(line);
            int x = alignment.getOffset(w, maxWidth);
            for (char c : line.toCharArray()) {
                FontCharacter character = font.getCharacterData(c);
                if (character != null && character.getPage() != null) {
                    MeshBuilder builder = meshBuilders.get(character.getPageMat());
                    if (builder == null) {
                        builder = new MeshBuilder();
                        meshBuilders.put(character.getPageMat(), builder);
                    }

                    if (shadowColor.a() != 0) {
                        addCharacter(builder, character, x, y, shadowColor, 1, 1, SHADOW_DEPTH);
                    }
                    addCharacter(builder, character, x, y, currentColor, 0, 0, 0);

                    x += character.getxAdvance();
                } else if (FontColor.isValid(c)) {
                    if (c == FontColor.getReset()) {
                        if (!prevColors.isEmpty()) {
                            currentColor = prevColors.removeLast();
                        }
                    } else {
                        prevColors.addLast(currentColor);
                        currentColor = FontColor.toColor(c);
                    }
                }
            }
            y += font.getLineHeight();
        }
    }

    private void addCharacter(MeshBuilder builder, FontCharacter character, int x, int y, Color color, float xOffset, float yOffset, float depth) {
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
