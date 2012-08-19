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

import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

/**
 * A combo box.
 *
 */
public class UIComboBox extends UIDisplayContainer {
    private UIInput  baseInput;
    private UIButton baseButton;
    private UIList   baseList;

    private boolean opened;

    /**
     * Creates a combo box with the given size.
     * @param size
     */
    public UIComboBox(Vector2f size){
        initBaseItems(size, new Vector2f(size.x - 2, size.x + size.x/2 - 2));
    }
    
    /**
     * Creates a combo box with the given size for the combo box size and the list size.
     * @param size The size of the combo box (without the list).
     * @param listSize The size of the list.
     */
    public UIComboBox(Vector2f size, Vector2f listSize){
        initBaseItems(size, listSize);
    }

    private void initBaseItems(Vector2f size, Vector2f listSize){
        setSize(size);
        opened = false;
        
        addMouseButtonListener(new MouseButtonListener() {
			@Override
			public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

			}
			
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				if (intersect) {
					opened = !opened;
				}
				else if (!baseList.intersects(new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY()))) {
					opened = false;
				}
				
		        baseList.setVisible(opened);
				baseButton.setToggleState(opened);
			}
			
			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
	
			}
		});
        addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void leave(UIDisplayElement element) {

			}
			
			@Override
			public void hover(UIDisplayElement element) {

			}
			
			@Override
			public void enter(UIDisplayElement element) {
				AudioManager.play(new AssetUri(AssetType.SOUND, "engine:PlaceBlock"));
			}

			@Override
			public void move(UIDisplayElement element) {

			}
		});

        baseInput = new UIInput(size);
        baseInput.setVisible(true);
        baseInput.setDisabled(true);

        baseButton = new UIButton(new Vector2f(18f, 20f), UIButton.eButtonType.TOGGLE);
        baseButton.setVisible(true);
        baseButton.getPosition().x = size.x   - baseButton.getSize().x;
        baseButton.getPosition().y = size.y/2 - baseButton.getSize().y/2;
        baseButton.getLabel().setText("");
        baseButton.setTexture("engine:gui_menu");
        baseButton.setNormalState(new Vector2f(432f, 0f), new Vector2f(18f, 18f));
        baseButton.setPressedState(new Vector2f(432f, 18f), new Vector2f(18f, 18f));

        baseList = new UIList(listSize);
        baseList.setPosition(new Vector2f(1f, size.y - 1f));
        baseList.setBorderSolid(1f, 0, 0, 0, 1.0f);
        baseList.setBackgroundColor(0xFF, 0xFF, 0xFF, 1.0f);
        baseList.setVisible(false);
        baseList.addClickListener(new ClickListener() {	
			@Override
			public void click(UIDisplayElement element, int button) {
				opened = !opened;
				baseList.setVisible(opened);
				baseButton.setToggleState(false);
			}
		});
        baseList.addChangedListener(new ChangedListener() {	
			@Override
			public void changed(UIDisplayElement element) {
				if (baseList.getSelectedItem() != null)
					baseInput.setValue(baseList.getSelectedItem().getText());
			}
		});

        /*
        _baseList.setClassStyle("windowSkin", "border-image-top: engine:gui_menu 159/512 1/512 263/512 17/512 2");
        _baseList.setClassStyle("windowSkin", "border-image-right: engine:gui_menu 1/512 63/512 423/512 17/512 2");
        _baseList.setClassStyle("windowSkin", "border-image-bottom: engine:gui_menu 159/512 1/512 263/512 81/512 2");
        _baseList.setClassStyle("windowSkin", "border-image-left: engine:gui_menu 1/512 64/512 263/512 17/512 2");

        _baseList.setClassStyle("windowSkin", "border-corner-topleft: engine:gui_menu 263/512 0");
        _baseList.setClassStyle("windowSkin", "border-corner-topright: engine:gui_menu 423/512 0");
        _baseList.setClassStyle("windowSkin", "border-corner-bottomright: engine:gui_menu 423/512 81/512");
        _baseList.setClassStyle("windowSkin", "border-corner-bottomleft: engine:gui_menu 64/512 81/512");
        _baseList.setClassStyle("windowSkin", "background-image: engine:gui_menu 159/512 63/512 264/512 18/512");
        _baseList.setClassStyle("windowSkin");
        */

        addDisplayElement(baseInput);
        addDisplayElement(baseButton);
        addDisplayElement(baseList);
    }

    public void addItem(String text, Object value) {
        baseList.addItem(text, value);
    }
    
    /**
     * Select an specific item in the list.
     * @param i The item to select.
     */
    public void setSelectedItemIndex(int i) {
    	baseList.setSelectedItemIndex(i);
    }
    
    /**
     * Get the selected item in the list.
     * @return Returns the selected item.
     */
    public int getSelectedItemIndex() {
		return baseList.getSelectedItemIndex();
	}
    
    /**
     * Get the value of the selected item in the combo box list.
     * @return Returns the value of the selected item. If no data is attached to the list entry null will be returned.
     * @see UIList
     */
    public Object getValue() {
    	return baseList.getValue();
    }
}
