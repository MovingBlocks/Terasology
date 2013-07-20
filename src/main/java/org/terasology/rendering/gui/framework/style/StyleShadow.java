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
package org.terasology.rendering.gui.framework.style;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class StyleShadow extends UIDisplayContainer implements Style {
    
    /*
       TODO add to gui_menu.png?
       shadow.png is divided into 3 section, each 10px height:
        ___________
       |  outer    |  corner which will be used if the shadow is outside
       |  corner   |  
       |-----------| 
       |  strip    |  will be stretched to fit the size of the display element
       |           |  
       |-----------|
       |  inner    |  corner which will be used if the shadow is inside
       |  corner   |  
       |___________| 
    */

    //direction
    public static enum EShadowDirection {
        INSIDE, OUTSIDE
    }

    ;
    private EShadowDirection direction = EShadowDirection.OUTSIDE;

    //texture
    private final String textureUrl = "engine:shadow";
    private final Vector2f sectorSize = new Vector2f(10f, 10f);
    private final Vector2f outerCornerPosition = new Vector2f(0f, 0f);
    private final Vector2f stripPosition = new Vector2f(0f, 10f);
    private final Vector2f innerCornerPosition = new Vector2f(0f, 20f);
    private float opacity;
    private Texture shadow;
    private Mesh mesh;

    //settings
    private float lightSourceOffset = 3f;
    private Vector4f width = new Vector4f(0, 0, 0, 0);  //top right bottom left
    private float[] offset = new float[]{0, 0,        //top       (left end - right end)
            0, 0,        //right     (top end - bottom end)
            0, 0,        //bottom    (left end - right end)
            0, 0};       //left      (top end - bottom end)

    public StyleShadow(Vector4f width, EShadowDirection direction, float opacity) {
        shadow = Assets.getTexture(textureUrl);
        createMesh(opacity);

        this.opacity = opacity;
        this.direction = direction;

        setWidth(width);
        setCrop(false);
    }

    @Override
    public void render() {
        if (mesh == null)
            return;

        if (mesh.isDisposed()) {
            return;
        }

        if (shadow != null) {
            ShaderManager.getInstance().enableDefaultTextured();
            glBindTexture(GL11.GL_TEXTURE_2D, shadow.getId());

            if (direction == EShadowDirection.INSIDE) {
                renderInside();
            } else if (direction == EShadowDirection.OUTSIDE) {
                renderOutside();
            }
        }
    }

    @Override
    public void update() {

    }

    private void renderOutside() {
        //=======================================
        // render strips
        //=======================================
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(stripPosition.x / shadow.getWidth(), stripPosition.y / shadow.getHeight(), 0.0f);
        glScalef(sectorSize.x / shadow.getWidth(), sectorSize.y / shadow.getHeight(), 1.0f);
        glMatrixMode(GL11.GL_MODELVIEW);

        //top
        if (width.x > 0) {
            glPushMatrix();
            glRotatef(90f, 0f, 0f, 1f);
            glTranslatef(-width.x, -getSize().x + offset[1], 0f);
            glScalef(width.x, getSize().x - offset[0] - offset[1], 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //right
        if (width.y > 0) {
            glPushMatrix();
            glRotatef(180f, 0f, 0f, 1f);
            glTranslatef(-getSize().x - width.y, -getSize().y + offset[3], 0f);
            glScalef(width.y, getSize().y - offset[2] - offset[3], 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom
        if (width.z > 0) {
            glPushMatrix();
            glRotatef(-90f, 0f, 0f, 1f);
            glTranslatef(-getSize().y - width.z, offset[4], 0f);
            glScalef(width.z, getSize().x - offset[4] - offset[5], 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //left
        if (width.w > 0) {
            glPushMatrix();
            glRotatef(0f, 0f, 0f, 1f);
            glTranslatef(-width.w, offset[6], 0f);
            glScalef(width.w, getSize().y - offset[6] - offset[7], 1.0f);
            mesh.render();
            glPopMatrix();
        }

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();

        glMatrixMode(GL11.GL_MODELVIEW);

        //=======================================
        // render corners
        //=======================================
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(outerCornerPosition.x, outerCornerPosition.y, 0.0f);
        glScalef(sectorSize.x / shadow.getWidth(), sectorSize.y / shadow.getHeight(), 1.0f);
        glMatrixMode(GL11.GL_MODELVIEW);

        //top left
        if (width.x > 0 && width.w > 0) {
            glPushMatrix();
            glRotatef(0f, 0f, 0f, 1f);
            glTranslatef(-width.w, -width.x, 0f);
            glScalef(width.w, width.x, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //top right
        if (width.x > 0 && width.y > 0) {
            glPushMatrix();
            glRotatef(90f, 0f, 0f, 1f);
            glTranslatef(-width.x, -getSize().x - width.y, 0f);
            glScalef(width.x, width.y, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom left
        if (width.z > 0 && width.w > 0) {
            glPushMatrix();
            glRotatef(-90f, 0f, 0f, 1f);
            glTranslatef(-getSize().y - width.z, -width.w, 0f);
            glScalef(width.z, width.w, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom right
        if (width.y > 0 && width.z > 0) {
            glPushMatrix();
            glRotatef(-180f, 0f, 0f, 1f);
            glTranslatef(-getSize().x - width.y, -getSize().y - width.z, 0f);
            glScalef(width.y, width.z, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();

        glMatrixMode(GL11.GL_MODELVIEW);
    }


    private void renderInside() {
        //=======================================
        // render strips
        //=======================================
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(stripPosition.x / shadow.getWidth(), stripPosition.y / shadow.getHeight(), 0.0f);
        glScalef(sectorSize.x / shadow.getWidth(), sectorSize.y / shadow.getHeight(), 1.0f);
        glMatrixMode(GL11.GL_MODELVIEW);

        //top
        if (width.x > 0) {
            glPushMatrix();
            glRotatef(-90f, 0f, 0f, 1f);
            glTranslatef(-width.x, width.w, 0f);
            glScalef(width.x, getSize().x - width.y - width.w, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //right
        if (width.y > 0) {
            glPushMatrix();
            glRotatef(0f, 0f, 0f, 1f);
            glTranslatef(getSize().x - width.y, width.x, 0f);
            glScalef(width.y, getSize().y - width.x - width.z, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom
        if (width.z > 0) {
            glPushMatrix();
            glRotatef(90f, 0f, 0f, 1f);
            glTranslatef(getSize().y - width.z, -getSize().x + width.y, 0f);
            glScalef(width.z, getSize().x - width.y - width.w, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //left
        if (width.w > 0) {
            glPushMatrix();
            glRotatef(180f, 0f, 0f, 1f);
            glTranslatef(-width.w, -getSize().y + width.z, 0f);
            glScalef(width.w, getSize().y - width.x - width.z, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();

        glMatrixMode(GL11.GL_MODELVIEW);

        //=======================================
        // render corners
        //=======================================
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(innerCornerPosition.x / shadow.getWidth(), innerCornerPosition.y / shadow.getHeight(), 0.0f);
        glScalef(sectorSize.x / shadow.getWidth(), sectorSize.y / shadow.getHeight(), 1.0f);
        glMatrixMode(GL11.GL_MODELVIEW);

        //top left
        if (width.x > 0 && width.w > 0) {
            glPushMatrix();
            glRotatef(180f, 0f, 0f, 1f);
            glTranslatef(-width.w, -width.x, 0f);
            glScalef(width.w, width.x, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //top right
        if (width.x > 0 && width.y > 0) {
            glPushMatrix();
            glRotatef(-90f, 0f, 0f, 1f);
            glTranslatef(-width.x, getSize().x - width.y, 0f);
            glScalef(width.x, width.y, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom left
        if (width.z > 0 && width.w > 0) {
            glPushMatrix();
            glRotatef(90f, 0f, 0f, 1f);
            glTranslatef(getSize().y - width.z, -width.w, 0f);
            glScalef(width.z, width.w, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        //bottom right
        if (width.y > 0 && width.z > 0) {
            glPushMatrix();
            glRotatef(0f, 0f, 0f, 1f);
            glTranslatef(getSize().x - width.y, getSize().y - width.z, 0f);
            glScalef(width.y, width.z, 1.0f);
            mesh.render();
            glPopMatrix();
        }

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();

        glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void createMesh(float opacity) {
        if (mesh != null) {
            mesh.dispose();
        }

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addGUIQuadMesh(tessellator, new Vector4f(1, 1, 1, opacity), 1.0f, 1.0f);
        mesh = tessellator.generateMesh();
    }

    private void calcOffset() {
        if (direction == EShadowDirection.OUTSIDE) {
            //if the shadow is outside, the 'offset' is responsible for the impression of the light source to be top-left, top-right, 
            //bottom-right or bottom-left, if for example just the right (y) and bottom (z) shadow is assigned, the light source 
            //will be top-left. (It will therefore end the shadow before the edge by the value of 'lightSourceOffset' to achieve this impression)

            for (int i = 0; i < offset.length; i++) {
                offset[i] = 0;
            }

            if (width.x > 0 && width.z == 0) {
                if (width.y > 0 && width.w == 0) {
                    offset[0] = lightSourceOffset; //offset top (left end)
                }

                if (width.w > 0 && width.y == 0) {
                    offset[1] = lightSourceOffset; //offset top (right end)
                }
            }

            if (width.y > 0 && width.w == 0) {
                if (width.z > 0 && width.x == 0) {
                    offset[2] = lightSourceOffset; //offset right (top end)
                }

                if (width.x > 0 && width.z == 0) {
                    offset[3] = lightSourceOffset; //offset right (bottom end)
                }
            }

            if (width.z > 0 && width.x == 0) {
                if (width.y > 0 && width.w == 0) {
                    offset[4] = lightSourceOffset; //offset bottom (left end)
                }

                if (width.w > 0 && width.y == 0) {
                    offset[5] = lightSourceOffset; //offset bottom (right end)
                }
            }

            if (width.w > 0 && width.y == 0) {
                if (width.z > 0 && width.x == 0) {
                    offset[6] = lightSourceOffset; //offset left (top end)
                }

                if (width.x > 0 && width.z == 0) {
                    offset[7] = lightSourceOffset; //offset left (bottom end)
                }
            }
        }
    }

    public Vector4f getWidth() {
        return width;
    }

    public void setWidth(Vector4f width) {
        this.width = width;
        calcOffset();
    }

    public EShadowDirection getDirection() {
        return direction;
    }

    public void setDirection(EShadowDirection direction) {
        this.direction = direction;
        calcOffset();
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        createMesh(opacity);
    }

    @Override
    public int getLayer() {
        return 4;
    }
}
