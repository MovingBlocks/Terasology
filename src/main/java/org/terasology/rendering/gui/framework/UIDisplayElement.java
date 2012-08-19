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

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.events.input.KeyEvent;
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
 */
public abstract class UIDisplayElement {

	protected static UIDisplayElement _focusedElement;
	
	//event stuff
	private enum EMouseEvents {ENTER, LEAVE, HOVER, MOVE};
    private final ArrayList<MouseMoveListener> mouseListeners = new ArrayList<MouseMoveListener>();
    private final ArrayList<MouseButtonListener> mouseButtonListeners = new ArrayList<MouseButtonListener>();
    private final ArrayList<ClickListener> clickListeners = new ArrayList<ClickListener>();
    private final ArrayList<FocusListener> focusListeners = new ArrayList<FocusListener>();
    private EMouseEvents lastMouseState;
    private boolean mouseIsDown = false;
    
    //layout
    private boolean visible = false;
    private boolean overlay;
    private boolean isFixed = true;
    private boolean isCroped = true;
    
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f size = new Vector2f(1, 1);

    private UIDisplayElement parent;

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
            glPushMatrix();
            glTranslatef(getPosition().x, getPosition().y, 0);
            render();
            glPopMatrix();
        }
    }
    
    public void processBindButton(BindButtonEvent event) {
    	//TODO process bind buttons
    }

    public void processKeyboardInput(KeyEvent event) {
    	//TODO process raw keyboard
    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {
    	if (!isVisible())
    		return;
    	
    	if (mouseListeners.size() > 0 || mouseButtonListeners.size() > 0 || clickListeners.size() > 0) {
    		if (intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
    			//mouse button listeners
		        if (button != -1 && state && !mouseIsDown) {			//mouse down
		            notifyMouseButtonListeners(button, true, wheelMoved, true);
		            mouseIsDown = true;
		        } else if (button != -1 && !state && mouseIsDown) {	//mouse up
		        	notifyClickListeners(button);
    	    		notifyMouseButtonListeners(button, false, wheelMoved, true);
    	    		mouseIsDown = false;
    	        }
		        
		        //mouse wheel listeners
		        if (wheelMoved != 0) {
		            notifyMouseButtonListeners(-1, false, wheelMoved, true);
		        }
    			
		        //mouse position listeners
    			notifyMouseListeners(EMouseEvents.HOVER);
    			
    			if (lastMouseState == EMouseEvents.LEAVE || lastMouseState == null) {
    				notifyMouseListeners(EMouseEvents.ENTER);
    				lastMouseState = EMouseEvents.ENTER;
    			}
    		}
    		else {
    			//mouse button listeners
    	        if (button != -1 && state) {			//mouse down
    	            notifyMouseButtonListeners(button, true, wheelMoved, false);
    	        } else if (button != -1 && !state) {	//mouse up
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
    }

    public boolean isFocused() {
        if (_focusedElement == this)
        	return true;
        else
        	return false;
    }
    
    public void setFocus(UIDisplayElement focus) {
        if (_focusedElement != focus) {
        	if (focus == null && _focusedElement != this)
        		return;
        	
        	if (_focusedElement != null)
        		_focusedElement.notifyFocusListeners(false);
 
        	_focusedElement = focus;
        	if (_focusedElement != null)
        		_focusedElement.notifyFocusListeners(true);
        }
    }

    /**
     * Render related actions here. Will be executed every tick.
     */
    public abstract void render();

    /**
     * Update related actions, for tasks which needs continuously updates. Will be executed every tick. Needs to be avoided.
     */
    public abstract void update();
    
    /**
     * Set the layout of the child elements here. Will be executed if the display or a parent window was resized.
     */
    public abstract void layout();

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
    	this.position.set(position);
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f scale) {
    	this.size.set(scale);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        
        if (visible)
        	layout();
    }

    public boolean isVisible() {
        return visible;
    }

    public UIDisplayElement getParent() {
        return parent;
    }

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
        return (point.x >= calcAbsolutePosition().x && point.y >= calcAbsolutePosition().y && point.x <= calcAbsolutePosition().x + getSize().x && point.y <= calcAbsolutePosition().y + getSize().y);
    }

    /**
     * Calculates the center position on the screen based on the active resolution and size of the display element.
     *
     * @return The center position
     */
    public Vector2f calcCenterPosition() {
        return new Vector2f(Display.getWidth() / 2 - getSize().x / 2, Display.getHeight() / 2 - getSize().y / 2);
    }

    public void center() {
        getPosition().set(calcCenterPosition());
    }

    public void centerVertically() {
        getPosition().y = calcCenterPosition().y;
    }

    public void centerHorizontally() {
        getPosition().x = calcCenterPosition().x;
    }

    public Vector2f calcAbsolutePosition() {
        if (parent == null) {
            return getPosition();
        } else {
            return new Vector2f(parent.calcAbsolutePosition().x + getPosition().x, parent.calcAbsolutePosition().y + getPosition().y);
        }
    }

    public boolean isOverlay() {
        return overlay;
    }

    public void setOverlay(boolean value) {
        overlay = value;
    }

    public void setFixed(boolean fix) {
        isFixed = fix;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setCroped(boolean setCroped) {
        isCroped = setCroped;
    }

    public boolean isCroped() {
        return isCroped;
    }
    
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
