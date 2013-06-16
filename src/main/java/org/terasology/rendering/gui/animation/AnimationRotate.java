/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.gui.animation;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class AnimationRotate extends Animation {
    private float angle;
    private float speed;
    private int   factor;
    private float currentAngle;
    private Mesh mesh;

    private DefaultRenderingProcess.FBO fbo = null;
    private String id = null;


    public AnimationRotate(float angle, float speed){
        this.angle = angle;
        this.speed = speed;
        this.id = generateId();

        if(angle<0){
            factor = -1;
        }else{
            factor = 1;
        }

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addGUIQuadMesh(tessellator, new Vector4f(255f/256f, 255f/256f, 255f/256f, 1f), 1.0f, 1.0f);
        mesh = tessellator.generateMesh();
    }

    @Override
    public void renderBegin(){
        if(fbo == null){
            fbo = DefaultRenderingProcess.getInstance().createFBO(id, Display.getWidth(), Display.getHeight(), DefaultRenderingProcess.FBOType.DEFAULT, false, false);
        }else if(fbo.height != Display.getHeight() || fbo.width != Display.getWidth()){
            fbo = DefaultRenderingProcess.getInstance().createFBO(id, Display.getWidth(), Display.getHeight(), DefaultRenderingProcess.FBOType.DEFAULT, false, false);
        }

        DefaultRenderingProcess.getInstance().getFBO(id).bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void renderEnd(){
        DefaultRenderingProcess.getInstance().getFBO(id).unbind();
        DefaultRenderingProcess.getInstance().getFBO(id).bindTexture();
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
            glLoadIdentity();
            glTranslatef(0, 0.5f, 0);
            glScalef(1f, -1f, 1f);
            glTranslatef(target.getPosition().x / Display.getWidth(), target.getPosition().y / Display.getHeight() - 0.5f, 0f);
            glScalef(target.getSize().x / Display.getWidth(), target.getSize().y / Display.getHeight(), 1f);
            glMatrixMode(GL11.GL_MODELVIEW);
            glPushMatrix();
                glLoadIdentity();
                glTranslatef(target.getPosition().x+target.getSize().x/2, target.getPosition().y+target.getSize().y/2, 0f);
                glRotatef(currentAngle, 0f, 0f, 1f);
                glTranslatef(-target.getSize().x/2, -target.getSize().y/2, 0f);
                glScalef(target.getSize().x, target.getSize().y, 1.0f);
                mesh.render();
            glPopMatrix();
            glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void update(){
        if( (factor>0 && angle>currentAngle) || (factor<0 && angle<currentAngle)){
            currentAngle += factor * speed/*tick*/;
        }else{
            if(!isRepeat()){
                currentAngle = angle;
                this.stop();
            }else{
                currentAngle = 0;
            }

        }
    }

    private String generateId(){
        return "rotate" + this.hashCode();
    }

}
