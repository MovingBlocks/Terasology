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
package com.github.begla.blockmania.rendering.gui.menus;

import com.github.begla.blockmania.rendering.gui.components.UIText;
import com.github.begla.blockmania.rendering.gui.components.UITransparentOverlay;
import com.github.begla.blockmania.rendering.gui.framework.UIDisplayRenderer;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIStatusScreen extends UIDisplayRenderer {

    final UITransparentOverlay _overlay;
    final UIText _status;

    public UIStatusScreen() {
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
