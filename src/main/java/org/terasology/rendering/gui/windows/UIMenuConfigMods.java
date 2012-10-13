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
package org.terasology.rendering.gui.windows;

import org.terasology.asset.AssetManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigMods extends UIWindow {

    final UIImage _title;

    private final UIButton _minionsButton,
            _minionOptionsButton,
            _backToConfigMenuButton;

    public UIMenuConfigMods() {
        setId("config:mods");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        _title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        _title.setSize(new Vector2f(512f, 128f));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);

        _minionsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _minionsButton.getLabel().setText("Minions enabled : false");
        _minionsButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _minionsButton.setPosition(new Vector2f(0f, 300f));
        _minionsButton.setVisible(true);

        _minionOptionsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _minionOptionsButton.getLabel().setText("Minion Options...");
        _minionOptionsButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _minionOptionsButton.setPosition(new Vector2f(0f, 300f + 40f));
        _minionOptionsButton.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });
        _backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        _backToConfigMenuButton.setVisible(true);
        
        addDisplayElement(_title);

        addDisplayElement(_minionsButton);
        addDisplayElement(_minionOptionsButton);
        addDisplayElement(_backToConfigMenuButton);
    }
}
