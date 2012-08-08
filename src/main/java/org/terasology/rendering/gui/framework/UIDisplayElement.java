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

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.FocusListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Base class for all displayable UI elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class UIDisplayElement {

	protected static UIDisplayElement _focusedElement;
	protected enum EMouseEvents {ENTER, LEAVE, HOVER, MOVE};
    private final ArrayList<MouseMoveListener> _mouseListeners = new ArrayList<MouseMoveListener>();
    private final ArrayList<MouseButtonListener> _mouseButtonListeners = new ArrayList<MouseButtonListener>();
    private final ArrayList<ClickListener> _clickListeners = new ArrayList<ClickListener>();
    private final ArrayList<FocusListener> _focusListeners = new ArrayList<FocusListener>();
    private EMouseEvents lastMouseState;
    private boolean _mouseIsDown = false;
    // TODO: Default this to true
    private boolean _visible = false;

    private final Vector2f _position = new Vector2f(0, 0);
    private final Vector2f _size = new Vector2f(1, 1);

    protected boolean _disabled = false;

    private boolean _overlay;

    private boolean _isFixed = true;
    private boolean _isCroped = true;

    private UIDisplayElement _parent;

    public UIDisplayElement() {
    }

    public UIDisplayElement(Vector2f position) {
        _position.set(position);
    }

    public UIDisplayElement(Vector2f position, Vector2f size) {
        _position.set(position);
        _size.set(size);
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

    public void processKeyboardInput(int key) {
        // Nothing to do here
    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {
    	if (_mouseListeners.size() > 0 || _mouseButtonListeners.size() > 0 || _clickListeners.size() > 0) {
    		if (intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
    			//mouse button listeners
		        if (button != -1 && state && !_mouseIsDown) {			//mouse down
		            notifyMouseButtonListeners(button, true, wheelMoved, true);
		            _mouseIsDown = true;
		        } else if (button != -1 && !state && _mouseIsDown) {	//mouse up
		        	notifyClickListeners(button);
    	    		notifyMouseButtonListeners(button, false, wheelMoved, true);
    	    		_mouseIsDown = false;
    	        }
		       
		        if (wheelMoved != 0) {
		            notifyMouseButtonListeners(-1, false, wheelMoved, false);
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
    	    		_mouseIsDown = false;
    	        }
    	        
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
    	
    	if (_mouseListeners.size() > 0) {
    		notifyMouseListeners(EMouseEvents.MOVE);
    	}
    }

    /**
     * @param id
     * @param pressed
     * @return Whether the bind was consumed
     */
    public boolean processBindButton(String id, boolean pressed) {
        return false;
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
        return _position;
    }

    public void setPosition(Vector2f position) {
        _position.set(position);
    }

    public Vector2f getSize() {
        return _size;
    }

    public void setSize(Vector2f scale) {
        _size.set(scale);
    }

    public void setVisible(boolean visible) {
        _visible = visible;
        
        if (_visible)
        	layout();
    }

    public boolean isVisible() {
        return _visible;
    }

    public UIDisplayElement getParent() {
        return _parent;
    }

    public void setParent(UIDisplayElement parent) {
        _parent = parent;
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
        if (_parent == null) {
            return getPosition();
        } else {
            return new Vector2f(_parent.calcAbsolutePosition().x + getPosition().x, _parent.calcAbsolutePosition().y + getPosition().y);
        }
    }

    public boolean isOverlay() {
        return _overlay;
    }

    public void setOverlay(boolean value) {
        _overlay = value;
    }

    public void setFixed(boolean fix) {
        _isFixed = fix;
    }

    public boolean isFixed() {
        return _isFixed;
    }

    public void setCroped(boolean setCroped) {
        _isCroped = setCroped;
    }

    public boolean isCroped() {
        return _isCroped;
    }
    
    private void notifyMouseButtonListeners(int button, boolean state, int wheel, boolean intersect) {
    	if (button == -1) {
	        for (MouseButtonListener listener : _mouseButtonListeners) {
	        	listener.wheel(this, wheel, intersect);
	        }
    	}
    	else if (state) {
	        for (MouseButtonListener listener : _mouseButtonListeners) {
	        	listener.down(this, button, intersect);
	        }
    	}
    	else {
	        for (MouseButtonListener listener : _mouseButtonListeners) {
	        	listener.up(this, button, intersect);
	        }
    	}  	
    }
    
    public void addMouseButtonListener(MouseButtonListener listener) {
    	_mouseButtonListeners.add(listener);
    }

    public void removeMouseButtonListener(MouseButtonListener listener) {
    	_mouseButtonListeners.remove(listener);
    }
    
    private void notifyClickListeners(int value) {
        for (ClickListener listener : _clickListeners) {
        	listener.click(this, value);
        }
    }
    
    public void addClickListener(ClickListener listener) {
    	_clickListeners.add(listener);
    }

    public void removeClickListener(ClickListener listener) {
    	_clickListeners.remove(listener);
    }
    
    private void notifyFocusListeners(boolean focus) {
    	if (focus) {
	        for (FocusListener listener : _focusListeners) {
	        	listener.focusOn(this);
	        }
    	}
    	else {
	        for (FocusListener listener : _focusListeners) {
	        	listener.focusOff(this);
	        }
    	}
    }
    
    public void addFocusListener(FocusListener listener) {
    	_focusListeners.add(listener);
    }

    public void removeFocusListener(FocusListener listener) {
    	_focusListeners.remove(listener);
    }
    
    private void notifyMouseListeners(EMouseEvents type) {
    	switch (type) {
    	case ENTER:
    		for (MouseMoveListener listener : _mouseListeners) {
    			listener.enter(this);
    		}
        break;
    	case LEAVE:
    		for (MouseMoveListener listener : _mouseListeners) {
    			listener.leave(this);
    		}
        break;
    	case HOVER:
    		for (MouseMoveListener listener : _mouseListeners) {
    			listener.hover(this);
    		}
        break;
    	case MOVE:
    		for (MouseMoveListener listener : _mouseListeners) {
    			listener.move(this);
    		}
        break;
    	}
    }
    
    public void addMouseMoveListener(MouseMoveListener listener) {
        _mouseListeners.add(listener);
    }

    public void removeMouseMoveListener(MouseMoveListener listener) {
    	_mouseListeners.remove(listener);
    }
}
