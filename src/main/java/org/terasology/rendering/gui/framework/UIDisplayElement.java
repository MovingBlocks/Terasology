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

import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.FocusListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * Base class for all displayable UI elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 * TODO improve z-order for click events
 */
public abstract class UIDisplayElement {

    protected static UIDisplayElement focusedElement;
    private UIDisplayElement parent;
    
    //events
    private final ArrayList<MouseMoveListener> mouseListeners = new ArrayList<MouseMoveListener>();
    private final ArrayList<MouseButtonListener> mouseButtonListeners = new ArrayList<MouseButtonListener>();
    private final ArrayList<ClickListener> clickListeners = new ArrayList<ClickListener>();
    private final ArrayList<FocusListener> focusListeners = new ArrayList<FocusListener>();
    private static enum EMouseEvents {ENTER, LEAVE, HOVER, MOVE};
    private EMouseEvents lastMouseState;
    private boolean mouseIsDown = false;
    
    //layout
    private boolean isVisible = false;
    private boolean isFixed = false;
    private boolean isCrop = true;
    
    //align
    public static enum EVerticalAlign {TOP, CENTER, BOTTOM};
    public static enum EHorizontalAlign {LEFT, CENTER, RIGHT};
    private EVerticalAlign verticalAlign = EVerticalAlign.TOP;
    private EHorizontalAlign horizontalAlign = EHorizontalAlign.LEFT;
    
    //position and size
    public static enum EUnitType {PIXEL, PERCENTAGE};
    private EUnitType unitPositionX = EUnitType.PIXEL;
    private EUnitType unitPositionY = EUnitType.PIXEL;
    private EUnitType unitSizeX = EUnitType.PIXEL;
    private EUnitType unitSizeY = EUnitType.PIXEL;
    
    public static enum EPositionType {ABSOLUTE, RELATIVE};
    private EPositionType positionType = EPositionType.RELATIVE;
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f positionOriginal  = new Vector2f(0, 0);
    
    private final Vector2f size = new Vector2f(0, 0);
    private final Vector2f sizeOriginal = new Vector2f(0, 0);
    
    public UIDisplayElement() {
    }

    public UIDisplayElement(Vector2f position) {
        this.position.set(position);
    }

    public UIDisplayElement(Vector2f position, Vector2f size) {
        this.position.set(position);
        this.size.set(size);
    }

    public void renderTransformed() {
        ShaderManager.getInstance().enableDefault();

        if (isVisible()) {
            if (positionType == EPositionType.RELATIVE) {
                glPushMatrix();
                glTranslatef(getPosition().x, getPosition().y, 0);
                render();
                glPopMatrix();
            } else if (positionType == EPositionType.ABSOLUTE) {
                glPushMatrix();
                glLoadIdentity();
                glTranslatef(getPosition().x, getPosition().y, 0);
                render();
                glPopMatrix();
            }
        }
    }
    
    /**
     * Process the bind keyboard input. Bind keyboard input will be passed down from the GUI Manager to each element within the active window.
     * @param event The event which contains all necessary information.
     */
    public void processBindButton(BindButtonEvent event) {
        //TODO process bind buttons
    }

    /**
     * Process the raw keyboard input. Keyboard input will be passed down from the GUI Manager to each element within the active window.
     * @param event The event which contains all necessary information.
     */
    public void processKeyboardInput(KeyEvent event) {
        //TODO process raw keyboard
    }

