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
package org.terasology.rendering.gui.framework;

import com.google.common.collect.Lists;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.gui.animation.Animation;
import org.terasology.rendering.gui.framework.events.AnimationListener;
import org.terasology.rendering.gui.framework.events.BindKeyListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.FocusListener;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.VisibilityListener;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Base class for all displayable UI elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         <p/>
 *         TODO remove this class, move this to UIDisplayContainer
 */
public abstract class UIDisplayElement {
    protected static UIDisplayElement focusedElement;

    private static final Logger logger = LoggerFactory.getLogger(UIDisplayElement.class);

    protected final Vector2f positionOriginal = new Vector2f(0, 0);
    protected final Vector2f sizeOriginal = new Vector2f(0, 0);

    protected EUnitType unitPositionX = EUnitType.PIXEL;
    protected EUnitType unitPositionY = EUnitType.PIXEL;
    protected EUnitType unitSizeX = EUnitType.PIXEL;
    protected EUnitType unitSizeY = EUnitType.PIXEL;

    protected EPositionType positionType = EPositionType.RELATIVE;

    private UIWindow window;
    private UIDisplayElement parent;
    private String id = "";

    //events
    private final List<VisibilityListener> visibilityListeners = new ArrayList<>();
    private final List<MouseMoveListener> mouseMoveListeners = new ArrayList<>();
    private final List<MouseButtonListener> mouseButtonListeners = new ArrayList<>();
    private final List<ClickListener> clickListeners = new ArrayList<>();
    private final List<ClickListener> doubleClickListeners = new ArrayList<>();
    private final List<FocusListener> focusListeners = new ArrayList<>();
    private final List<KeyListener> keyListeners = new ArrayList<>();
    private final List<BindKeyListener> bindKeyListeners = new ArrayList<>();

    private static enum EMouseEvents {
        ENTER,
        LEAVE,
        HOVER,
        MOVE
    }

    private EMouseEvents lastMouseState;

    private final long doubleClickTimeout = 200;
    private long lastTime;
    private int lastButton = -1;

    private boolean mouseIsDown;
    private boolean consumeEvents = true;

    //layout
    private boolean isVisible = true;
    private boolean isFixed;
    private boolean isCrop = true;

    //align
    public static enum EVerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    public static enum EHorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    private EVerticalAlign verticalAlign = EVerticalAlign.TOP;
    private EHorizontalAlign horizontalAlign = EHorizontalAlign.LEFT;

    //position type
    public static enum EPositionType {
        ABSOLUTE,
        RELATIVE
    }

    //position and size
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f size = new Vector2f(0, 0);

    //position and size unit
    public static enum EUnitType {
        PIXEL, PERCENTAGE
    }

    //animation
    private final List<Animation> animations = new ArrayList<>();

    //
    private Object userData;

    public UIDisplayElement() {

    }

    public void renderTransformed() {
        CoreRegistry.get(ShaderManager.class).enableDefault();

        if (isVisible()) {

            for (Animation animation : animations) {
                if (animation.isStarted()) {
                    animation.renderBegin();
                }
            }

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

            for (Animation animation : animations) {
                if (animation.isStarted()) {
                    animation.renderEnd();
                }
            }

        }
    }

    /**
     * Process the bind keyboard input. Bind keyboard input will be passed down from the GUI Manager to each element within the active window.
     *
     * @param event The event which contains all necessary information.
     */
    public void processBindButton(BindButtonEvent event) {
        notifyBindKeyListeners(event);
    }

    /**
     * Process the raw keyboard input. Keyboard input will be passed down from the GUI Manager to each element within the active window.
     *
     * @param event The event which contains all necessary information.
     */
    public void processKeyboardInput(KeyEvent event) {
        notifyKeyListeners(event);
    }

