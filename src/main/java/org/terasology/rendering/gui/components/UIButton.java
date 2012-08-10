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
package org.terasology.rendering.gui.components;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

import javax.vecmath.Vector2f;

/**
 * A simple graphical button usable for creating user interface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIButton extends UIDisplayContainer {

    private final UIText _label;
    
	public enum eButtonType {NORMAL, TOGGLE};
    private boolean _toggleState = false;
    private eButtonType _buttonType;

    /**
     * Create a simple button, where 2 types are possible. The normal button and the toggle button.
     * @param size The size of the button.
     * @param buttonType The type of the button which can be normal or toggle.
     */
    public UIButton(Vector2f size, eButtonType buttonType) {
        setSize(size);
        
        _buttonType = buttonType;
        
        //TODO give user of the UIButton component more styling options. deliver these styles over the constructor?
        setClassStyle("button", "background-image: engine:gui_menu 256/512 30/512 0 0");
        setClassStyle("button-mouseover", "background-image: engine:gui_menu 256/512 30/512 0 30/512");
        setClassStyle("button-mouseclick", "background-image: engine:gui_menu 256/512 30/512 0 60/512");
        setClassStyle("button");
        
        addMouseMoveListener(new MouseMoveListener() {	
			@Override
			public void leave(UIDisplayElement element) {
				if (_buttonType == eButtonType.TOGGLE) {
					if (_toggleState)
						setClassStyle("button-mouseclick");
					else
						setClassStyle("button");
				}
				else
					setClassStyle("button");
			}
			
			@Override
			public void hover(UIDisplayElement element) {

			}
			
			@Override
			public void enter(UIDisplayElement element) {
	            AudioManager.play(new AssetUri(AssetType.SOUND, "engine:click"), 1.0f);
	            if (_buttonType == eButtonType.NORMAL)
	            	setClassStyle("button-mouseover");
			}

			@Override
			public void move(UIDisplayElement element) {

			}
		});
        
        addMouseButtonListener(new MouseButtonListener() {					
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				if (_buttonType == eButtonType.NORMAL)
					setClassStyle("button");
			}
			
			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
				if (intersect) {
					if (_buttonType == eButtonType.TOGGLE) {
						if (_toggleState) {
							setClassStyle("button");
							_toggleState = false;
						}
						else {
							setClassStyle("button-mouseclick");
							_toggleState = true;
						}
					}
					else
						setClassStyle("button-mouseclick");
				}
			}
			
			@Override
			public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

			}
		});
        
        _label = new UIText("Untitled");
        _label.setVisible(true);
        _label.addChangedListener(new ChangedListener() {
			@Override
			public void changed(UIDisplayElement element) {
				layout();
			}
		});
        
        addDisplayElement(_label);
    }

    @Override
    public void layout() {
    	super.layout();
    	
    	if (_label != null) {
    		_label.setPosition(new Vector2f(getSize().x / 2 - getLabel().getTextWidth() / 2, getSize().y / 2 - getLabel().getTextHeight() / 2));
    	}
    }

    public UIText getLabel() {
        return _label;
    }
    
    public boolean getToggleState() {
		return _toggleState;
	}
    
    /**
     * Set the state of the toggle button. Only has an affect if the button was created as an toggle button.
     * @param state True to set the pressed state.
     */
    public void setToggleState(boolean state) {
    	_toggleState = state;
    	
		if (_toggleState)
			setClassStyle("button-mouseclick");
		else
			setClassStyle("button");
    }
}
