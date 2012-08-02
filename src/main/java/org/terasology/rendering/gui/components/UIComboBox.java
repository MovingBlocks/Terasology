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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

public class UIComboBox extends UIDisplayContainer {
    private UIInput  _baseInput;
    private UIButton _baseButton;
    private UIList   _baseList;

    private boolean _opened;

    public UIComboBox(Vector2f size){
        initBaseItems(size, new Vector2f(size.x - 2, size.x + size.x/2 - 2));
    }
    
    public UIComboBox(Vector2f size, Vector2f listSize){
        initBaseItems(size, listSize);
    }

    private void initBaseItems(Vector2f size, Vector2f listSize){
        setSize(size);
        _opened = false;

        _baseInput  = new UIInput(size);
        _baseInput.setVisible(true);
        _baseInput.setDisabled(true);

        _baseButton = new UIButton(new Vector2f(18f, 18f));
        _baseButton.setVisible(true);
        _baseButton.getPosition().x = size.x   - _baseButton.getSize().x;
        _baseButton.getPosition().y = size.y/2 - _baseButton.getSize().y/2;
        _baseButton.getLabel().setText("");

        _baseButton.setClassStyle("button", "background-image: engine:gui_menu 18/512 18/512 432/512 0");
        _baseButton.setClassStyle("button-mouseover", "background-image: engine:gui_menu 18/512 18/512 432/512 0");
        _baseButton.setClassStyle("button-mouseclick", "background-image: engine:gui_menu 18/512 18/512 432/512 18/512");

        _baseList   = new UIList(listSize);

        _baseList.getPosition().y = size.y + 2;
        _baseList.setVisible(false);

        setListStyle();

        addDisplayElement(_baseInput);
        addDisplayElement(_baseButton);
        addDisplayElement(_baseList);
    }

    public void setValue(String test){
        _baseInput.setValue(test);
    }

    public void addItem(String text, Object value) {
        _baseList.addItem(text, value);
    }
    
    public UIListItem getSelectedItem(){
        if(_baseList.getSelectedItem()!=null){
            return _baseList.getSelectedItem();
        }else{
            return _baseList.getItem(0);
        }
    }

    public void update(){
        super.update();
        checkSelectedItem();
        
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        
        if(_baseList.checkClickOnScrollBar(mousePos) ||
           _baseList.getScrollBarVertival().isScrolled() ||
           _baseList.getScrollBarHorizontal().isScrolled()
          ){
            return;
        }

        if (intersects(mousePos)) {

            if (!_clickSoundPlayed) {
                AudioManager.play(new AssetUri(AssetType.SOUND, "engine:PlaceBlock"));
                _clickSoundPlayed = true;
            }

            if (_mouseUp) {
                _mouseUp = false;
            }
            
            if(_mouseDown){
                clicked();
                _mouseDown = false;
            }

        }else{
            //if(_baseList)

            if(_mouseDown && _opened){
                clicked();
            }

            _mouseDown = false;
            _clickSoundPlayed = false;
            _mouseUp = false;

        }

    }
    
    private void checkSelectedItem(){
        if(_baseList.getSelectedItem() == null){
            if(!_baseList.isEmpty()){
                _baseInput.setValue(_baseList.getItem(0).getText());
            }
        }else{
            _baseInput.setValue(_baseList.getSelectedItem().getText());
        }
    }

    private void setListStyle(){
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
    }

    public void clicked() {
        _opened = !_opened;
        _baseInput.setFocus(_opened);
        _baseList.setVisible(_opened);

        if(_opened){
            _baseButton.setClassStyle("button-mouseclick");
        }else{
            _baseButton.setClassStyle("button");
        }
    }
}
