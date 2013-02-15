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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 * A combo box.
 */
public class UIComboBox extends UIDisplayContainer {

    //events
    private final ArrayList<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
    private final ArrayList<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

    private UIText baseInput;
    private UIButton baseButton;
    private UIList baseList;

    private boolean opened;

    /**
     * Creates a combo box with the given size.
     *
     * @param size
     */
    public UIComboBox(Vector2f size) {
        initBaseItems(size, new Vector2f(size.x - 2, size.x + size.x / 2 - 2));
    }

    /**
     * Creates a combo box with the given size for the combo box size and the list size.
     *
     * @param size     The size of the combo box (without the list).
     * @param listSize The size of the list.
     */
    public UIComboBox(Vector2f size, Vector2f listSize) {
        initBaseItems(size, listSize);
    }

    private void initBaseItems(Vector2f size, Vector2f listSize) {
        setSize(size);
        opened = false;

        baseInput = new UIText();
        baseInput.setSize(size);
        baseInput.setVisible(true);
        baseInput.setDisabled(true);
        baseInput.addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                if (intersect) {
                    opened = !opened;
                } else if (!baseList.intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
                    opened = false;
                }

                baseList.setVisible(opened);
                baseButton.setToggleState(opened);
            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {

            }
        });

        baseInput.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {

            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:PlaceBlock"), 1.0f);
            }

            @Override
            public void move(UIDisplayElement element) {

            }
        });

        baseButton = new UIButton(new Vector2f(23f, 23f), UIButton.ButtonType.TOGGLE);
        baseButton.setVisible(true);
        baseButton.setPosition(new Vector2f(size.x - baseButton.getSize().x, size.y / 2 - baseButton.getSize().y / 2));
        baseButton.getLabel().setText("");
        baseButton.setTexture("engine:gui_menu");
        baseButton.setNormalState(new Vector2f(432f, 0f), new Vector2f(18f, 18f));
        baseButton.setPressedState(new Vector2f(432f, 18f), new Vector2f(18f, 18f));
        baseButton.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                opened = baseButton.getToggleState();
                baseList.setVisible(opened);
                setFocus(UIComboBox.this);
            }
        });

        baseList = new UIList();
        baseList.setSize(listSize);
        baseList.setPosition(new Vector2f(0f, size.y));
        baseList.setBorderSolid(new Vector4f(1f, 1f, 1f, 1f), new Color(0, 0, 0));
        baseList.setBackgroundColor(new Color(255, 255, 255));
        baseList.setVisible(false);
        baseList.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                if (baseList.getSelection() != null) {
                    baseInput.setText((baseList.getSelection()).getText());
                }
                opened = false;
                baseList.setVisible(opened);
                baseButton.setToggleState(false);

                notifySelectionListeners();
            }
        });
        baseList.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                notifyChangedListeners();
            }
        });

        addDisplayElement(baseInput);
        addDisplayElement(baseButton);
        addDisplayElement(baseList);
    }

    /**
     * Add an item to a specific location in the list.
     *
     * @param index The index, where the item should be added.
     * @param item  The item to add.
     */
    public void addItem(int index, UIListItem item) {
        baseList.addItem(index, item);
    }

    /**
     * Add an item to the list.
     *
     * @param item The item to add.
     */
    public void addItem(UIListItem item) {
        baseList.addItem(item);
    }

    /**
     * Remove an item from the list.
     *
     * @param item Reference of the item to remove.
     */
    public void removeItem(UIListItem item) {
        baseList.removeItem(item);
    }

    /**
     * Remove an item at a specific location.
     *
     * @param index The index of the item to remove.
     */
    public void removeItem(int index) {
        baseList.removeItem(index);
    }

    /**
     * Remove all items.
     */
    public void removeAll() {
        baseList.removeAll();
    }

    /**
     * Get the selected item.
     *
     * @return Returns the selected item or null if none is selected.
     */
    public UIListItem getSelection() {
        return baseList.getSelection();
    }

    /**
     * Get the selected item in the list.
     *
     * @return Returns the selected item.
     */
    public int getSelectionIndex() {
        return baseList.getSelectionIndex();
    }

    /**
     * Select a specific item in the list.
     *
     * @param item The reference of the item to select.
     */
    public void select(UIListItem item) {
        baseList.select(item);
    }

    /**
     * Select an specific item in the list.
     *
     * @param index The item to select.
     */
    public void select(int index) {
        baseList.select(index);
    }

    /**
     * Get the size of the list.
     *
     * @return Returns the number of items in the list.
     */
    public int getItemCount() {
        return baseList.getItemCount();
    }

    /**
     * Get the index of the given item.
     *
     * @param item The item reference to get the index from.
     * @return Returns the item index or -1 if item is not in the list.
     */
    public int getItem(UIListItem item) {
        return baseList.getItem(item);
    }

    /**
     * Get an item at a specific location.
     *
     * @param index The index of the item.
     * @return Returns the item at this index.
     */
    public UIListItem getItem(int index) {
        return baseList.getItem(index);
    }

    /**
     * Get all items in the list.
     *
     * @return Returns the list of all items.
     */
    public List<UIListItem> getItems() {
        return baseList.getItems();
    }

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
