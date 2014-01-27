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
package org.terasology.rendering.base;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.newdawn.slick.Color;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontCharacter;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.nui.HorizontalAlign;

import com.google.common.collect.Maps;

public abstract class BaseFont extends AbstractAsset<FontData> implements Font {

    protected FontData data;

    public BaseFont(AssetUri uri, FontData data) {
        super(uri);
        reload(data);
    }

    @Override
    public abstract void drawString(int x, int y, String text, Color color);

    @Override
    public void reload(FontData fontData) {
        this.data = fontData;
    }

    @Override
    public Map<Material, Mesh> createTextMesh(List<String> lines, int width, HorizontalAlign alignment) {
        Map<Material, MeshBuilder> meshBuilders = Maps.newLinkedHashMap();
        addLinesToMesh(lines, meshBuilders, width, alignment);

        Map<Material, Mesh> result = Maps.newLinkedHashMap();
        for (Map.Entry<Material, MeshBuilder> entry : meshBuilders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().build());
        }
        return result;
    }

    private void addLinesToMesh(List<String> lines, Map<Material, MeshBuilder> meshBuilders, int maxWidth, HorizontalAlign alignment) {
        int y = 0;
        for (String line : lines) {
            int w = getWidth(line);
            int x = alignment.getOffset(w, maxWidth);
            for (char c : line.toCharArray()) {
                FontCharacter character = data.getCharacter(c);
                if (character != null && character.getPage() != null) {
                    MeshBuilder builder = meshBuilders.get(character.getPageMat());
                    if (builder == null) {
                        builder = new MeshBuilder();
                        meshBuilders.put(character.getPageMat(), builder);
                    }
                    addCharacter(builder, character, x, y);

                    x += character.getxAdvance();
                }
            }
            y += data.getLineHeight();
        }
    }

    public void addCharacter(MeshBuilder builder, FontCharacter character, int x, int y) {
        float top = y + character.getyOffset();
        float bottom = top + character.getHeight();
        float left = x + character.getxOffset();
        float right = left + character.getWidth();
        float texTop = character.getY();
        float texBottom = texTop + character.getTexHeight();
        float texLeft = character.getX();
        float texRight = texLeft + character.getTexWidth();

        Vector3f v1 = new Vector3f(left, top, 0);
        Vector3f v2 = new Vector3f(right, top, 0);
        Vector3f v3 = new Vector3f(right, bottom, 0);
        Vector3f v4 = new Vector3f(left, bottom, 0);
        builder.addPoly(v1, v2, v3, v4);
        builder.addTexCoord(texLeft, texTop);
        builder.addTexCoord(texRight, texTop);
        builder.addTexCoord(texRight, texBottom);
        builder.addTexCoord(texLeft, texBottom);
    }

    @Override
    public int getWidth(String text) {
        int largestWidth = 0;
        int currentWidth = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                largestWidth = Math.max(largestWidth, currentWidth);
                currentWidth = 0;
            } else {
                FontCharacter character = data.getCharacter(c);
                if (character != null) {
                    currentWidth += character.getxAdvance();
                }
            }
        }
        return Math.max(largestWidth, currentWidth);
    }

    @Override
    public int getWidth(Character c) {
        FontCharacter character = data.getCharacter(c);
        if (character != null) {
            return character.getxAdvance();
        }
        return 0;
    }

    @Override
    public int getHeight(String text) {
        int height = data.getLineHeight();
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                height += data.getLineHeight();
            }
        }
        return height;
    }

    @Override
    public int getLineHeight() {
        return data.getLineHeight();
    }

    @Override
    public Vector2i getSize(List<String> lines) {
        int height = getLineHeight() * lines.size();
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, getWidth(line));
        }
        return new Vector2i(width, height);
    }

    @Override
    public boolean hasCharacter(Character c) {
        return c == '\n' || data.getCharacter(c) != null;
    }

    @Override
    public void dispose() {
        this.data = null;
    }

    @Override
    public boolean isDisposed() {
        return data == null;
    }

    @Override
    public String toString() {
        return getURI().toString();
    }
}
