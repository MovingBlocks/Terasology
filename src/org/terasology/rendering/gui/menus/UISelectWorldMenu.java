/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.components.*;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.game.Terasology;

import javax.vecmath.Vector2f;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @todo Add scrollbar, slider
 */
public class UISelectWorldMenu extends UIDisplayRenderer {

    final UIImageOverlay _overlay;
    final UIList _list;
    final UIButton _goToBack;
    final UIButton _addToList;
    final UIButton _deleteFromList;
    final UIInput  _input;

    public UISelectWorldMenu() {
        _overlay = new UIImageOverlay("menuBackground");
        _overlay.setVisible(false);

        _list = new UIList(new Vector2f(512f, 256f));
        _list.setVisible(true);

        _input = new UIInput(new Vector2f(256f, 30f));
        _input.setVisible(true);

        Object[] worlds = Terasology.getInstance().getListWolds();

        for(Object worldName: worlds){
            _list.addItem(worldName.toString(), worldName.toString());
        }


        _goToBack = new UIButton(new Vector2f(256f, 32f));
        _goToBack.getLabel().setText("Go to back");
        _goToBack.setVisible(true);

        _addToList = new UIButton(new Vector2f(256f, 32f));
        _addToList.getLabel().setText("Add element");
        _addToList.setVisible(true);

        _deleteFromList = new UIButton(new Vector2f(256f, 32f));
        _deleteFromList.getLabel().setText("Delete selected element");
        _deleteFromList.setVisible(true);

        /*_goToBack.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                Terasology.getInstance().getGameMode().deactivateScreen("select_world");
                Terasology.getInstance().getGameMode().activateScreen("main_menu");
            }
        });   */

        _addToList.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                if(_input.getValue().length()>0){
                    _list.addItem(_input.getValue(), _input.getValue());
                }else{
                    _list.addItem("Add world " + _list.size(), "Add world " + _list.size());
                }

            }
        });

        _deleteFromList.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                _list.removeSelectedItem();
            }
        });

      addDisplayElement(_list);
      addDisplayElement(_goToBack);
      addDisplayElement(_addToList);
      addDisplayElement(_deleteFromList);
      addDisplayElement(_input);

      update();
    }

    @Override
    public void update() {
        super.update();
        _list.centerHorizontally();
        _list.getPosition().y = 230f;

        _input.centerHorizontally();
        _input.getPosition().x = _list.getPosition().x;
        _input.getPosition().y = _list.getPosition().y  + _list.getSize().y + 32f;

        _addToList.getPosition().x =_input.getPosition().x + _input.getSize().x + 15f;
        _addToList.getPosition().y =_input.getPosition().y;

        _deleteFromList.getPosition().x = _addToList.getPosition().x;
        _deleteFromList.getPosition().y = _addToList.getPosition().y  + _addToList.getSize().y + 32f;

        _goToBack.centerHorizontally();
        _goToBack.getPosition().y = Display.getHeight() - _goToBack.getSize().y - 32f;

    }

    public UIButton getGoToBackButton(){
        return _goToBack;
    }
}
