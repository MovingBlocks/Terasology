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

import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.layout.GridLayout;

import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple graphical List.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 *         <p/>
 *         TODO maybe integrate the double click listener into the base class, to support double click in all display elements
 */
public class UIList extends UIDisplayContainerScrollable {

    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

    //selection
    private UIListItem selection = null;

    //options
    private boolean isDisabled = false;

    //child elements
    private final UIComposite list;

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
     *
     * @param index The index, where the item should be added.
     * @param item  The item to add.
     */
    public void addItem(int index, final UIListItem item) {
        item.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (!isDisabled) {
                    //select the item
                    select(getItem(item));
                }
            }
        });
        item.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                notifyDoubleClickListeners(button);
            }
        });
        item.setList(this);
        item.setVisible(true);

        list.addDisplayElementToPosition(index, item);

        layout();

        notifyChangedListeners();
    }

    /**
     * Add an item to the list.
     *
     * @param item The item to add.
     */
    public void addItem(UIListItem item) {
        addItem(getItemCount(), item);
    }

    /**
     * Remove an item from the list.
     *
     * @param item Reference of the item to remove.
     */
    public void removeItem(UIListItem item) {
        List<UIListItem> items = getItems();
        if (items.contains(item)) {
            int selectionIndex = -1;
            if (item == selection) {
                selectionIndex = getSelectionIndex();
            }

            list.removeDisplayElement(item);

            layout();

            notifyChangedListeners();

            if (selectionIndex != -1) {
                if (selectionIndex < getItemCount()) {
                    select(selectionIndex);
                } else if (selectionIndex > 0) {
                    select(selectionIndex - 1);
                }
            }
        }
    }

    /**
     * Remove an item at a specific location.
     *
     * @param index The index of the item to remove.
     */
    public void removeItem(int index) {
        removeItem(getItem(index));
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
     *
     * @return Returns the selected item or null if none is selected.
     */
    public UIListItem getSelection() {
        return selection;
    }

    /**
     * Get the selected item index.
     *
     * @return Returns the selected item index of -1 if none is selected.
     */
    public int getSelectionIndex() {
        return getItem(selection);
    }

    /**
     * Select a specific item in the list.
     *
     * @param item The reference of the item to select.
     */
    public void select(UIListItem item) {
        List<UIListItem> items = getItems();
        if (items.contains(item)) {
            if (item != selection && !isDisabled) {
                if (selection != null) {
                    selection.setSelected(false);
                }

                item.setSelected(true);
                selection = item;

                notifySelectionListeners();
            }
        }
    }

    /**
     * Select a specific item in the list.
     *
     * @param index The item index to select.
     */
    public void select(int index) {
        select(getItem(index));
    }

    /**
     * Get the size of the list.
     *
     * @return Returns the number of items in the list.
     */
    public int getItemCount() {
        return getItems().size();
    }

    /**
     * Get the index of the given item.
     *
     * @param item The item reference to get the index from.
     * @return Returns the item index or -1 if item is not in the list.
     */
    public int getItem(UIListItem item) {
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
     *
     * @param index The index of the item.
     * @return Returns the item at this index.
     */
    public UIListItem getItem(int index) {
        return getItems().get(index);
    }

    /**
     * Get all items in the list.
     *
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
