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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A simple graphical List
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.2
 */

public class UIList extends UIScrollableDisplayContainer implements IInputDataElement {

    private UIListItem _selectedItem = null;
    private final ArrayList<ClickListener> _doubleClickListeners = new ArrayList<ClickListener>();

    //List items
    private final List<UIListItem> _items = new ArrayList<UIListItem>();
	private final List<ChangedListener> _changedListeners = new ArrayList<ChangedListener>();
	
	public class UIListItem extends UIDisplayContainer {
	    private Object _value;
	    private String _text;
	    private boolean _isSelected;
	    private Vector2f _padding = new Vector2f(5f, 15f);

	    private final UIText _label;

	    public UIListItem(Vector2f size, String text, Object value) {
	        setSize(size);
	        _text = text;
	        _value = value;

	        _label = new UIText();
	        _label.setVisible(true);
	        _label.setColor(Color.lightGray);
	        _label.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));
	        _label.setText(_text);

	        if (getSize().x < _label.getTextWidth()) {
	            setSize(new Vector2f(_label.getTextWidth(), getSize().y));
	        }
	        
	        addMouseMoveListener(new MouseMoveListener() {		
				@Override
				public void leave(UIDisplayElement element) {
					if(!_isSelected)
						_label.setColor(Color.lightGray);
				}
				
				@Override
				public void hover(UIDisplayElement element) {

				}
				
				@Override
				public void enter(UIDisplayElement element) {
					if(!_isSelected)
						_label.setColor(Color.orange);
				}

				@Override
				public void move(UIDisplayElement element) {

				}
			});

