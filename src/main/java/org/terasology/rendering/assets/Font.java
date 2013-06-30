/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.assets;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetData;
import org.terasology.asset.AssetUri;
import org.terasology.asset.CompatibilityHackAsset;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.texture.Texture;

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
public class Font extends CompatibilityHackAsset implements Asset<AssetData> {

    private TIntObjectMap<FontCharacter> charMap = new TIntObjectHashMap<FontCharacter>();
    private int lineHeight;

    public Font(AssetUri uri) {
        super(uri);
    }

    public void setCharacter(int id, FontCharacter character) {
        charMap.put(id, character);
    }

    @Override
    public void dispose() {

    }

    public void drawString(int x, int y, String text, Color color) {
        ShaderManager.getInstance().enableDefaultTextured();

        Texture bound = null;

        int posX = x;
        int posY = y;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                posX = x;
                posY += lineHeight;
            } else {
                FontCharacter character = charMap.get(c);
                if (character != null) {
                    if (!character.getPage().equals(bound)) {
                        glBindTexture(GL11.GL_TEXTURE_2D, character.getPage().getId());
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

    public int getWidth(String text) {
        int largestWidth = 0;
        int currentWidth = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                largestWidth = Math.max(largestWidth, currentWidth);
                currentWidth = 0;
            } else {
                FontCharacter character = charMap.get(c);
                if (character != null) {
                    currentWidth += character.getxAdvance();
                }
            }
        }
        return Math.max(largestWidth, currentWidth);
    }

    public int getHeight(String text) {
        int height = lineHeight;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                height += lineHeight;
            }
        }
        return height;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }
}
