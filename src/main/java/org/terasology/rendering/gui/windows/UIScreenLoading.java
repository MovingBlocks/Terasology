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
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIProgressBar;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIScreenLoading extends UIWindow {

    final UIImage background;
    final UIProgressBar _progressBar;

    public UIScreenLoading() {
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        background = new UIImage(AssetManager.loadTexture("engine:menuBackground"));
        background.setVisible(true);

        _progressBar = new UIProgressBar();
        _progressBar.setHorizontalAlign(EHorizontalAlign.CENTER);
        _progressBar.setVerticalAlign(EVerticalAlign.BOTTOM);
        _progressBar.setPosition(new Vector2f(0f, -80f));
        _progressBar.setVisible(true);

        addDisplayElement(background);
        addDisplayElement(_progressBar);
    }

    public void updateStatus(String string, float percent) {
        _progressBar.setValue((int)percent);
        _progressBar.setText(string);
    }
}
