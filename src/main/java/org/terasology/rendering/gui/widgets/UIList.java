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
package org.terasology.rendering.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A simple graphical List
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.3
 * 
 * TODO get rid of the IInputDataElement
 * TODO here is a bug with deleting the selected item..
 */

public class UIList extends UIDisplayContainerScrollable implements IInputDataElement {

    private UIListItem _selectedItem = null;
    private final ArrayList<ClickListener> _doubleClickListeners = new ArrayList<ClickListener>();

    //List items
    private final List<UIListItem> items = new ArrayList<UIListItem>();
    private final List<ChangedListener> _changedListeners = new ArrayList<ChangedListener>();
    
    private float listItemHeight = 32f;
    
    public class UIListItem extends UIDisplayContainer {
        private Object value;
        private String text;
        private boolean isSelected;
        private final Vector2f padding = new Vector2f(5f, 10f);

        private final UIText label;

        public UIListItem(float height, String text, Object value) {
            setSize("100%", height + "px");
            this.text = text;
            this.value = value;

            label = new UIText();
            label.setVisible(true);
            label.setColor(Color.lightGray);
            label.setPosition(new Vector2f((getPosition().x + padding.x), (getPosition().y + padding.y)));
            label.setText(text);

            if (getSize().x < label.getSize().x) {
                setSize(new Vector2f(label.getSize().x, getSize().y));
            }
            
            addMouseMoveListener(new MouseMoveListener() {        
                @Override
                public void leave(UIDisplayElement element) {
                    if(!isSelected) {
                        label.setColor(Color.lightGray);
                    }
                }
                
                @Override
                public void hover(UIDisplayElement element) {

                }
                
                @Override
                public void enter(UIDisplayElement element) {
                    if(!isSelected) {
                        label.setColor(Color.orange);
                    }
                }

                @Override
                public void move(UIDisplayElement element) {

                }
            });

            addDisplayElement(label);
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            label.setText(this.text);
            this.text = text;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;

            if (isSelected) {
                setBackgroundColor(0xE1, 0xDD, 0xD4, 1.0f);
                label.setColor(Color.orange);
            } else {
                removeBackgroundColor();
                label.setColor(Color.lightGray);
            }
        }
        
        @Override
        public void render() {
            // TODO Auto-generated method stub
            super.render();
        }
    }

    public UIList(Vector2f size) {
        super(size);
    }

    /**
     * Get the count of the elements in this list.
     * @return Returns the count of the elements in this list.
     */
    public int size() {
        return items.size();
    }

    /**
     * Add an item to the list.
     * @param text The text of the item.
     * @param value The value which the item holds.
     */
    public void addItem(String text, Object value) {

        final UIListItem newItem = new UIListItem(listItemHeight, text, value);

        newItem.setVisible(true);

        if (items.size() > 0) {
            newItem.setPosition(items.get(0).getPosition());
        }

        newItem.setPosition(new Vector2f(0f, listItemHeight * items.size()));
        newItem.addClickListener(new ClickListener() {
            private long _lastTime = System.currentTimeMillis();
            private int _lastButton = -1;
            
            @Override
            public void click(UIDisplayElement element, int button) {
                //Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
                //handle double click
                if ((System.currentTimeMillis() - _lastTime) < 200 && _lastButton == button) {
                    notifyDoubleClickListeners();
                }
                _lastTime = System.currentTimeMillis();
                _lastButton = button;
                
                //select the clicked item
                UIListItem item = (UIListItem) element;
                
                if (_selectedItem != null)
                    _selectedItem.setSelected(false);
                
                _selectedItem = item;
                _selectedItem.setSelected(true);
                
                notifyChangedListeners();
            }
        });

        //lets add the item
        items.add(newItem);
        addDisplayElement(newItem);
    }
    
    /**
     * Get an item by the items index.
     * @param index The item index.
     * @return Returns the item at the given index.
     */
    public UIListItem getItem(int index){
        if(!items.isEmpty()){
            return items.get(index);
        } else {
            return null;
        }
    }

    /**
     * Select a particular item in this list.
     * @param i The index of the item to select.
     */
    public void setSelectedItemIndex(int i) {
        if (_selectedItem != null) {
            _selectedItem.setSelected(false);
        }
        
        _selectedItem = items.get(i);
        _selectedItem.setSelected(true);
        
        notifyChangedListeners();
    }
    
    /**
     * Get the selected item.
     * @return Returns the selected item.
     */
    public UIListItem getSelectedItem() {
        return _selectedItem;
    }
    
    /**
     * Get the value of the selected item.
     * @return Returns the value of the selected item.
     */
    public Object getValue() {
        return _selectedItem.getValue();
    }
    
    /**
     * Get the index of the selected item.
     * @return Returns the index of the selected item.
     */
    public int getSelectedItemIndex() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == _selectedItem) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Remove a item in the list by the items index.
     * @param index The index of the item to remove.
     */
    public void removeItem(int index) {
        removeDisplayElement(items.get(index));
        items.remove(index);
    }
    
    /**
     * Remove the selected item.
     */
    public void removeSelectedItem() {

        if (_selectedItem == null) {
            return;
        }

        int index = getSelectedItemIndex();
        removeDisplayElement(_selectedItem);
        items.remove(_selectedItem);

        for (int i = index; i < items.size(); i++) {
            items.get(i).setPosition(new Vector2f(0f, items.get(i).getPosition().y - listItemHeight));
        }

        if (items.size() > 0) {
            if (index <= items.size() - 1) {
                setSelectedItemIndex(index);
            }
            else {
                setSelectedItemIndex(items.size() - 1);
            }

        }
        else {
            _selectedItem = null;
        }
    }

    /**
     * Remove all items in this list.
     */
    public void removeAll() {
        clearData();
        for (int i = (items.size() - 1); i >= 0; i--) {
            removeDisplayElement(items.get(i));
            items.remove(i);
        }
    }

    /**
     * Check if the list is empty.
     * @return Returns true if the list is empty.
     */
    public boolean isEmpty(){
        return items.isEmpty();
    }

    /**
     * Clear data (?)
     */
    public void clearData() {
        if (_selectedItem != null)
            _selectedItem.setSelected(false);
        _selectedItem = null;
    }
    
    /*
       Event listeners
    */

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
