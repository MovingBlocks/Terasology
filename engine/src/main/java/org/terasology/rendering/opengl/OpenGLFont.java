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

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetUri;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.font.FontCharacter;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.font.BaseFont;

/**
 * @author Immortius
 */
public class OpenGLFont extends BaseFont {
    public OpenGLFont(AssetUri uri, FontData data) {
        super(uri, data);
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
                        color = new Color(FontColor.toColor(c).getRepresentation());
                    }
                }
            }
        }
    }
}