    /**
     * Process the mouse input. Mouse input will be passed down from the GUI Manager to each element within the active window.
     *
     * @param button             The button. 0 = left, 1 = right, 2 = middle. If no button was pressed the value will be -1.
     * @param state              The state of the button. True if the button is pressed.
     * @param wheelMoved         The value of how much the mouse wheel was moved. If the value is greater than 0, the mouse wheel was moved up.
     *                           If lower than 0 the mouse wheel was moved down.
     * @param previouslyConsumed True if the input event was already consumed by another widget.
     */
    public boolean processMouseInput(int button, boolean state, int wheelMoved, boolean previouslyConsumed, boolean previouslyCropped) {
        if (!isVisible()) {
            return previouslyConsumed;
        }

        boolean cropped = previouslyCropped;
        boolean consumed = previouslyConsumed;
        if (mouseMoveListeners.size() > 0 || mouseButtonListeners.size() > 0 || clickListeners.size() > 0 || doubleClickListeners.size() > 0) {
            if (intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
                if (!cropped) {
                    if (lastMouseState == EMouseEvents.ENTER && (consumed || cropped)) {
                        notifyMouseMoveListeners(EMouseEvents.LEAVE, consumed);
                        lastMouseState = EMouseEvents.LEAVE;
                    }

                    //mouse button listeners
                    if (button != -1 && state && !mouseIsDown) {            //mouse down
                        notifyMouseButtonListeners(button, true, wheelMoved, true, consumed);
                        if (!consumed) {
                            mouseIsDown = true;
                            if (consumeEvents) {
                                consumed = true;
                            }
                        }
                    } else if (button != -1 && !state && mouseIsDown) {    //mouse up
                        notifyClickListeners(button, consumed);
                        notifyMouseButtonListeners(button, false, wheelMoved, true, consumed);
                        mouseIsDown = false;

                        //check double click
                        if ((System.currentTimeMillis() - lastTime) < doubleClickTimeout && lastButton == button) {
                            notifyDoubleClickListeners(button);
                        }
                        lastTime = System.currentTimeMillis();
                        lastButton = button;

                        if (!consumed && consumeEvents) {
                            consumed = true;
                        }
                    }

                    if (lastMouseState == EMouseEvents.LEAVE || lastMouseState == null) {
                        notifyMouseMoveListeners(EMouseEvents.ENTER, consumed);
                        if (!consumed) {
                            lastMouseState = EMouseEvents.ENTER;
                            if (consumeEvents) {
                                consumed = true;
                            }
                        }
                    }

                    //mouse wheel listeners
                    if (wheelMoved != 0) {
                        notifyMouseButtonListeners(-1, false, wheelMoved, true, consumed);
                    }

                    //mouse position listeners
                    notifyMouseMoveListeners(EMouseEvents.HOVER, consumed);

                    if (!consumed) {
                        lastMouseState = EMouseEvents.ENTER;
                        if (consumeEvents) {
                            consumed = true;
                        }
                    }
                } else {
                    if (lastMouseState == EMouseEvents.ENTER) {
                        notifyMouseMoveListeners(EMouseEvents.LEAVE, consumed);
                        lastMouseState = EMouseEvents.LEAVE;
                    }
                }
            } else {
                //mouse button listeners
                if (button != -1 && state && !mouseIsDown) {            //mouse down
                    notifyMouseButtonListeners(button, true, wheelMoved, false, consumed);
                } else if (button != -1 && !state) {    //mouse up
                    notifyMouseButtonListeners(button, false, wheelMoved, false, consumed);
                    mouseIsDown = false;
                }

                //mouse wheel listeners
                if (wheelMoved != 0) {
                    notifyMouseButtonListeners(-1, false, wheelMoved, false, consumed);
                }

                //mouse position listeners
                if (lastMouseState == EMouseEvents.ENTER || lastMouseState == null) {
                    notifyMouseMoveListeners(EMouseEvents.LEAVE, consumed);
                    lastMouseState = EMouseEvents.LEAVE;
                }
            }
        }

        //check for no changes in button presses -> this means mouse was moved
        if (mouseMoveListeners.size() > 0 && button == -1 && wheelMoved == 0) {
            notifyMouseMoveListeners(EMouseEvents.MOVE, consumed);
        }

        return consumed;
    }

    /**
     * Check whether the display element has the focus or not.
     *
     * @return Returns true if the display element has the focus.
     */
    public boolean isFocused() {
        return focusedElement == this;
    }

