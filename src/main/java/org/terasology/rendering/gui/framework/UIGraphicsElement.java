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
package org.terasology.rendering.gui.framework;


import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import java.util.logging.Logger;

/**
 * Provides support for rendering graphical elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIGraphicsElement extends UIDisplayElement {
    private Logger logger = Logger.getLogger(getClass().getName());

    private Texture texture;

    private Vector2f _textureOrigin = new Vector2f(0.0f, 0.0f);
    private Vector2f _textureSize = new Vector2f(1.0f, 1.0f);

    private float _rotate = 0f;
    private Mesh _mesh;
    
    public UIGraphicsElement() {
    	
    }
    
    public UIGraphicsElement(int r, int g, int b, float a) {
    	setColor(r, g, b, a);
    }

    public UIGraphicsElement(Texture texture) {
        setTexture(texture);
    }
    
    private float RGBtoColor(int v) {
    	return (float)v / 255.0f;
    }

    @Override
    public void render() {
        if (_mesh == null)
            return;

        if (_mesh.isDisposed()) {
            logger.severe("Disposed mesh encountered!");
            return;
        }

        if (texture != null) {
	        ShaderManager.getInstance().enableDefaultTextured();
	        glBindTexture(GL11.GL_TEXTURE_2D, texture != null ? texture.getId() : 0);
	
	        glMatrixMode(GL_TEXTURE);
	        glPushMatrix();
	        glTranslatef(_textureOrigin.x, _textureOrigin.y, 0.0f);
	        glScalef(_textureSize.x, _textureSize.y, 1.0f);
	        glMatrixMode(GL11.GL_MODELVIEW);
	
	        glPushMatrix();
	        if (_rotate > 0f) {
	            glRotatef(_rotate, 0f, 0f, 1f);
	        }
	        glScalef(getSize().x, getSize().y, 1.0f);
	        _mesh.render();
	        glPopMatrix();
	
	        glMatrixMode(GL_TEXTURE);
	        glPopMatrix();
	        glMatrixMode(GL11.GL_MODELVIEW);
        } else {
            glPushMatrix();
	        if (_rotate > 0f) {
	            glRotatef(_rotate, 0f, 0f, 1f);
	        }
            glScalef(getSize().x, getSize().y, 0.0f);
            _mesh.render();
            glPopMatrix();
        }
    }

    /**
     * Get the texture origin.
     * @return Returns the texture origin.
     * 
     * @deprecated Actually this method is not deprecated. But use setTextureOrigin to set the origin instead!
     */
    public Vector2f getTextureOrigin() {
        return _textureOrigin;
    }
    
    /**
     * Set the texture origin. You don't need to divide by the texture width/height, this will be done within this method.
     * @param origin The origin of the texture.
     */
    public void setTextureOrigin(Vector2f origin) {
    	if (origin != null && texture != null) {
    		_textureOrigin = new Vector2f(origin.x / (float)texture.getWidth(), origin.y / (float)texture.getHeight());
    	}
    }

    /**
     * Get the texture size.
     * @return Returns the texture size.
     * 
     * @deprecated Actually this method is not deprecated. But use setTextureSize to set the size instead! (deprecated tag will be removed in the future)
     */
    public Vector2f getTextureSize() {
        return _textureSize;
    }
    
    /**
     * Set the texture size. You don't need to divide by the texture width/height, this will be done within this method.
     * @param size The size of the texture.
     */
    public void setTextureSize(Vector2f size) {
    	if (size != null && texture != null) {
    		_textureSize = new Vector2f(size.x / (float)texture.getWidth(), size.y / (float)texture.getHeight());
    	}
    }
    
    /**
     * Set the texture.
     * @param texture The texture.
     */
    public void setTexture(Texture texture) {    	
		this.texture = texture;
		
    	if (texture != null) {
    		setColor(255, 255, 255, 1f);
    	}
    }
    
    public void setColor(int r, int g, int b, float a) {
    	generateMesh(r, g, b, a);
    }
    
    private void generateMesh(int r, int g, int b, float a) {
    	if (_mesh != null) {
    		_mesh.dispose();
    	}
    	
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addGUIQuadMesh(tessellator, new Vector4f(RGBtoColor(r), RGBtoColor(g), RGBtoColor(b), a), 1.0f, 1.0f);
        _mesh = tessellator.generateMesh();
    }

    @Override
    public void update() {
    }
    
	@Override
	public void layout() {

	}

    /*
     * Rotate graphics element
     */
    public void setRotateAngle(float angle) {
        _rotate = angle;
    }

    public float getRotateAngle() {
        return _rotate;
    }
}
