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
package org.terasology.rendering.opengl;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontCharacter;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.HorizontalAlign;

import javax.vecmath.Vector3f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * @author Immortius
 */
public class OpenGLFont extends AbstractAsset<FontData> implements Font {
    private FontData data;

    public OpenGLFont(AssetUri uri, FontData data) {
        super(uri);
        reload(data);
    }

    // This function shouldn't be here, should be in a Canvas style class most likely.
    public void drawString(int x, int y, String text, Color baseColor) {
        if (isDisposed()) {
            return;
        }
        CoreRegistry.get(ShaderManager.class).enableDefaultTextured();

        Texture bound = null;
        
        Color color = baseColor;

        int posX = x;
        int posY = y;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                posX = x;
                posY += data.getLineHeight();
            } else {
                FontCharacter character = data.getCharacter(c);
                if (character != null && character.getPage() != null) {
                    Texture page = character.getPage();
                    if (!character.getPage().equals(bound)) {
                        glBindTexture(GL11.GL_TEXTURE_2D, page.getId());
                        bound = character.getPage();
                    }

                    glMatrixMode(GL11.GL_MODELVIEW);
                    glPushMatrix();
                    glTranslatef(posX + character.getxOffset(), posY + character.getyOffset(), 0);
                    glBegin(GL_TRIANGLE_FAN);
                    glColor4f(color.r, color.g, color.b, color.a);
                    glTexCoord2f(character.getX(), character.getY());
                    glVertex3f(0, 0, 0);
                    glTexCoord2f(character.getX() + character.getTexWidth(), character.getY());
                    glVertex3f(character.getWidth(), 0, 0);
                    glTexCoord2f(character.getX() + character.getTexWidth(), character.getY() + character.getTexHeight());
                    glVertex3f(character.getWidth(), character.getHeight(), 0);
                    glTexCoord2f(character.getX(), character.getY() + character.getTexHeight());
                    glVertex3f(0, character.getHeight(), 0);
                    glEnd();
                    glPopMatrix();

                    posX += character.getxAdvance();
                } else if (FontColor.isValid(c)) {
                    if (c == FontColor.getReset()) {
                        color = baseColor;
                    } else {
                        color = FontColor.toColor(c);
                    }
                }
            }
        }
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

    public int getHeight(String text) {
        int height = data.getLineHeight();
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                height += data.getLineHeight();
            }
        }
        return height;
    }

    public int getLineHeight() {
        return data.getLineHeight();
    }

    @Override
    public void reload(FontData newData) {
        this.data = newData;
    }

    @Override
    public void dispose() {
        this.data = null;
    }

    @Override
    public boolean isDisposed() {
        return data == null;
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

}
