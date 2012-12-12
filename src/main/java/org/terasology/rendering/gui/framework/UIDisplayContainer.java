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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.style.Style;
import org.terasology.rendering.gui.framework.style.StyleBackgroundColor;
import org.terasology.rendering.gui.framework.style.StyleBackgroundImage;
import org.terasology.rendering.gui.framework.style.StyleBorderImage;
import org.terasology.rendering.gui.framework.style.StyleBorderSolid;
import org.terasology.rendering.gui.framework.style.StyleShadow;
import org.terasology.rendering.gui.framework.style.StyleShadow.EShadowDirection;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collections;

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
    private final ArrayList<Style> styles = new ArrayList<Style>();

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
        
        UIDisplayElement styleElement;
        for (Style style : styles) {
            styleElement = ((UIDisplayElement)style);
            allowsCrop = cropContainer && !styleElement.isCrop();
            if (allowsCrop) {
                glDisable(GL_SCISSOR_TEST);
            }
            styleElement.renderTransformed();
            if (allowsCrop) {
                glEnable(GL_SCISSOR_TEST);
                glScissor(cropX, cropY, cropWidth, cropHeight);
            }
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
        
        super.update();
        
        //update styles
        for (Style style : styles) {
            ((UIDisplayElement)style).update();
        }

        // Update all display elements
        for (int i = 0; i < displayElements.size(); i++) {
            displayElements.get(i).update();
        }
    }
    
    public void layout() {
        if (!isVisible())
            return;
        
        super.layout();
        
        //update layout styles
        for (Style style : styles) {
            ((UIDisplayElement)style).layout();
        }

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
    public boolean processMouseInput(int button, boolean state, int wheelMoved, boolean consumed, boolean croped) {
        if (!isVisible())
            return consumed;
        
        //cancel mouse click event if the click is out of the cropped area
        if (cropContainer) {
            if (!intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
                croped = true;
            }
        }

        // Pass the mouse event to all display elements
        for (int i = displayElements.size() - 1; i >= 0; i--) {
            consumed = displayElements.get(i).processMouseInput(button, state, wheelMoved, consumed, croped);
        }
        
        consumed = super.processMouseInput(button, state, wheelMoved, consumed, croped);
        
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
        //element.setVisible(true);
        
        layout();
    }

    /**
     * Add a new display element to the display container at a particular position.
     * @param element The element to add.
     */
    public void addDisplayElementToPosition(int position, UIDisplayElement element) {
        displayElements.add(position, element);
        element.setParent(this);
        //element.setVisible(true);
        
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
     * 
     * @param element
     * @param index
     */
    public void orderDisplayElement(UIDisplayElement element, int index) {
        //TODO implement
    }
    
    /**
     * Change the z-order of the given display element. Move it to the top, over all other display elements.
     * @param element The element to change the z-order.
     */
    public void orderDisplayElementTop(UIDisplayElement element) {
        int pos = getDisplayElements().indexOf(element);
        
        if (pos != -1) {
            Collections.rotate(getDisplayElements().subList(pos, getDisplayElements().size()), -1);
        }
    }
    
    /**
     * Change the z-order of the given display element. Move it to the bottom, under all other display elements.
     * @param element The element to change the z-order.
     */
    public void orderDisplayElementBottom(UIDisplayElement element) {
        int pos = getDisplayElements().indexOf(element);
        
        if (pos != -1) {
            Collections.rotate(getDisplayElements().subList(pos, getDisplayElements().size()), 1);
        }
    }

    /**
     * Get the list of all display elements of the display container.
     * @return Returns the list of all display elements.
     */
    public ArrayList<UIDisplayElement> getDisplayElements() {        
        return displayElements;
    }

    /**
     * 
     * @param elementId
     * @return
     */
    public UIDisplayElement getElementById(String elementId) {
        UIDisplayElement ret = null;
        for (UIDisplayElement element : getDisplayElements()) {
            if (element.getId().equals(elementId)) {
                ret = element;
                break;
            }
            
            if (element instanceof UIDisplayContainer) {
                if ((ret = ((UIDisplayContainer)element).getElementById(elementId)) != null) {
                    break;
                }
            }
        }
        
        return ret;
    }
    
    /*
       Styling System
       Styles are just special child display elements of the container which offer a function which is commonly used, such as a background image
    */
    
    /**
     * Get a style by its class.
     * @param style The style class.
     * @return Returns the style class if one was added to the display container or null if none was added.
     */
    public <T> T getStyle(Class<T> style) {
        for (Style s : styles) {
            if (s.getClass() == style) {
                return style.cast(s);
            }
        }
        
        return null;
    }
    
    /**
     * Add a style.
     * @param style The style to add.
     */
    protected void addStyle(Style style) {        
        boolean added = false;
        for (int i = 0; i < styles.size(); i++) {
            if (styles.get(i).getLayer() > style.getLayer()) {
                styles.add(i, style);
                added = true;
                
                break;
            }
        }
        
        if (!added) {
            styles.add(style);
        }
        
        ((UIDisplayElement) style).setParent(this);
        
        layout();
    }
    
    /**
     * Removes a style.
     * @param style The style to remove.
     */
    protected void removeStyle(Style style) {
        styles.remove(style);
    }
    
    /**
     * Set the background color. The background color will fill the whole element.
     * @param r Red value. (0-255)
     * @param g Green value. (0-255)
     * @param b Blue value. (0-255)
     * @param a Alpha value. (0-1)
     */
    public void setBackgroundColor(Color color) {
        StyleBackgroundColor style = getStyle(StyleBackgroundColor.class);
        if (style == null) {
            style = new StyleBackgroundColor(color);
            style.setVisible(true);
            addStyle(style);
        } else {
            style.setColor(color);
        }
    }
    
    /**
     * Set the background color. The background color will fill the whole element.
     * @param color The hex color value. #FFFFFFFF = ARGB
     */
    public void setBackgroundColor(String color) {
        StyleBackgroundColor style = getStyle(StyleBackgroundColor.class);
        if (style == null) {
            style = new StyleBackgroundColor(color);;
            style.setVisible(true);
            addStyle(style);
        } else {
            style.setColor(color);
        }
    }
    
    /**
     * Remove the background color.
     */
    public void removeBackgroundColor() {
        StyleBackgroundColor style = getStyle(StyleBackgroundColor.class);
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the background image. Uses the whole texture.
     * @param texture The texture to load.
     */
    public void setBackgroundImage(String texture) {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);

        if (style == null) {
            style = new StyleBackgroundImage(Assets.getTexture(texture));
            style.setTextureOrigin(new Vector2f(0f, 0f));
            style.setTextureSize(new Vector2f(style.getTexture().getWidth(), style.getTexture().getHeight()));
            style.setVisible(true);
            addStyle(style);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(Assets.getTexture(texture));
            }
        }
    }

    /**
     * Set the background Image.
     * @param texture The texture to load.
     * @param origin The origin of the texture.
     * @param size The size of the texture.
     */
    public void setBackgroundImage(String texture, Vector2f origin, Vector2f size) {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);

        if (style == null) {
            style = new StyleBackgroundImage(Assets.getTexture(texture));
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
            style.setVisible(true);
            addStyle(style);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(Assets.getTexture(texture));
            }
            
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
        }
    }
    
    /**
     * Set the origin and size of the loaded background image texture. If no image was loaded this won't have any effect.
     * @param origin The origin of the texture.
     * @param size The size of the texture.
     */
    public void setBackgroundImage(Vector2f origin, Vector2f size) {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);

        if (style != null) {            
            style.setTextureOrigin(origin);
            style.setTextureSize(size);
        }
    }

    /**
     * Set the position of the background image. On default the background will fill the whole display element.
     * @param position The position.
     */
    public void setBackgroundImagePosition(Vector2f position) {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);
        
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
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);
        
        if (style != null) {
            style.setPosition(x, y);
        }
    }
    
    /**
     * Set the size of the background image. On default the background will fill the whole display element.
     * @param size
     */
    public void setBackgroundImageSize(Vector2f size) {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);
        
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
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);
        
        if (style != null) {
            style.setSize(width, height);
        }
    }
    
    /**
     * Remove the background image.
     */ 
    public void removeBackgroundImage() {
        StyleBackgroundImage style = getStyle(StyleBackgroundImage.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the border.
     * @param width The width of the border. x = top, y = right, z = bottom, w = left
     * @param color The color.
     */
    public void setBorderSolid(Vector4f width, Color color) {
        StyleBorderSolid style = getStyle(StyleBorderSolid.class);

        if (style == null) {
            style = new StyleBorderSolid(width, color);
            style.setSize("100%", "100%");
            style.setVisible(true);
            addStyle(style);
        } else {
            style.setColor(color);
            style.setWidth(width);
        }
    }
    
    /**
     * Set the border.
     * @param width The width of the border. x = top, y = right, z = bottom, w = left
     * @param color The hex color value. #FFFFFFFF = ARGB
     */
    public void setBorderSolid(Vector4f width, String color) {
        StyleBorderSolid style = getStyle(StyleBorderSolid.class);

        if (style == null) {
            style = new StyleBorderSolid(width, color);
            style.setSize("100%", "100%");
            style.setVisible(true);
            addStyle(style);
        } else {
            style.setColor(color);
            style.setWidth(width);
        }
    }
    
    /**
     * Remove the border.
     */
    public void removeBorderSolid() {
        StyleBorderSolid style = getStyle(StyleBorderSolid.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the border from an image.
     * @param texture The texture.
     * @param origin The origin of the border in the texture.
     * @param size The size of the border container in the texture.
     * @param width The border width for each side. x = top, y = right, z = bottom, w = left
     */
    public void setBorderImage(String texture, Vector2f origin, Vector2f size, Vector4f borderSize) {
        StyleBorderImage style = getStyle(StyleBorderImage.class);

        if (style == null) {
            style = new StyleBorderImage(Assets.getTexture(texture));
            style.setBorderSource(origin, size, borderSize);
            style.setVisible(true);
            addStyle(style);
        } else {
            //check if same texture is already loaded
            if (!style.getTexture().getURI().toString().equals("texture:" + texture)) {
                style.setTexture(Assets.getTexture(texture));
            }

            style.setBorderSource(origin, size, borderSize);
        }
    }

    /**
     * Remove the border image.
     */
    public void removeBorderImage() {
        StyleBorderImage style = getStyle(StyleBorderImage.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
    
    /**
     * Set the shadow.
     * @param width The width of the shadow for each side. x = top, y = right, z = bottom, w = left
     * @param direction The direction of the shadow. OUTSIDE draws a shadow which points away from the element. INSIDE draws a shadow which points to the center of the element.
     * @param opacity The opacity of the shadow. 0-1
     */
    public void setShadow(Vector4f width, EShadowDirection direction, float opacity) {
        StyleShadow style = getStyle(StyleShadow.class);
        
        if (style == null) {
            style = new StyleShadow(width, direction, opacity);
            style.setSize("100%", "100%");
            style.setVisible(true);
            addStyle(style);
        } else {
            style.setWidth(width);
            style.setDirection(direction);
            style.setOpacity(opacity);
        }
    }
    
    /**
     * Remove the shadow.
     */
    public void removeShadow() {
        StyleShadow style = getStyle(StyleShadow.class);
        
        if (style != null) {
            removeStyle(style);
        }
    }
}