    /**
     * Set whether the display element should be focused.
     *
     * @param focus True if the display element should be focused.
     */
    public void setFocus(UIDisplayElement focus) {
        if (focusedElement != focus && (focus == null || !focus.isParentOf(focusedElement))) {
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

    private boolean isParentOf(UIDisplayElement otherElement) {
        UIDisplayElement nextElement = otherElement;
        while (nextElement != null) {
            if (nextElement == this) {
                return true;
            }
            nextElement = nextElement.parent;
        }
        return false;
    }

    /**
     * Render the display element. Will be executed every tick.
     */
    public abstract void render();

    /**
     * Update the display element. For tasks which needs continuously updates. Will be executed every tick and needs to be avoided.
     */
    public void update() {
        for (Animation animation : animations) {
            if (animation.isStarted()) {
                animation.update();
            }
        }
    }

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
        } else if (unitPositionX == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            //recalculate position x absolute to display if unit is percentage
            getPosition().x += Display.getWidth() * positionOriginal.x / 100f;
        } else {
            getPosition().x += positionOriginal.x;
        }

        //recalculate position x relative to parent if unit is percentage
        if (unitPositionY == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getPosition().y += parent.getSize().y * positionOriginal.y / 100f;
        } else if (unitPositionY == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            //recalculate position x absolute to display if unit is percentage
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
        } else if (unitSizeX == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            //recalculate width absolute to display if unit is percentage
            getSize().x = Display.getWidth() * size.x / 100f;
        }

        //recalculate height relative to parent if unit is percentage
        if (unitSizeY == EUnitType.PERCENTAGE && positionType == EPositionType.RELATIVE && parent != null) {
            getSize().y = parent.getSize().y * size.y / 100f;
        } else if (unitSizeY == EUnitType.PERCENTAGE && positionType == EPositionType.ABSOLUTE) {
            //recalculate height absolute to display if unit is percentage
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

    /**
     * Get the position type which can be RELATIVE or ABSOLUTE.
     *
     * @return Returns the position type.
     */
    public EPositionType getPositionType() {
        return positionType;
    }

    /**
     * Set the position type which can be RELATIVE or ABSOLUTE.
     *
     * @param positionType
     */
    public void setPositionType(EPositionType positionType) {
        this.positionType = positionType;
    }

    /**
     * Get the vertical align which describes the position of the element in its parent element or on the display,
     * depending on the position type (<i>RELATIVE</i> or <i>ABSOLUTE</i>).
     * The position will be added as an offset to the the <i>TOP</i>, <i>CENTER</i> or <i>BOTTOM</i> align.
     *
     * @return Returns the vertical align.
     */
    public EVerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * Set the vertical align which describes the position of the element in its parent element or on the display,
     * depending on the position type (<i>RELATIVE</i> or <i>ABSOLUTE</i>).
     * The position will be added as an offset to the the <i>TOP</i>, <i>CENTER</i> or <i>BOTTOM</i> align.
     *
     * @param verticalAlign The vertical align.
     */
    public void setVerticalAlign(EVerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * Get the horizontal align which describes the position of the element in its parent element or on the display,
     * depending on the position type (<i>RELATIVE</i> or <i>ABSOLUTE</i>).
     * The position will be added as an offset to the the <i>LEFT</i>, <i>CENTER</i> or <i>RIGHT</i> align.
     *
     * @return Returns the horizontal align.
     */
    public EHorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    /**
     * Set the horizontal align which describes the position of the element in its parent element or on the display,
     * depending on the position type (<i>RELATIVE</i> or <i>ABSOLUTE</i>).
     * The position will be added as an offset to the the <i>LEFT</i>, <i>CENTER</i> or <i>RIGHT</i> align.
     *
     * @param horizontalAlign The horizontal align.
     */
    public void setHorizontalAlign(EHorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    /**
     * Get the position of the display element.
     *
     * @return Returns the position.
     */
    public Vector2f getPosition() {
        return position;
    }

    /**
     * Set the position of the display element.
     *
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

        layout();
    }

    /**
     * Get the unit of the position in y direction, which can be <i>PIXEL</i> or <i>PERCENTAGE</i>
     *
     * @return Returns the unit.
     */
    public EUnitType getUnitPositionX() {
        return unitPositionX;
    }

    /**
     * Get the unit of the position in y direction, which can be <i>PIXEL</i> or <i>PERCENTAGE</i>
     *
     * @return Returns the unit.
     */
    public EUnitType getUnitPositionY() {
        return unitPositionY;
    }

    /**
     * Get the size of the display element.
     *
     * @return Returns the size of the display element.
     */
    public Vector2f getSize() {
        return size;
    }

    /**
     * Set the size of the display element.
     *
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
     *
     * @param width  The width to set including the unit.
     * @param height The height to set including the unit.
     */
    public void setSize(String width, String height) {
        String normalisedWidth = width.replace(" ", "").toLowerCase();
        String normalisedHeight = height.replace(" ", "").toLowerCase();

        float widthValue = sizeOriginal.x;
        float heightValue = sizeOriginal.x;

        try {
            if (normalisedWidth.matches("^\\d+(\\.\\d+)?%$")) {
                widthValue = Float.valueOf(normalisedWidth.substring(0, normalisedWidth.length() - 1));
                unitSizeX = EUnitType.PERCENTAGE;
            } else if (normalisedWidth.matches("^\\d+(\\.\\d+)?px$")) {
                widthValue = Float.valueOf(normalisedWidth.substring(0, normalisedWidth.length() - 2));
                unitSizeX = EUnitType.PIXEL;
            } else if (normalisedWidth.matches("^\\d+(\\.\\d+)?$")) {
                widthValue = Float.valueOf(normalisedWidth);
                unitSizeX = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid number for setSize: {}", normalisedWidth, e);
        }

        try {
            if (normalisedHeight.matches("^\\d+(\\.\\d+)?%$")) {
                heightValue = Float.valueOf(normalisedHeight.substring(0, normalisedHeight.length() - 1));
                unitSizeY = EUnitType.PERCENTAGE;
            } else if (normalisedHeight.matches("^\\d+(\\.\\d+)?px$")) {
                heightValue = Float.valueOf(normalisedHeight.substring(0, normalisedHeight.length() - 2));
                unitSizeY = EUnitType.PIXEL;
            } else if (normalisedHeight.matches("^\\d+(\\.\\d+)?$")) {
                heightValue = Float.valueOf(normalisedHeight);
                unitSizeY = EUnitType.PIXEL;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid number for setSize: {}", normalisedHeight, e);
        }

        this.size.set(widthValue, heightValue);
        this.sizeOriginal.set(widthValue, heightValue);

        layout();
    }

    /**
     * Get the unit of the size in x direction, which can be <i>PIXEL</i> or <i>PERCENTAGE</i>
     *
     * @return Returns the unit.
     */
    public EUnitType getUnitSizeX() {
        return unitSizeX;
    }

    /**
     * Get the unit of the size in y direction, which can be <i>PIXEL</i> or <i>PERCENTAGE</i>
     *
     * @return Returns the unit.
     */
    public EUnitType getUnitSizeY() {
        return unitSizeY;
    }

    /**
     * Check whether the display element consumes the mouse events. On default all display elements consume the mouse events.
     * If a display element doesn't consume the mouse events, the z-ordering of will have no effect if the display element is on top of another.
     *
     * @return Returns true when the display element consumes the mouse events.
     */
    public boolean isConsumeEvents() {
        return consumeEvents;
    }

    /**
     * Check whether the display element consumes the mouse events. On default all display elements consume the mouse events.
     * If a display element doesn't consume the mouse events, the z-ordering of will have no effect if the display element is on top of another.
     *
     * @param consumeEvents True to consume the mouse events.
     */
    public void setConsumeEvents(boolean consumeEvents) {
        this.consumeEvents = consumeEvents;
    }

    /**
     * Check whether the display element is visible.
     *
     * @return Returns true if the display element is visible.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the visibility of the display element.
     *
     * @param visible True to set the element visible.
     */
    public void setVisible(boolean visible) {
        if (!isVisible && visible) {
            notifyVisibilityListeners(visible);
        } else if (isVisible && !visible) {
            notifyVisibilityListeners(visible);
        }

        this.isVisible = visible;

        if (visible) {
            layout();
        }
    }

    /**
     * Check whether the display element can be moved or resized.
     *
     * @return Returns true if the display element is fixed.
     */
    public boolean isFixed() {
        return isFixed;
    }

    /**
     * Set whether the display element can be moved or resized.
     *
     * @param fix True to fix the elements position and size.
     */
    public void setFixed(boolean fix) {
        isFixed = fix;
    }

    /**
     * Check whether the element can be cropped.
     *
     * @return Returns true if the element can be cropped.
     */
    public boolean isCrop() {
        return isCrop;
    }

    /**
     * Set if the the element can be cropped. As default all elements can be cropped.
     *
     * @param crop True to allow cropping.
     */
    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    /**
     * Get the window where the display element belongs to.
     *
     * @return Returns the window where the display element belongs to. If the element itself is an instance of UIWindow, a null reference will be returned.
     */
    public UIWindow getWindow() {
        return window;
    }

    /**
     * Set the window where the display element belongs to. Will be set in setParent.
     *
     * @param window The window.
     */
    protected void setWindow(UIWindow window) {
        this.window = window;
    }

    /**
     * Get the parent of the display element.
     *
     * @return Returns the parent of the display element or null if no parent was assigned.
     */
    public UIDisplayElement getParent() {
        return parent;
    }

    /**
     * Set the parent of the display element.
     *
     * @param value The parent to set.
     */
    protected void setParent(UIDisplayElement value) {
        this.parent = value;
        if (value != null) {
            setWindow(value.getWindow());
        } else {
            setWindow(null);
        }
    }

    /**
     * Get the ID of the display element.
     *
     * @return Returns the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the ID of the display element ID.
     *
     * @param id The ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns true if the given point intersects the display element.
     *
     * @param point The point to test
     * @return True if intersecting
     */
    public boolean intersects(Vector2f point) {
        return point.x >= getAbsolutePosition().x
                && point.y >= getAbsolutePosition().y
                && point.x <= getAbsolutePosition().x + getSize().x
                && point.y <= getAbsolutePosition().y + getSize().y;
    }

    /**
     * Calculate the absolute position of the display element.
     *
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

    /**
     * Add an animation, such as the rotation, movement or opacity animation.
     * Every display element can only have one object of a specific class animation. To get an animation use the getAnimation method.
     *
     * @param newAnimation The animation to add.
     */
    public void addAnimation(Animation newAnimation) {
        Animation animation = getAnimation(newAnimation.getClass());

        newAnimation.setTarget(this);

        if (animation != null) {
            if (animation.isStarted()) {
                animation.stop();
            }
            animations.remove(animation);
        }

        animations.add(newAnimation);
    }

    /**
     * Get a specific animation which was added through the addAnimation method.
     *
     * @param animation The animation class.
     * @return Returns the animation of the given class. If no animation of this class is added a null reference will be returned.
     */
    public <T> T getAnimation(Class<T> animation) {
        for (Animation s : animations) {
            if (s.getClass() == animation) {
                return animation.cast(s);
            }
        }

        return null;
    }

    /*
       The event listeners which every display element in the UI supports
    */

    /**
     * Notify the visibility listeners which will be notified when the elements visibility will be changed.
     *
     * @param visible
     */
    protected void notifyVisibilityListeners(boolean visible) {
        //we copy the list so the listener can remove itself within the close/open method call (see UIInventoryCell). Otherwise ConcurrentModificationException.
        //TODO other solution?
        @SuppressWarnings("unchecked")
        List<VisibilityListener> listeners = Lists.newArrayList(visibilityListeners);

        for (VisibilityListener listener : listeners) {
            listener.changed(this, visible);
        }
    }

    /**
     * Add a visibility listener which will be notified when the elements visibility will be changed.
     *
     * @param listener The listener to add.
     */
    public void addVisibilityListener(VisibilityListener listener) {
        visibilityListeners.add(listener);
    }

    /**
     * Remove a visibility listener which will be notified when the elements visibility will be changed.
     *
     * @param listener The listener to remove.
     */
    public void removeVisibilityListener(VisibilityListener listener) {
        visibilityListeners.remove(listener);
    }

    /**
     * Notify the mouse button listeners which will be notified when the left, right or middle mouse buttons state changes or the mouse wheel will be moved.
     *
     * @param button
     * @param state
     * @param wheel
     * @param intersect
     * @param consumed
     */
    protected void notifyMouseButtonListeners(int button, boolean state, int wheel, boolean intersect, boolean consumed) {
        if (button == -1) {
            for (MouseButtonListener listener : mouseButtonListeners) {
                listener.wheel(this, wheel, intersect);
            }
        } else if (state) {
            if (!consumed) {
                for (MouseButtonListener listener : mouseButtonListeners) {
                    listener.down(this, button, intersect);
                }
            }
        } else {
            for (MouseButtonListener listener : mouseButtonListeners) {
                listener.up(this, button, intersect);
            }
        }
    }

    /**
     * Add a mouse button listener which will be notified when the left, right or middle mouse buttons state changes or the mouse wheel will be moved.
     *
     * @param listener The listener to add.
     */
    public void addMouseButtonListener(MouseButtonListener listener) {
        mouseButtonListeners.add(listener);
    }

    /**
     * Remove a mouse button listener which will be notified when the left, right or middle mouse buttons state changes or the mouse wheel will be moved.
     *
     * @param listener The listener to remove.
     */
    public void removeMouseButtonListener(MouseButtonListener listener) {
        mouseButtonListeners.remove(listener);
    }

    /**
     * Notify the click listeners which will be notified if the element was clicked on.
     *
     * @param value
     * @param consumed
     */
    protected void notifyClickListeners(int value, boolean consumed) {
        if (!consumed) {
            for (ClickListener listener : clickListeners) {
                listener.click(this, value);
            }
        }
    }

    /**
     * Add a click listener which will be notified if the element was clicked on.
     *
     * @param listener The listener to add.
     */
    public void addClickListener(ClickListener listener) {
        clickListeners.add(listener);
    }

    /**
     * Remove a click listener which will be notified if the element was clicked on.
     *
     * @param listener The listener to remove.
     */
    public void removeClickListener(ClickListener listener) {
        clickListeners.remove(listener);
    }

    /**
     * Notify the double click listeners which will be notified if the element was double clicked on.
     *
     * @param button
     */
    protected void notifyDoubleClickListeners(int button) {
        for (int i = 0; i < doubleClickListeners.size(); i++) {
            doubleClickListeners.get(i).click(this, button);
        }
    }

    /**
     * Add a double click listener which will be notified if the element was double clicked on.
     *
     * @param listener The listener to add.
     */
    public void addDoubleClickListener(ClickListener listener) {
        doubleClickListeners.add(listener);
    }

    /**
     * Remove a double click listener which will be notified if the element was double clicked on.
     *
     * @param listener The listener to remove.
     */
    public void removeDoubleClickListener(ClickListener listener) {
        doubleClickListeners.remove(listener);
    }

    /**
     * Notify the key listeners which will be notified with raw keyboard input data when the state of a key changes.
     *
     * @param event
     */
    protected void notifyKeyListeners(KeyEvent event) {
        for (KeyListener listener : keyListeners) {
            listener.key(this, event);
        }
    }

    /**
     * Add a key listener which will be notified with raw keyboard input data when the state of a key changes.
     *
     * @param listener The listener to add.
     */
    public void addKeyListener(KeyListener listener) {
        keyListeners.add(listener);
    }

    /**
     * Remove a key listener which will be notified with raw keyboard input data when the state of a key changes.
     *
     * @param listener The listener to remove.
     */
    public void removeKeyListener(KeyListener listener) {
        keyListeners.remove(listener);
    }

    /**
     * Notify the bind key listeners which will be notified if a special key was pressed, which is bind to a specific action.
     *
     * @param event
     */
    protected void notifyBindKeyListeners(BindButtonEvent event) {
        for (BindKeyListener listener : bindKeyListeners) {
            listener.key(this, event);
        }
    }

    /**
     * Add a bind key listener which will be notified if a special key was pressed, which is bind to a specific action.
     *
     * @param listener The listener to add.
     */
    public void addBindKeyListener(BindKeyListener listener) {
        bindKeyListeners.add(listener);
    }

    /**
     * Remove a bind key listener which will be notified if a special key was pressed, which is bind to a specific action.
     *
     * @param listener The listener to remove.
     */
    public void removeBindKeyListener(BindKeyListener listener) {
        bindKeyListeners.remove(listener);
    }

    /**
     * Notify the focus listeners which will be notified if the element gains or loses the focus.
     *
     * @param focus
     */
    protected void notifyFocusListeners(boolean focus) {
        if (focus) {
            for (FocusListener listener : focusListeners) {
                listener.focusOn(this);
            }
        } else {
            for (FocusListener listener : focusListeners) {
                listener.focusOff(this);
            }
        }
    }

    /**
     * Add a focus listener which will be notified if the element gains or loses the focus.
     *
     * @param listener The listener to add.
     */
    public void addFocusListener(FocusListener listener) {
        focusListeners.add(listener);
    }

    /**
     * Remove a focus listener which will be notified if the element gains or loses the focus.
     *
     * @param listener The listener to remove.
     */
    public void removeFocusListener(FocusListener listener) {
        focusListeners.remove(listener);
    }

    /**
     * Notify the mouse move listeners which will be notified on raw mouse move actions, as the mouse enters or leaves the element or the mouse is over the element.
     *
     * @param type
     * @param consumed
     */
    protected void notifyMouseMoveListeners(EMouseEvents type, boolean consumed) {
        switch (type) {
            case ENTER:
                if (!consumed) {
                    for (MouseMoveListener listener : mouseMoveListeners) {
                        listener.enter(this);
                    }
                }
                break;
            case LEAVE:
                for (MouseMoveListener listener : mouseMoveListeners) {
                    listener.leave(this);
                }
                break;
            case HOVER:
                if (!consumed) {
                    for (MouseMoveListener listener : mouseMoveListeners) {
                        listener.hover(this);
                    }
                }
                break;
            case MOVE:
                for (MouseMoveListener listener : mouseMoveListeners) {
                    listener.move(this);
                }
                break;
        }
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    /**
     * Add a mouse move listener which will be notified on raw mouse move actions, as the mouse enters or leaves the element or the mouse is over the element.
     *
     * @param listener The listener to add.
     */
    public void addMouseMoveListener(MouseMoveListener listener) {
        mouseMoveListeners.add(listener);
    }

    /**
     * Remove a mouse move listener which will be notified on raw mouse move actions, as the mouse enters or leaves the element or the mouse is over the element.
     *
     * @param listener The listener to remove.
     */
    public void removeMouseMoveListener(MouseMoveListener listener) {
        mouseMoveListeners.remove(listener);
    }

    /**
     * Add a animation listener to a specific animation which will be notified when the animation start, stops or repeats.
     *
     * @param animation The class of the animation.
     * @param listener  The listener to add.
     */
    public <T extends Animation> void addAnimationListener(Class<T> animation, AnimationListener listener) {
        Animation animationClass = getAnimation(animation);
        if (animationClass != null) {
            animationClass.addAnimationListener(listener);
        }
    }

    /**
     * Remove a animation listener of a specific animation.
     *
     * @param animation The class of the animation.
     * @param listener  The listener to remove.
     */
    public <T extends Animation> void removeAnimationListener(Class<T> animation, AnimationListener listener) {
        Animation animationClass = getAnimation(animation);
        if (animationClass != null) {
            animationClass.removeAnimationListener(listener);
        }
    }

    public static final GUIManager getGUIManager() {
        return CoreRegistry.get(GUIManager.class);
    }

}