    /**
     * Process the mouse input. Mouse input will be passed down from the GUI Manager to each element within the active window.
     * @param button The button. 0 = left, 1 = right, 2 = middle. If no button was pressed the value will be -1.
     * @param state The state of the button. True if the button is pressed.
     * @param wheelMoved The value of how much the mouse wheel was moved. If the value is greater than 0, the mouse wheel was moved up. If lower than 0 the mouse wheel was moved down.
     */
    public boolean processMouseInput(int button, boolean state, int wheelMoved, boolean consumed) {
        if (!isVisible())
            return consumed;

        if (mouseListeners.size() > 0 || mouseButtonListeners.size() > 0 || clickListeners.size() > 0) {
            if (intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
                if (!consumed) {
                    //mouse button listeners
                    if (button != -1 && state && !mouseIsDown) {            //mouse down
                        notifyMouseButtonListeners(button, true, wheelMoved, true);
                        mouseIsDown = true;
                    } else if (button != -1 && !state && mouseIsDown) {    //mouse up
                        notifyClickListeners(button);
                        notifyMouseButtonListeners(button, false, wheelMoved, true);
                        mouseIsDown = false;
                    }
                    
                    //mouse position listeners
                    notifyMouseListeners(EMouseEvents.HOVER);
                    
                    if (lastMouseState == EMouseEvents.LEAVE || lastMouseState == null) {
                        notifyMouseListeners(EMouseEvents.ENTER);
                        lastMouseState = EMouseEvents.ENTER;
                    }
                    
                    consumed = true;
                } else {
                    if (button != -1 && !state && mouseIsDown) {    //mouse up
                        notifyClickListeners(button);
                        notifyMouseButtonListeners(button, false, wheelMoved, true);
                        mouseIsDown = false;
                    }
                    
                    if (lastMouseState == EMouseEvents.ENTER || lastMouseState == null) {
                        notifyMouseListeners(EMouseEvents.LEAVE);
                        lastMouseState = EMouseEvents.LEAVE;
                    }
                }
                
                //mouse wheel listeners
                if (wheelMoved != 0) {
                    notifyMouseButtonListeners(-1, false, wheelMoved, true);
                }
            }
            else {
                //mouse button listeners
                if (button != -1 && state && !mouseIsDown) {            //mouse down
                    notifyMouseButtonListeners(button, true, wheelMoved, false);
                } else if (button != -1 && !state) {    //mouse up
                    notifyMouseButtonListeners(button, false, wheelMoved, false);
                    mouseIsDown = false;
                }
                
                //mouse wheel listeners
                if (wheelMoved != 0) {
                    notifyMouseButtonListeners(-1, false, wheelMoved, false);
                }
                
                //mouse position listeners
                if (lastMouseState == EMouseEvents.ENTER || lastMouseState == null) {
                    notifyMouseListeners(EMouseEvents.LEAVE);
                    lastMouseState = EMouseEvents.LEAVE;
                }
            }
        }
        
        //check for no changes in button presses -> this means mouse was moved
        if (mouseListeners.size() > 0 && button == -1 && wheelMoved == 0) {
            notifyMouseListeners(EMouseEvents.MOVE);
        }
        
        return consumed;
    }

