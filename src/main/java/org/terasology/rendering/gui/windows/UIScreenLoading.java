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

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIProgressBar;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIScreenLoading extends UIDisplayWindow {

    final UIGraphicsElement background;
    final UIProgressBar _progressBar;

    public UIScreenLoading() {
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        background = new UIGraphicsElement(AssetManager.loadTexture("engine:menuBackground"));
        background.setVisible(true);

        _progressBar = new UIProgressBar();
        _progressBar.setVisible(true);

        addDisplayElement(background);
        addDisplayElement(_progressBar);

        layout();

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
