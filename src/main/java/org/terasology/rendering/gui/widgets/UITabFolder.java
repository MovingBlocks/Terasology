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

import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.framework.style.Style;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.layout.RowLayout;
import org.terasology.rendering.gui.layout.StackLayout;

/**
 * A tab folder which can hold multiple UITabItems.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see UITabItem
 */
public class UITabFolder extends UIDisplayContainer {
    
    //selection
    private UITabItem selection = null;
    
    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
    
    //child elements
    private final UIComposite container;
    private final UIComposite tabsBar;
    private final UIComposite tabs;
    
    public UITabFolder() {        
        container = new UIComposite();
        container.setLayout(new GridLayout(1));
        container.setSize("100%", "100%");
        container.setVisible(true);
        
        RowLayout tabsBarLayout = new RowLayout();
        tabsBarLayout.setSpacingHorizontal(5);
        
        tabsBar = new UIComposite();
        tabsBar.setLayout(tabsBarLayout);
        tabsBar.setVisible(true);
        
        StackLayout tabsLayout = new StackLayout();
        
        tabs = new UIComposite();
        tabs.setLayout(tabsLayout);
        tabs.setVisible(true);
        
        addDisplayElement(container);
        container.addDisplayElement(tabsBar);
        container.addDisplayElement(tabs);
    }
    
    @Override
    public void setSize(Vector2f size) {
        super.setSize(size);
        
        tabs.setSize(new Vector2f(getSize().x, getSize().y - tabsBar.getSize().y));
    }
    
    @Override
    public void setSize(String width, String height) {
        super.setSize(width, height);
        
        tabs.setSize(new Vector2f(getSize().x, getSize().y - tabsBar.getSize().y));
    }
    
    @Override
    protected void addStyle(Style style) {
        tabs.addStyle(style);
    }
    
    @Override
    protected void removeStyle(Style style) {
        tabs.removeStyle(style);
    }
    
    @Override
    public <T> T getStyle(Class<T> style) {
        return tabs.getStyle(style);
    }
    
    /**
     * 
     * @param index
     * @param item
     */
    public void addItem(int index, UITabItem item) {
        item.setPosition(new Vector2f(0f, 0f));
        item.setTabFolder(this);
        item.setVisible(true);
        
        tabsBar.addDisplayElement(item.getTab());
        tabs.addDisplayElement(item);
        container.applyLayout();
        
        notifyChangedListeners();
        
        if (getItemCount() == 1) {
            select(item);
        }
        
        tabs.setSize(new Vector2f(getSize().x, getSize().y - tabsBar.getSize().y));
    }
    
    /**
     * 
     * @param item
     */
    public void addItem(UITabItem item) {
        addItem(getItemCount(), item);
    }
    
    /**
     * 
     * @param item
     */
    public void removeItem(UITabItem item) {
        tabsBar.removeDisplayElement(item.getTab());
        tabs.removeDisplayElement(item);
        
        tabs.setSize(new Vector2f(getSize().x, getSize().y - tabsBar.getSize().y));
    }
    
    /**
     * 
     * @param index
     */
    public void removeItem(int index) {
        removeItem(getItem(index));
    }
    
    /**
     * 
     */
    public void removeAll() {
        tabsBar.removeAllDisplayElements();
        tabs.removeAllDisplayElements();
        
        tabs.setSize(new Vector2f(getSize().x, getSize().y - tabsBar.getSize().y));
    }
    
    /**
     * 
     * @return
     */
    public UITabItem getSelection() {
        return selection;
    }
    
    /**
     * 
     * @return
     */
    public int getSelectionIndex() {
        return getItem(selection);
    }
    
    /**
     * 
     * @param item
     */
    public void select(UITabItem item) {
        List<UITabItem> items = getItems();
        if (items.contains(item)) {
            if (item != selection) {
                ((StackLayout)tabs.getLayout()).setTop(item);
                
                if (selection != null) {
                    selection.getTab().select(false);
                }
                
                selection = item;
                selection.getTab().select(true);
                
                notifySelectionListeners();
            }
        }
    }
    
    /**
     * 
     * @param index
     */
    public void select(int index) {
        select(getItem(index));
    }
    
    /**
     * 
     * @return
     */
    public int getItemCount() {
        return getItems().size();
    }
    
    /**
     * 
     * @param item
     * @return
     */
    public int getItem(UITabItem item) {
        List<UITabItem> list = getItems();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public UITabItem getItem(int index) {
        return getItems().get(index);
    }
    
    /**
     * 
     * @return
     */
    public List<UITabItem> getItems() {
        List<UITabItem> items = new ArrayList<UITabItem>();
        for (UIDisplayElement element : tabs.getDisplayElements()) {
            if (element instanceof UITabItem) {
                items.add((UITabItem) element);
            }
        }
        
        return items;
    }
    
    /**
     * 
     * @return
     */
    public float getTabSpacing() {
        return ((RowLayout)tabsBar.getLayout()).getSpacingHorizontal();
    }
    
    /**
     * 
     * @param tabSpacing
     */
    public void setTabSpacing(float tabSpacing) {
        ((RowLayout)tabsBar.getLayout()).setSpacingHorizontal(tabSpacing);
    }
    
    /*
        Event listeners
    */
    private void notifySelectionListeners() {
        for (SelectionListener listener : selectionListeners) {
            listener.changed(this);
        }
    }
    
    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }
    
    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
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
