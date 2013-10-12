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
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Vector2i;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontCharacter;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Color;

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
    public void drawString(int x, int y, String text, org.newdawn.slick.Color color) {
        if (isDisposed()) {
            return;
        }
        CoreRegistry.get(ShaderManager.class).enableDefaultTextured();

        Texture bound = null;

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
                }
            }
        }
    }

    @Override
    public Map<Material, Mesh> createTextMesh(List<String> lines, Color color) {
        return createStringMesh(lines, color, null);
    }

    @Override
    public Map<Material, Mesh> createStringMesh(List<String> lines, Color color, Color shadowColor) {
        Map<Material, MeshBuilder> meshBuilders = Maps.newLinkedHashMap();

        if (shadowColor != null) {
            addLinesToMesh(lines, 1, 1, shadowColor, meshBuilders);
        }
        addLinesToMesh(lines, 0, 0, color, meshBuilders);


        Map<Material, Mesh> result = Maps.newLinkedHashMap();
        for (Map.Entry<Material, MeshBuilder> entry : meshBuilders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getMesh());
        }
        return result;
    }

    private void addLinesToMesh(List<String> lines, int offsetX, int offsetY, Color color, Map<Material, MeshBuilder> meshBuilders) {
        int x = offsetX;
        int y = offsetY;
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                FontCharacter character = data.getCharacter(c);
                if (character != null && character.getPage() != null) {
                    MeshBuilder builder = meshBuilders.get(character.getPageMat());
                    if (builder == null) {
                        builder = new MeshBuilder();
                        meshBuilders.put(character.getPageMat(), builder);
                    }
                    builder.addCharacter(character, x, y, color);

                    x += character.getxAdvance();
                }
            }
            x = offsetX;
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

    private static class MeshBuilder {
        private MeshData meshData = new MeshData();
        private int vertCount;

        public void addCharacter(FontCharacter character, int x, int y, Color color) {
            float top = y + character.getyOffset();
            float bottom = top + character.getHeight();
            float left = x + character.getxOffset();
            float right = left + character.getWidth();
            float texTop = character.getY();
            float texBottom = texTop + character.getTexHeight();
            float texLeft = character.getX();
            float texRight = texLeft + character.getTexWidth();

            meshData.getVertices().add(left);
            meshData.getVertices().add(top);
            meshData.getVertices().add(0);
            meshData.getTexCoord0().add(texLeft);
            meshData.getTexCoord0().add(texTop);

            meshData.getVertices().add(right);
            meshData.getVertices().add(top);
            meshData.getVertices().add(0);
            meshData.getTexCoord0().add(texRight);
            meshData.getTexCoord0().add(texTop);

            meshData.getVertices().add(right);
            meshData.getVertices().add(bottom);
            meshData.getVertices().add(0);
            meshData.getTexCoord0().add(texRight);
            meshData.getTexCoord0().add(texBottom);

            meshData.getVertices().add(left);
            meshData.getVertices().add(bottom);
            meshData.getVertices().add(0);
            meshData.getTexCoord0().add(texLeft);
            meshData.getTexCoord0().add(texBottom);

            for (int i = 0; i < 4; ++i) {
                meshData.getColors().add(color.rf());
                meshData.getColors().add(color.gf());
                meshData.getColors().add(color.bf());
                meshData.getColors().add(color.af());
            }

            meshData.getIndices().add(vertCount);
            meshData.getIndices().add(vertCount + 1);
            meshData.getIndices().add(vertCount + 2);
            meshData.getIndices().add(vertCount);
            meshData.getIndices().add(vertCount + 2);
            meshData.getIndices().add(vertCount + 3);

            vertCount += 4;
        }

        public Mesh getMesh() {
            return Assets.generateAsset(AssetType.MESH, meshData, Mesh.class);
        }
    }
}
