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
package org.terasology.rendering.gui.menus;

import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIConfigMenu extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;
    final UIText _version;

    private final UIButton _backToMainMenuButton;
    private final UIButton _videoButton;
    private final UIButton _audioButton;
    private final UIButton _controlsButton;
    private final UIButton _modsButton;

    public UIConfigMenu() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Settings");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _videoButton = new UIButton(new Vector2f(256f, 32f));
        _videoButton.getLabel().setText("Video");
        _videoButton.setVisible(true);

        _audioButton = new UIButton(new Vector2f(256f, 32f));
        _audioButton.getLabel().setText("Audio");
        _audioButton.setVisible(true);

        _controlsButton = new UIButton(new Vector2f(256f, 32f));
        _controlsButton.getLabel().setText("Controls");
        _controlsButton.setVisible(true);

        _modsButton = new UIButton(new Vector2f(256f, 32f));
        _modsButton.getLabel().setText("Mods");
        _modsButton.setVisible(true);

        _backToMainMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToMainMenuButton.getLabel().setText("Return to Main Menu");
        _backToMainMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_videoButton, "videoButton");
        addDisplayElement(_audioButton, "audioButton");
        addDisplayElement(_controlsButton, "controlsButton");
        addDisplayElement(_modsButton, "modsButton");
        addDisplayElement(_backToMainMenuButton, "backToMainMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _videoButton.centerHorizontally();
        _videoButton.getPosition().y = 300f;

        _audioButton.centerHorizontally();
        _audioButton.getPosition().y = 300f + 40f;

        _controlsButton.centerHorizontally();
        _controlsButton.getPosition().y = 300f + 2 * 40f;

        _modsButton.centerHorizontally();
        _modsButton.getPosition().y = 300f + 3 * 40f;

        _backToMainMenuButton.centerHorizontally();
        _backToMainMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
