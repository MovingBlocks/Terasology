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
 */
public class UIConfigMenu extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _graphicsQualityButton,
            _backToMainMenuButton,
            _viewingDistanceButton,
            _fovButton;

    final UIText _version;

    public UIConfigMenu() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Pre Alpha");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _graphicsQualityButton = new UIButton(new Vector2f(256f, 32f));
        _graphicsQualityButton.getLabel().setText("Graphics Quality: Ugly");
        _graphicsQualityButton.setVisible(true);

        _viewingDistanceButton = new UIButton(new Vector2f(256f, 32f));
        _viewingDistanceButton.getLabel().setText("Viewing Distance: Near");
        _viewingDistanceButton.setVisible(true);

        // TODO: Replace with a slider later on
        _fovButton = new UIButton(new Vector2f(256f, 32f));
        _fovButton.getLabel().setText("Field of View: 80");
        _fovButton.setVisible(true);

        _backToMainMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToMainMenuButton.getLabel().setText("Return to Main Menu");
        _backToMainMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_graphicsQualityButton, "graphicsQualityButton");
        addDisplayElement(_fovButton, "fovButton");
        addDisplayElement(_backToMainMenuButton, "backToMainMenuButton");
        addDisplayElement(_viewingDistanceButton, "viewingDistanceButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _graphicsQualityButton.centerHorizontally();
        _graphicsQualityButton.getPosition().y = 300f + 40f;

        _viewingDistanceButton.centerHorizontally();
        _viewingDistanceButton.getPosition().y = 300f + 2 * 40f;

        _fovButton.centerHorizontally();
        _fovButton.getPosition().y = 300f + 3 * 40f;

        _backToMainMenuButton.centerHorizontally();
        _backToMainMenuButton.getPosition().y = 300f + 5 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
