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

    //child elements
    private final ArrayList<UIDisplayElement> displayElements = new ArrayList<UIDisplayElement>();
    private final List<UIStyle> styles = new ArrayList<UIStyle>();
    
    //cropping
    private boolean cropContainer = false;
    protected Vector4f cropMargin = new Vector4f(0.0f, 0.0f,0.0f, 0.0f);

    public UIDisplayContainer() {
        super();
    }

    public void render() {
        boolean allowsCrop = false;
        int cropX = 0;
        int cropY = 0;
        int cropWidth = 0;
        int cropHeight = 0;

        if (!isVisible())
            return;

        //Cut the elements
        if (cropContainer) {
            cropX = (int) getAbsolutePosition().x - (int) (cropMargin.w);
            cropY = Display.getHeight() - (int) getAbsolutePosition().y - (int) getSize().y - (int) cropMargin.z;
            cropWidth = (int) getSize().x + (int) cropMargin.y;
            cropHeight = (int) getSize().y + (int) cropMargin.x;
            glEnable(GL_SCISSOR_TEST);
            glScissor(cropX, cropY, cropWidth, cropHeight);
        }

        // Render all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            allowsCrop = cropContainer && !displayElements.get(i).isCrop();
            if (allowsCrop) {
                glDisable(GL_SCISSOR_TEST);
            }
            displayElements.get(i).renderTransformed();
            if (allowsCrop) {
                glEnable(GL_SCISSOR_TEST);
                glScissor(cropX, cropY, cropWidth, cropHeight);
            }
        }

        if (cropContainer) {
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public void update() {
        if (!isVisible())
            return;

        // Update all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            displayElements.get(i).update();
        }
    }
    
    public void layout() {
        if (!isVisible())
            return;
        
        super.layout();

        // Update layout of all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            displayElements.get(i).layout();
        }
    }
    
    @Override
    public void processBindButton(BindButtonEvent event) {
        if (!isVisible())
            return;
        
        super.processBindButton(event);
        
        // Pass the bind key to all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            displayElements.get(i).processBindButton(event);
        }
    }

    @Override
    public void processKeyboardInput(KeyEvent event) {
        if (!isVisible())
            return;

        super.processKeyboardInput(event);

        // Pass the pressed key to all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            displayElements.get(i).processKeyboardInput(event);
        }
    }

    @Override
    public boolean processMouseInput(int button, boolean state, int wheelMoved, boolean consumed) {
        if (!isVisible())
            return consumed;

        // Pass the mouse event to all display elements
        for (int i = displayElements.size() - 1; i >= 0; i--) {
            consumed = displayElements.get(i).processMouseInput(button, state, wheelMoved, consumed);
        }
        
        consumed = super.processMouseInput(button, state, wheelMoved, consumed);
        
        return consumed;
    }
    
    /**
     * Set whether the child elements will be cropped to the size of the display container. To exclude particular child elements from cropping, use the setCrop method.
     * @param crop True to crop all child elements.
     */
    public void setCropContainer(boolean crop) {
        this.cropContainer = crop;
    }

    /**
     * Set crop margin for this container.
     * @param margin The margin where x = top, y = right, z = bottom, w = left
     */
    public void setCropMargin(Vector4f margin) {
        cropMargin = margin;
    }
    
    /*
       Methods to add or remove child elements of the container
    */

    /**
     * Add a new display element to the display container.
     * @param element The element to add.
     */
    public void addDisplayElement(UIDisplayElement element) {
        displayElements.add(element);
        element.setParent(this);
        
        layout();
    }

    /**
     * Add a new display element to the display container at a particular position.
     * @param element The element to add.
     */
    public void addDisplayElementToPosition(int position, UIDisplayElement element) {
        displayElements.add(position, element);
        element.setParent(this);
        
        layout();
    }

    /**
     * Remove a display element.
     * @param element The element to remove.
     */
    public void removeDisplayElement(UIDisplayElement element) {
        displayElements.remove(element);
        element.setParent(null);
        
        layout();
    }

    /**
     * Remove all display elements.
     */
    public void removeAllDisplayElements() {
        for (UIDisplayElement element : displayElements) {
            element.setParent(null);
        }
        displayElements.clear();
        
        layout();
    }

    /**
     * Get the list of all display elements of the display container.
     * @return Returns the list of all display elements.
     */
    public ArrayList<UIDisplayElement> getDisplayElements() {        
        return displayElements;
    }
    
    /*
       Styling System
       Styles are just special child display elements of the container which offer a function which is commonly used, such as a background image
    */
    
    /**
     * Get a style by its class.
     * @param style The style class.
     * @return Returns the style class if one was added to the display container. Null if none was added.
     */
    private <T> T getStyle(Class<T> style) {
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
     * TODO need to put particular styles at the particular position.. (background at the beginning, border on top) -> don't add styles to the display element list
     */
    private void addStyle(UIStyle style, boolean listStart) {
        styles.add(style);
        
        UIDisplayElement element = (UIDisplayElement) style;
        if (listStart) {
            displayElements.add(0, element);
        } else {
            displayElements.add(element);
        }
        
        element.setParent(this);
        layout();
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
        UIStyleBackgroundColor style = getStyle(UIStyleBackgroundColor.class);
        if (style == null) {
            style = new UIStyleBackgroundColor(r, g, b, a);
            style.setVisible(true);
            addStyle(style, true);
        } else {
            style.setColor(r, g, b, a);
        }
    }
    
    public void setBackgroundColor(String color, float a) {
        UIStyleBackgroundColor style = getStyle(UIStyleBackgroundColor.class);
        if (style == null) {
            style = new UIStyleBackgroundColor(color, a);;
            style.setVisible(true);
            addStyle(style, true);
        } else {
            style.setColor(color, a);
        }
    }
    
    /**
     * Remove the background color from this display element.
     */
    public void removeBackgroundColor() {
        UIStyleBackgroundColor style = getStyle(UIStyleBackgroundColor.class);
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the background image.
     * @param texture The texture to load.
     */
    public void setBackgroundImage(String texture) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);

        if (style == null) {
            style = new UIStyleBackgroundImage(AssetManager.loadTexture(texture));
            style.setTextureOrigin(new Vector2f(0f, 0f));
            style.setTextureSize(new Vector2f(style.getTexture().getWidth(), style.getTexture().getHeight()));
            style.setVisible(true);
            addStyle(style, true);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(AssetManager.loadTexture(texture));
            }
        }
    }
    
    /**
     * Set the origin and size of the loaded background image. If no image was loaded this won't have any effect.
     * @param origin The origin of the texture.
     * @param size The size of the texture.
     */
    public void setBackgroundImage(Vector2f origin, Vector2f size) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);

        if (style != null) {            
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
        }
    }

    /**
     * Set the background Image for this display element.
     * @param texture The texture to load.
     * @param origin The origin of the texture. Null reference will set the origin to 0,0
     * @param size The size of the texture. Null reference will set the size to the size of the whole texture.
     */
    public void setBackgroundImage(String texture, Vector2f origin, Vector2f size) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);

        if (style == null) {
            style = new UIStyleBackgroundImage(AssetManager.loadTexture(texture));
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
            style.setVisible(true);
            addStyle(style, true);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(AssetManager.loadTexture(texture));
            }
            
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
        }
    }

    /**
     * Set the position of the background image. On default the background will fill the whole display element.
     * @param position The position.
     */
    public void setBackgroundImagePosition(Vector2f position) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);
        
        if (style != null) {
            style.setPosition(position);
        }
    }
    
    /**
     * Set the position of the background image including its unit. The unit can be pixel (px) or percentage (%). If no unit is given the default unit pixel will be used.
     * @param x The x position to set including the unit.
     * @param y The y position to set including the unit.
     */
    public void setBackgroundImagePosition(String x, String y) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);
        
        if (style != null) {
            style.setPosition(x, y);
        }
    }
    
    /**
     * Set the size of the background image. On default the background will fill the whole display element.
     * @param size
     */
    public void setBackgroundImageSize(Vector2f size) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);
        
        if (style != null) {
            style.setSize(size);
        }
    }
    
    /**
     * Set the size of the background image including its unit. The unit can be pixel (px) or percentage (%). If no unit is given the default unit pixel will be used.
     * @param width The width to set including the unit.
     * @param height The height to set including the unit.
     */
    public void setBackgroundImageSize(String width, String height) {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);
        
        if (style != null) {
            style.setSize(width, height);
        }
    }
    
    /**
     * Remove the background image from this display element.
     */ 
    public void removeBackgroundImage() {
        UIStyleBackgroundImage style = getStyle(UIStyleBackgroundImage.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the border for this display element.
     * @param width The width of the border.
     * @param r Red value. (0-255)
     * @param g Green value. (0-255)
     * @param b Blue value. (0-255)
     * @param a Alpha value. (0-1)
     */
    public void setBorderSolid(float width, int r, int g, int b, float a) {
        UIStyleBorderSolid style = getStyle(UIStyleBorderSolid.class);

        if (style == null) {
            style = new UIStyleBorderSolid(width, r, g, b, a);
            style.setSize("100%", "100%");
            style.setVisible(true);
            addStyle(style, false);
        } else {
            style.setColor(r, g, b, a);
            style.setWidth(width);
        }
    }
    
    /**
     * Set the border for this display element.
     * @param width The width of the border.
     * @param color The hex color value. (#FFFFFF)
     * @param a Alpha value. (0-1)
     */
    public void setBorderSolid(float width, String color, float a) {
        UIStyleBorderSolid style = getStyle(UIStyleBorderSolid.class);

        if (style == null) {
            style = new UIStyleBorderSolid(width, color, a);
            style.setSize("100%", "100%");
            style.setVisible(true);
            addStyle(style, false);
        } else {
            style.setColor(color, a);
            style.setWidth(width);
        }
    }
    
    /**
     * Remove the border from this display element.
     */
    public void removeBorderSolid() {
        UIStyleBorderSolid style = getStyle(UIStyleBorderSolid.class);
        
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
        UIStyleBorderImage style = getStyle(UIStyleBorderImage.class);

        if (style == null) {
            style = new UIStyleBorderImage(AssetManager.loadTexture(texture));
            style.setBorderSource(origin, size, borderSize);
            style.setVisible(true);
            addStyle(style, false);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(AssetManager.loadTexture(texture));
            }

            style.setBorderSource(origin, size, borderSize);
        }
    }
    
    /**
     * Remove the border image from this display element.
     */
    public void removeBorderImage() {
        UIStyleBorderImage style = getStyle(UIStyleBorderImage.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
}
