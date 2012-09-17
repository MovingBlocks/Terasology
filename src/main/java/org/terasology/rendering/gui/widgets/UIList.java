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

import javax.vecmath.Vector4f;

import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.layout.GridLayout;

/**
 * A simple graphical List
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIList extends UIDisplayContainerScrollable {
    
    //selection
    private UIListItem selection = null;
    
    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final List<SelectionListener> selectionChangedListeners = new ArrayList<SelectionListener>();
    private final ArrayList<ClickListener> doubleClickListeners = new ArrayList<ClickListener>();
    
    //child elements
    private final UIComposite list;
    
    //options
    private boolean isDisabled = false;
    
    public UIList() {     
        setEnableScrolling(true);
        setEnableScrollbar(true);
        
        list = new UIComposite();
        list.setSize("100%", "100%");
        list.setLayout(new GridLayout(1));
        list.setVisible(true);
        
        addDisplayElement(list);
    }
    
    @Override
    //TODO change behavior of list widget with padding
    public void setPadding(Vector4f padding) {
        
    }
    
    /**
     * Add an item to a specific location in the list.
     * @param index The index, where the item should be added.
     * @param item The item to add.
     */
    public void addItem(int index, final UIListItem item) {        
        item.addClickListener(new ClickListener() {
            private long lastTime = System.currentTimeMillis();
            private int lastButton = -1;
            
            @Override
            public void click(UIDisplayElement element, int button) {
                if (!isDisabled) {
                    //check double click
                    if ((System.currentTimeMillis() - lastTime) < 200 && lastButton == button) {
                        notifyDoubleClickListeners();
                    }
                    lastTime = System.currentTimeMillis();
                    lastButton = button;
                    
                    //select the item
                    select(getItemIndex(item));
                }
            }
        });
        item.setDisabled(isDisabled);
        item.setVisible(true);
        
        list.addDisplayElementToPosition(index, item);
        
        layout();
        
        notifyChangedListeners();
    }
    
    /**
     * Add an item to the list.
     * @param item The item to add.
     */
    public void addItem(UIListItem item) {
        addItem(getItemCount(), item);
    }
    
    /**
     * Remove an item at a specific location.
     * @param index The index of the item to remove.
     */
    public void removeItem(int index) {
        list.removeDisplayElement(getItems().get(index));
        
        //select next item
        if (index < getItemCount()) {
            select(index);
        } else if (index > 0) {
            select(index - 1);
        }
        
        layout();
        
        notifyChangedListeners();
    }
    
    /**
     * Remove all items.
     */
    public void removeAll() {
        list.removeAllDisplayElements();
        
        layout();
        
        notifyChangedListeners();
    }
    
    /**
     * Get the selected item.
     * @return Returns the selected item or null if none is selected.
     */
    public UIListItem getSelection() {
        return selection;
    }
    
    /**
     * Get the selected item index.
     * @return Returns the selected item index of -1 if none is selected.
     */
    public int getSelectionIndex() {
        return getItemIndex(selection);
    }
    
    /**
     * Select a specific item in the list.
     * @param index The item index to select.
     */
    public void select(int index) {
        if (index != getItemIndex(selection) && !isDisabled) {
            if (selection != null) {
                selection.setSelected(false);
            }
            
            getItem(index).setSelected(true);
            selection = getItem(index);
            
            notifySelectionChangedListeners();
        }
    }
    
    /**
     * Get the size of the list.
     * @return Returns the number of items in the list.
     */
    public int getItemCount() {
        return getItems().size();
    }
    
    /**
     * Get the index of the given item.
     * @param item The item reference to get the index from.
     * @return Returns the item index or -1 if item is not in the list.
     */
    public int getItemIndex(UIListItem item) {
        List<UIListItem> list = getItems();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Get an item at a specific location.
     * @param index The index of the item.
     * @return Returns the item at this index.
     */
    public UIListItem getItem(int index) {
        return getItems().get(index);
    }
    
    /**
     * Get all items in the list.
     * @return Returns the list of all items.
     */
    public List<UIListItem> getItems() {
        List<UIListItem> items = new ArrayList<UIListItem>();
        for (UIDisplayElement element : list.getDisplayElements()) {
            if (element instanceof UIListItem) {
                items.add((UIListItem) element);
            }
        }
        
        return items;
    }
    
    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        this.isDisabled = disabled;
        
        for (UIListItem item : getItems()) {
            item.setDisabled(disabled);
        }
    }
    
    /*
       Event listeners
    */
    
    private void notifyDoubleClickListeners() {
        for (int i = 0; i < doubleClickListeners.size(); i++) {
            doubleClickListeners.get(i).click(this, 0);
        }
    }
     
    public void addDoubleClickListener(ClickListener listener) {
        doubleClickListeners.add(listener);
    }
    
    public void removeDoubleClickListener(ClickListener listener) {
        doubleClickListeners.remove(listener);
    }
     
    private void notifySelectionChangedListeners() {
        for (SelectionListener listener : selectionChangedListeners) {
            listener.changed(this);
        }
    }
     
    public void addSelectionChangedListener(SelectionListener listener) {
        selectionChangedListeners.add(listener);
    }
    
    public void removeSelectionChangedListener(SelectionListener listener) {
        selectionChangedListeners.remove(listener);
    }
    
    private void notifyChangedListeners() {
        for (ChangedListener listener : changedListeners) {
            listener.changed(this);
        }
    }

    public void addChangedListener(ChangedListener listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(ChangedListener listener) {
        changedListeners.remove(listener);
    }
}
