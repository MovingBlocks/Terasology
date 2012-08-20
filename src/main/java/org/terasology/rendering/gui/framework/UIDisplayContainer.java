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

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.style.UIStyle;
import org.terasology.rendering.gui.framework.style.UIStyleBackgroundColor;
import org.terasology.rendering.gui.framework.style.UIStyleBackgroundImage;
import org.terasology.rendering.gui.framework.style.UIStyleBorderImage;
import org.terasology.rendering.gui.framework.style.UIStyleBorderSolid;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Composition of multiple display elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public abstract class UIDisplayContainer extends UIDisplayElement {

    final ArrayList<UIDisplayElement> _displayElements = new ArrayList<UIDisplayElement>();
    private boolean _crop = false;
    
    private final List<UIStyle> styles = new ArrayList<UIStyle>();

    protected Vector4f _cropMargin = new Vector4f(
    		/*TOP*/    0.0f,
            /*RIGHT*/  0.0f,
            /*BOTTOM*/ 0.0f,
            /*LEFT*/   0.0f
    );

    public UIDisplayContainer() {
        super();
    }

    public UIDisplayContainer(Vector2f position) {
        super(position);
    }

    public UIDisplayContainer(Vector2f position, Vector2f size) {
        super(position, size);
    }

    public void render() {
        boolean testCrop = false;
        int cropX = 0;
        int cropY = 0;
        int cropWidth = 0;
        int cropHeight = 0;

        if (!isVisible())
            return;

        //Cut the elements
        if (_crop) {
            cropX = (int) calcAbsolutePosition().x - (int) (_cropMargin.w);
            cropY = Display.getHeight() - (int) calcAbsolutePosition().y - (int) getSize().y - (int) _cropMargin.z;
            cropWidth = (int) getSize().x + (int) _cropMargin.y;
            cropHeight = (int) getSize().y + (int) _cropMargin.x;
            glEnable(GL_SCISSOR_TEST);
            glScissor(cropX, cropY, cropWidth, cropHeight);
        }

        // Render all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            testCrop = _crop && !_displayElements.get(i).isCroped();
            if (testCrop) {
                glDisable(GL_SCISSOR_TEST);
            }
            _displayElements.get(i).renderTransformed();
            if (testCrop) {
                glEnable(GL_SCISSOR_TEST);
                glScissor(cropX, cropY, cropWidth, cropHeight);
            }
        }

        if (_crop) {
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public void update() {
        if (!isVisible())
            return;

        // Update all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).update();
        }
    }
    
    public void layout() {
        if (!isVisible())
            return;

        // Update layout of all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).layout();
        }
    }
    
    @Override
    public void processBindButton(BindButtonEvent event) {
        if (!isVisible())
            return;
    	
        super.processBindButton(event);
        
        // Pass the bind key to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
        	_displayElements.get(i).processBindButton(event);
        }
    }

    @Override
    public void processKeyboardInput(KeyEvent event) {
        if (!isVisible())
            return;

        super.processKeyboardInput(event);

        // Pass the pressed key to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).processKeyboardInput(event);
        }
    }

    @Override
    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (!isVisible())
            return;

        super.processMouseInput(button, state, wheelMoved);

        // Pass the mouse event to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).processMouseInput(button, state, wheelMoved);
        }
    }

    public void addDisplayElement(UIDisplayElement element) {
        _displayElements.add(element);
        element.setParent(this);
    }

    public void addtDisplayElementToPosition(int position, UIDisplayElement element) {
        _displayElements.add(position, element);
        element.setParent(this);
    }

    public void removeDisplayElement(UIDisplayElement element) {
        _displayElements.remove(element);
        element.setParent(null);
    }

    public void removeAllDisplayElements() {
        for (UIDisplayElement element : _displayElements) {
            element.setParent(null);
        }
        _displayElements.clear();
    }

    public ArrayList<UIDisplayElement> getDisplayElements() {
        return _displayElements;
    }

    /*
     * Set the option for cut elements
     */
    public void setCrop(boolean crop) {
        _crop = crop;
    }

    /*
     * set crop margin for container where
     * x - top
     * y - right
     * z - bottom
     * w - left
     */
    public void setCropMargin(Vector4f margin) {
        _cropMargin = margin;
    }
    
    private <T> T getStyle2(Class<T> style) {
    	for (UIStyle s : styles) {
			if (s.getClass() == style) {
				return style.cast(s);
			}
		}
    	
    	return null;
    }
    
    /**
     * Add a style.
     * @param style The style to add.
     * @param listStart True to add the style at the beginning of the display element list.
     * 
     * TODO need to put particular styles at the particular position.. (background at the beginning, border on top)
     */
    private void addStyle(UIStyle style, boolean listStart) {
    	styles.add(style);
    	
    	UIDisplayElement element = (UIDisplayElement) style;
    	if (listStart) {
    		_displayElements.add(0, element);
    	} else {
    		_displayElements.add(element);
    	}
    	
        element.setParent(this);
    }
    
    private void removeStyle(UIStyle style) {
    	styles.remove(style);
    	removeDisplayElement((UIDisplayElement) style);
    }
    
    /**
     * Set the background color of this display element. The background color will fill the whole element.
     * @param r Red value. (0-255)
     * @param g Green value. (0-255)
     * @param b Blue value. (0-255)
     * @param a Alpha value. (0-1)
     */
    public void setBackgroundColor(int r, int g, int b, float a) {
        UIStyleBackgroundColor style = getStyle2(UIStyleBackgroundColor.class);
        if (style == null) {
        	style = new UIStyleBackgroundColor(r, g, b, a);
        	style.setSize(getSize());
        	style.setVisible(true);
        	addStyle(style, true);
        } else {
        	style.setColor(r, g, b, a);
        }
    }
    
    /**
     * Remove the background color from this display element.
     */
    public void removeBackgroundColor() {
    	UIStyleBackgroundColor style = getStyle2(UIStyleBackgroundColor.class);
    	if (style != null) {
    		removeStyle(style);
    	}
    }

    /**
     * Set the background Image for this display element.
     * @param texture The texture to load.
     */
    public void setBackgroundImage(String texture) {
        UIStyleBackgroundImage style = getStyle2(UIStyleBackgroundImage.class);
        
        //create the style if it not exists
        if (style == null) {
        	style = new UIStyleBackgroundImage(AssetManager.loadTexture(texture));
        	style.setVisible(true);
        	addStyle(style, true);
        }
        //edit the existing style
        else {
        	style.setTexture(AssetManager.loadTexture(texture));
        }
    }
    
    /**
     * Set the origin and size of the background in the texture file.
     * @param origin The origin.
     * @param size The size.
     */
    public void setBackgroundImageSource(Vector2f origin, Vector2f size) {
    	UIStyleBackgroundImage style = getStyle2(UIStyleBackgroundImage.class);
    	
        if (style != null) {
	        style.setTextureOrigin(origin);
	        style.setTextureSize(size);
        }
    }
    
    /**
     * Set the origin and size of the background on the display element. On default the background will fill the whole display element.
     * @param origin The origin.
     * @param size The size.
     */
    public void setBackgroundImageTarget(Vector2f origin, Vector2f size) {
    	UIStyleBackgroundImage style = getStyle2(UIStyleBackgroundImage.class);
    	
        if (style != null) {
	        style.setTarget(origin, size);
        }
    }
    
    /**
     * Remove the background image from this display element.
     */ 
    public void removeBackgroundImage() {
    	UIStyleBackgroundImage style = getStyle2(UIStyleBackgroundImage.class);
    	
    	if (style != null) {
    		removeStyle(style);
    	}
    }
    
    /**
     * Set the border for this display element.
     * @param width The width.
     * @param r Red value. (0-255)
     * @param g Green value. (0-255)
     * @param b Blue value. (0-255)
     * @param a Alpha value. (0-1)
     */
    public void setBorderSolid(float width, int r, int g, int b, float a) {
    	UIStyleBorderSolid style = getStyle2(UIStyleBorderSolid.class);

    	if (style == null) {
			style = new UIStyleBorderSolid(width, r, g, b, a);
			style.setVisible(true);
			addStyle(style, false);
    	} else {
    		style.setColor(r, g, b, a);
    		style.setWidth(width);
    	}
    }
    
    /**
     * Remove the border from this display element.
     */
    public void removeBorderSolid() {
    	UIStyleBorderSolid style = getStyle2(UIStyleBorderSolid.class);
    	
    	if (style != null) {
    		removeStyle(style);
    	}
    }
    
	/**
	 * Set the border from an image.
	 * @param texture The texture.
	 * @param origin The origin of the border in the texture.
	 * @param size The size of the border container in the texture.
	 * @param width The border width. x = top, y = right, z = bottom, w = left
	 */
    public void setBorderImage(String texture, Vector2f origin, Vector2f size, Vector4f borderSize) {
    	UIStyleBorderImage style = getStyle2(UIStyleBorderImage.class);

    	if (style == null) {
			style = new UIStyleBorderImage(AssetManager.loadTexture(texture));
			style.setBorderSource(origin, size, borderSize);
			style.setVisible(true);
			addStyle(style, false);
    	} else {

    	}
    }
    

    public void setBorderImageSource() {
    	UIStyleBorderImage style = getStyle2(UIStyleBorderImage.class);

    	if (style != null) {
    		
    	}
    }
    
    /**
     * Remove the border image from this display element.
     */
    public void removeBorderImage() {
    	UIStyleBorderImage style = getStyle2(UIStyleBorderImage.class);
    	
    	if (style != null) {
    		removeStyle(style);
    	}
    }
}
