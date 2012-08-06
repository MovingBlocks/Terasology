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

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIProgressBar;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UILoadingScreen extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIProgressBar _progressBar;

    public UILoadingScreen() {
        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _progressBar = new UIProgressBar(new Vector2f(256f, 15f));
        _progressBar.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_progressBar);

        layout();
        setVisible(true);
    }

    @Override
    public void layout() {
        super.layout();

        if (_progressBar != null) {
        	_progressBar.setPosition(new Vector2f(_progressBar.calcCenterPosition().x, Display.getHeight() - 84.0f));
        }
    }

    public void updateStatus(String string, float percent) {
        _progressBar.setValue((int)percent);
        _progressBar.setText(string);
    }
}
