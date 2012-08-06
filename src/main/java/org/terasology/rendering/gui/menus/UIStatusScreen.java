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

import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UITransparentOverlay;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIStatusScreen extends UIDisplayWindow {

    final UITransparentOverlay _overlay;
    final UIText _status;

    public UIStatusScreen() {
        _status = new UIText("Loading...");
        _status.setVisible(true);

        _overlay = new UITransparentOverlay();
        _overlay.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_status);

        layout();
    }

    @Override
    public void layout() {
        super.layout();

        if (_status != null) {
        	_status.setPosition(_status.calcCenterPosition());
        }
    }

    public void updateStatus(String string) {
        _status.setText(string);
    }
}