    /**
     * Check whether the display element has the focus or not.
     * @return Returns true if the display element has the focus.
     */
    public boolean isFocused() {
        if (focusedElement == this) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Set whether the display element should be focused.
     * @param focus True if the display element should be focused.
     */
    public void setFocus(UIDisplayElement focus) {
        if (focusedElement != focus) {
            if (focus == null && focusedElement != this) {
                return;
            }
            
            if (focusedElement != null) {
                focusedElement.notifyFocusListeners(false);
            }
 
            focusedElement = focus;
            if (focusedElement != null) {
                focusedElement.notifyFocusListeners(true);
            }
        }
    }

    /**
     * Render the display element. Will be executed every tick.
     */
    public abstract void render();

    /**
     * Update the display element. For tasks which needs continuously updates. Will be executed every tick and needs to be avoided.
     * 
     * TODO remove? isn't really needed anymore.
     */
    public abstract void update();
    
    /**
     * Set the layout of the child elements here. Will be executed if the display or a parent window was resized.
     */
    public void layout() {
        
        //reset to the position and size to the original position
        position.set(positionOriginal);
        size.set(sizeOriginal);
        
        /*
           Position
        */
        
        position.x = calcHorizontalAlign(horizontalAlign);
        position.y = calcVerticalAlign(verticalAlign);

        //recalculate position x relative to parent if unit is percentage
        if (unitPositionX == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getPosition().x += parent.getSize().x * positionOriginal.x / 100f;
        } 
        //recalculate position x absolute to display if unit is percentage
        else if (unitPositionX == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            getPosition().x += Display.getWidth() * positionOriginal.x / 100f;
        } else {
            getPosition().x += positionOriginal.x;
        }
        
        //recalculate position x relative to parent if unit is percentage
        if (unitPositionY == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getPosition().y += parent.getSize().y * positionOriginal.y / 100f;
        } 
        //recalculate position x absolute to display if unit is percentage
        else if (unitPositionY == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            getPosition().y += Display.getHeight() * positionOriginal.y / 100f;
        } else {
            getPosition().y += positionOriginal.y;
        }

        /*
           Size
        */
        
        //recalculate width relative to parent if unit is percentage
        if (unitSizeX == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getSize().x = parent.getSize().x * size.x / 100f;
        } 
        //recalculate width absolute to display if unit is percentage
        else if (unitSizeX == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            getSize().x = Display.getWidth() * size.x / 100f;
        }
        
        //recalculate height relative to parent if unit is percentage
        if (unitSizeY == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getSize().y = parent.getSize().y * size.y / 100f;
        } 
        //recalculate height absolute to display if unit is percentage
        else if (unitSizeY == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            getSize().y = Display.getHeight() * size.y / 100f;
        }
    }
    
    private float calcHorizontalAlign(EHorizontalAlign align) {
        
        if (align == EHorizontalAlign.LEFT) {
            
            return 0f;
            
        } else if (align == EHorizontalAlign.CENTER) {
            
            if (positionType == EPositionType.RELATIVE && parent != null) {
                return parent.getSize().x / 2 - size.x / 2;
            } else if (positionType == EPositionType.ABSOLUTE) {
                return Display.getWidth() / 2 - size.x / 2;
            }
            
        } else if (align == EHorizontalAlign.RIGHT) {
            if (positionType == EPositionType.RELATIVE && parent != null) {
                return parent.getSize().x - size.x;
            } else if (positionType == EPositionType.ABSOLUTE) {
                return Display.getWidth() - size.x;
            }
            
        }
        
        return 0f;
    }
    
    private float calcVerticalAlign(EVerticalAlign align) {
        
        if (align == EVerticalAlign.TOP) {
            return 0f;
        } else if (align == EVerticalAlign.CENTER) {
            if (positionType == EPositionType.RELATIVE && parent != null) {
                return parent.getSize().y / 2 - size.y / 2;
            } else if (positionType == EPositionType.ABSOLUTE) {
                return Display.getHeight() / 2 - size.y / 2;
            }
        } else if (align == EVerticalAlign.BOTTOM) {
            if (positionType == EPositionType.RELATIVE && parent != null) {
                return parent.getSize().y - size.y;
            } else if (positionType == EPositionType.ABSOLUTE) {
                return Display.getHeight() - size.y;
            }
        }
        
        return 0f;
    }
    
    public EPositionType getPositionType() {
        return positionType;
    }

    public void setPositionType(EPositionType positionType) {
        this.positionType = positionType;
    }
    
    public EVerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(EVerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    public EHorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(EHorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    /**
     * Set the position of the display element.
     * @return
     */
    public Vector2f getPosition() {
        return position;
    }

    /**
     * Set the position of the display element.
     * @param position The position to set.
     */
    public void setPosition(Vector2f position) {
        if (isFixed) {
            return;
        }
        
        unitPositionX = EUnitType.PIXEL;
        unitPositionY = EUnitType.PIXEL;
        
        this.position.set(position);
        this.positionOriginal.set(position);
    }
    
    /**
     * Set the position of the display element including its unit. The unit can be pixel (px) or percentage (%). If no unit is given the default unit pixel will be used.
     * @param x The x position to set including the unit.
     * @param y The y position to set including the unit.
     */
    public void setPosition(String x, String y) {
        x = x.replace(" ", "").toLowerCase();
        y = y.replace(" ", "").toLowerCase();
        
        float posX = 0;
        float posY = 0;
        
        try {
            if (x.matches("^\\d+(\\.\\d+)?%$")) {
                posX = Float.valueOf(x.substring(0, x.length() - 1));
                unitPositionX = EUnitType.PERCENTAGE;
            } else if (x.matches("^\\d+(\\.\\d+)?px$")) {
                posX = Float.valueOf(x.substring(0, x.length() - 2));
                unitPositionX = EUnitType.PIXEL;
            } else if (x.matches("^\\d+(\\.\\d+)?$")) {
                posX = Float.valueOf(x);
                unitPositionX = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        try {
            if (y.matches("^\\d+(\\.\\d+)?%$")) {
                posY = Float.valueOf(y.substring(0, y.length() - 1));
                unitPositionY = EUnitType.PERCENTAGE;
            } else if (y.matches("^\\d+(\\.\\d+)?px$")) {
                posY = Float.valueOf(y.substring(0, y.length() - 2));
                unitPositionY = EUnitType.PIXEL;
            } else if (y.matches("^\\d+(\\.\\d+)?$")) {
                posY = Float.valueOf(y);
                unitPositionY = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        this.size.set(posX, posY);
        this.sizeOriginal.set(posX, posY);
    }

    /**
     * Get the size of the display element.
     * @return Returns the size of the display element.
     */
    public Vector2f getSize() {
        return size;
    }

    /**
     * Set the size of the display element.
     * @param size The size to set.
     */
    public void setSize(Vector2f size) {
        if (isFixed) {
            return;
        }
        
        unitSizeX = EUnitType.PIXEL;
        unitSizeY = EUnitType.PIXEL;
        
        this.size.set(size);
        this.sizeOriginal.set(size);
        
        layout();
    }
    
    /**
     * Set the size of the display element including its unit. The unit can be pixel (px) or percentage (%). If no unit is given the default unit pixel will be used.
     * @param width The width to set including the unit.
     * @param height The height to set including the unit.
     */
    public void setSize(String width, String height) {
        width = width.replace(" ", "").toLowerCase();
        height = height.replace(" ", "").toLowerCase();
        
        float widthValue = 0;
        float heightValue = 0;
        
        try {
            if (width.matches("^\\d+(\\.\\d+)?%$")) {
                widthValue = Float.valueOf(width.substring(0, width.length() - 1));
                unitSizeX = EUnitType.PERCENTAGE;
            } else if (width.matches("^\\d+(\\.\\d+)?px$")) {
                widthValue = Float.valueOf(width.substring(0, width.length() - 2));
                unitSizeX = EUnitType.PIXEL;
            } else if (width.matches("^\\d+(\\.\\d+)?$")) {
                widthValue = Float.valueOf(width);
                unitSizeX = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        try {
            if (height.matches("^\\d+(\\.\\d+)?%$")) {
                heightValue = Float.valueOf(height.substring(0, height.length() - 1));
                unitSizeY = EUnitType.PERCENTAGE;
            } else if (height.matches("^\\d+(\\.\\d+)?px$")) {
                heightValue = Float.valueOf(height.substring(0, height.length() - 2));
                unitSizeY = EUnitType.PIXEL;
            } else if (height.matches("^\\d+(\\.\\d+)?$")) {
                heightValue = Float.valueOf(height);
                unitSizeY = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        this.size.set(widthValue, heightValue);
        this.sizeOriginal.set(widthValue, heightValue);
        
        layout();
    }
    
    /**
     * Check whether the display element is visible.
     * @return
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the visibility of the display element.
     * @param visible True to set the element visible.
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
        
        if (visible) {
            layout();
        }
    }
    
    /**
     * Check whether the display element can be moved or resized.
     * @return Returns true if the display element is fixed.
     * 
     */
    public boolean isFixed() {
        return isFixed;
    }
    
    /**
     * Set whether the display element can be moved or resized.
     * @param fix True to fix the elements position and size.
     */
    public void setFixed(boolean fix) {
        isFixed = fix;
    }
    
    /**
     * Check whether the element can be cropped.
     * @return
     */
    public boolean isCrop() {
        return isCrop;
    }
    
    /**
     * Set if the the element can be cropped. As default all elements can be cropped.
     * @param crop True to allow cropping.
     */
    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    /**
     * Get the parent of the display element.
     * @return Returns the parent of the display element or null if no parent was assigned.
     */
    public UIDisplayElement getParent() {
        return parent;
    }

    /**
     * Set the parent of the display element.
     * @param parent The parent to set.
     */
    public void setParent(UIDisplayElement parent) {
        this.parent = parent;
    }

    /**
     * Returns true if the given point intersects the display element.
     *
     * @param point The point to test
     * @return True if intersecting
     */
    public boolean intersects(Vector2f point) {
        return (point.x >= getAbsolutePosition().x && point.y >= getAbsolutePosition().y && point.x <= getAbsolutePosition().x + getSize().x && point.y <= getAbsolutePosition().y + getSize().y);
    }

    /**
     * Calculate the absolute position of the display element.
     * @return Returns the absolute position.
     */
    public Vector2f getAbsolutePosition() {
        if (positionType == EPositionType.RELATIVE) {
            if (parent == null) {
                return position;
            } else {
                return new Vector2f(parent.getAbsolutePosition().x + position.x, parent.getAbsolutePosition().y + position.y);
            }
        } else if (positionType == EPositionType.ABSOLUTE) {
            return position;
        }
        
        return position;
    }

    /*
       The event listeners which every display element in the UI supports
    */
    
    private void notifyMouseButtonListeners(int button, boolean state, int wheel, boolean intersect) {
        if (button == -1) {
            for (MouseButtonListener listener : mouseButtonListeners) {
                listener.wheel(this, wheel, intersect);
            }
        }
        else if (state) {
            for (MouseButtonListener listener : mouseButtonListeners) {
                listener.down(this, button, intersect);
            }
        }
        else {
            for (MouseButtonListener listener : mouseButtonListeners) {
                listener.up(this, button, intersect);
            }
        }      
    }
    
    public void addMouseButtonListener(MouseButtonListener listener) {
        mouseButtonListeners.add(listener);
    }

    public void removeMouseButtonListener(MouseButtonListener listener) {
        mouseButtonListeners.remove(listener);
    }
    
    private void notifyClickListeners(int value) {
        for (ClickListener listener : clickListeners) {
            listener.click(this, value);
        }
    }
    
    public void addClickListener(ClickListener listener) {
        clickListeners.add(listener);
    }

    public void removeClickListener(ClickListener listener) {
        clickListeners.remove(listener);
    }
    
    private void notifyFocusListeners(boolean focus) {
        if (focus) {
            for (FocusListener listener : focusListeners) {
                listener.focusOn(this);
            }
        }
        else {
            for (FocusListener listener : focusListeners) {
                listener.focusOff(this);
            }
        }
    }
    
    public void addFocusListener(FocusListener listener) {
        focusListeners.add(listener);
    }

    public void removeFocusListener(FocusListener listener) {
        focusListeners.remove(listener);
    }
    
    private void notifyMouseListeners(EMouseEvents type) {
        switch (type) {
        case ENTER:
            for (MouseMoveListener listener : mouseListeners) {
                listener.enter(this);
            }
        break;
        case LEAVE:
            for (MouseMoveListener listener : mouseListeners) {
                listener.leave(this);
            }
        break;
        case HOVER:
            for (MouseMoveListener listener : mouseListeners) {
                listener.hover(this);
            }
        break;
        case MOVE:
            for (MouseMoveListener listener : mouseListeners) {
                listener.move(this);
            }
        break;
        }
    }
    
    public void addMouseMoveListener(MouseMoveListener listener) {
        mouseListeners.add(listener);
    }

    public void removeMouseMoveListener(MouseMoveListener listener) {
        mouseListeners.remove(listener);
    }
}