	        addDisplayElement(_label);

	    }

	    public Object getValue() {
	        return _value;
	    }

	    public void setValue(Object value) {
	        _value = value;
	    }

	    public String getText() {
	        return _text;
	    }

	    public void setText(String text) {
	        _label.setText(_text);
	        _text = text;
	    }

	    public boolean isSelected() {
	        return _isSelected;
	    }

	    public void setSelected(boolean selected) {
	        _isSelected = selected;

	        if (_isSelected) {
	            setStyle("background-color", "#e1ddd4 1");
	            _label.setColor(Color.orange);
	        } else {
	            setStyle("background", "none");
	            _label.setColor(Color.lightGray);
	        }
	    }
	}

    public UIList(Vector2f size) {
        setSize(size);
        setCrop(true);
        setScrollBarsPosition(getPosition(), getSize());

        //ToDo Create skin for UIList
        setClassStyle("screenSkin", "border-image-top: engine:gui_menu 159/512 18/512 264/512 0 18");
        setClassStyle("screenSkin", "border-image-right: engine:gui_menu 9/512 63/512 423/512 18/512 9");
        setClassStyle("screenSkin", "border-image-bottom: engine:gui_menu 159/512 9/512 264/512 81/512 9");
        setClassStyle("screenSkin", "border-image-left: engine:gui_menu 8/512 64/512 256/512 17/512 8");

        setClassStyle("screenSkin", "border-corner-topleft: engine:gui_menu 256/512 0");
        setClassStyle("screenSkin", "border-corner-topright: engine:gui_menu 423/512 0");
        setClassStyle("screenSkin", "border-corner-bottomright: engine:gui_menu 423/512 81/512");
        setClassStyle("screenSkin", "border-corner-bottomleft: engine:gui_menu 256/512 81/512");
        setClassStyle("screenSkin", "background-image: engine:gui_menu 159/512 63/512 264/512 18/512");

        setClassStyle("screenSkin");

        setCropMargin(new Vector4f(-15f, -15f, -15f, 0));
    }

    /*
    * Returns count of elements
    */
    public int size() {
        return _items.size();
    }

    public void addItem(String text, Object value) {

        final UIListItem newItem = new UIListItem(new Vector2f(getSize().x, (32f)), text, value);

        newItem.setVisible(true);

        if (_items.size() > 0) {
            newItem.setPosition(_items.get(0).getPosition());
        }

        newItem.getPosition().y += 32f * _items.size();
        newItem.setFixed(false);
        newItem.addClickListener(new ClickListener() {
        	private long _lastTime = System.currentTimeMillis();
        	private int _lastButton = -1;
        	
			@Override
			public void click(UIDisplayElement element, int button) {
				Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
				if (!getScrollBarHorizontal().intersects(mousePos) && !getScrollBarVertival().intersects(mousePos)) {
					//handle double click
					if ((System.currentTimeMillis() - _lastTime) < 200 && _lastButton == button) {
						notifyDoubleClickListeners();
					}
					_lastTime = System.currentTimeMillis();
					_lastButton = button;
					
					//select the clicked item
					UIListItem item = (UIListItem) element;
					
					if (item != _selectedItem) {
						if (_selectedItem != null)
							_selectedItem.setSelected(false);
						
						_selectedItem = item;
						_selectedItem.setSelected(true);
						
						notifyChangedListeners();
					}
				}
			}
		});

        _items.add(newItem);
        addDisplayElement(newItem);
    }

    /*
    * Remove selected item
    */
    public void removeSelectedItem() {

        if (_selectedItem == null) {
            return;
        }

        int index = getSelectedItemIndex();
        removeDisplayElement(_selectedItem);
        _items.remove(_selectedItem);

        for (int i = index; i < _items.size(); i++) {
            _items.get(i).getPosition().y -= 32f;
        }

        if (_items.size() > 0) {
	        if (index <= _items.size() - 1)
	        	setSelectedItemIndex(index);
	        else
	        	setSelectedItemIndex(_items.size() - 1);

        }
        else {
        	_selectedItem = null;
        }
    }

    public void setSelectedItemIndex(int i) {
    	if (_selectedItem != null)
    		_selectedItem.setSelected(false);
    	
    	_selectedItem = _items.get(i);
    	_selectedItem.setSelected(true);
    	
    	notifyChangedListeners();
    }
    
    public UIListItem getSelectedItem() {
		return _selectedItem;
    }
    
    public int getSelectedItemIndex() {
    	for (int i = 0; i < _items.size(); i++) {
			if (_items.get(i) == _selectedItem)
				return i;
		}
    	
    	return -1;
    }

	/*
     * Remove all items
     */
    public void removeAll() {
        clearData();
        for (int i = (_items.size() - 1); i >= 0; i--) {
            removeDisplayElement(_items.get(i));
            _items.remove(i);
        }
    }

    /*
    * Remove item by index
    */
    public void removeItem(int index) {
        removeDisplayElement(_items.get(index));
        _items.remove(index);
    }

    /*
    * Returns the value of the selected item
    */
    public Object getValue() {
        return _selectedItem.getValue();
    }

    public UIListItem getItem(int index){
        if(!_items.isEmpty()){
            return _items.get(index);
        } else {
            return null;
        }
    }

    public boolean isEmpty(){
        return _items.isEmpty();
    }

    /*
     * Reset to selected element
     */
    public void clearData() {
    	if (_selectedItem != null)
    		_selectedItem.setSelected(false);
    	_selectedItem = null;
    }

    private void notifyDoubleClickListeners() {
        for (int i = 0; i < _doubleClickListeners.size(); i++) {
            _doubleClickListeners.get(i).click(this, 0);
        }
    }
    
    public void addDoubleClickListener(ClickListener listener) {
        _doubleClickListeners.add(listener);
    }

    public void removeDoubleClickListener(ClickListener listener) {
        _doubleClickListeners.remove(listener);
    }
    
	private void notifyChangedListeners() {
		for (ChangedListener listener : _changedListeners) {
			listener.changed(this);
		}
	}
    
    public void addChangedListener(ChangedListener listener) {
        _changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
    	_changedListeners.remove(listener);
    }
}
