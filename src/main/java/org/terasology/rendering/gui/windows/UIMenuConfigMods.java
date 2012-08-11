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

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigMods extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _minionsButton,
            _minionOptionsButton,
            _backToConfigMenuButton;

    public UIMenuConfigMods() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _minionsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _minionsButton.getLabel().setText("Minions enabled : false");
        _minionsButton.setVisible(true);

        _minionOptionsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _minionOptionsButton.getLabel().setText("Minion Options...");
        _minionOptionsButton.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);

        addDisplayElement(_minionsButton, "minionsButton");
        addDisplayElement(_minionOptionsButton, "minionOptionsButton");
        addDisplayElement(_backToConfigMenuButton, "backToConfigMenuButton");

        layout();
    }

    @Override
    public void layout() {
        super.layout();

        if (_minionsButton != null) {
	        _minionsButton.centerHorizontally();
	        _minionsButton.getPosition().y = 300f;
	
	        _minionOptionsButton.centerHorizontally();
	        _minionOptionsButton.getPosition().y = 300f + 40f;
	
	        _backToConfigMenuButton.centerHorizontally();
	        _backToConfigMenuButton.getPosition().y = 300f + 7 * 40f;
	
	        _title.centerHorizontally();
	        _title.getPosition().y = 128f;
        }
    }
}
