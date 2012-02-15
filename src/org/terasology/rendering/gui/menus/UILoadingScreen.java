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
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;

import javax.vecmath.Vector2f;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UILoadingScreen extends UIDisplayRenderer {

    final UIImageOverlay _overlay;
    final UIText _status;

    public UILoadingScreen() {
        _status = new UIText("Loading...");
        _status.setVisible(true);

        _overlay = new UIImageOverlay("loadingBackground");
        _overlay.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_status);

        update();
    }

    @Override
    public void update() {
        super.update();

        _status.setPosition(new Vector2f(_status.calcCenterPosition().x, Display.getHeight() - 64.0f));
    }

    public void updateStatus(String string) {
        _status.setText(string);
    }
}
