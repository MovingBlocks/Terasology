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
package com.github.begla.blockmania.gui.menus;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.gui.components.UIText;
import com.github.begla.blockmania.gui.components.UITransparentOverlay;
import com.github.begla.blockmania.gui.framework.UIDisplayRenderer;
import com.github.begla.blockmania.gui.framework.UIGraphicsElement;
import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UILoadingScreen extends UIDisplayRenderer {

    UITransparentOverlay _overlay;
    UIText _status;

    public UILoadingScreen() {
        _status = new UIText("Loading...");
        _status.setVisible(true);

        _overlay = new UITransparentOverlay();
        _overlay.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_status);
    }

    @Override
    public void update() {
        super.update();

        _status.setPosition(_status.calcCenterPosition());
    }

    public void updateStatus(String string) {
        _status.setText(string);
    }
}
