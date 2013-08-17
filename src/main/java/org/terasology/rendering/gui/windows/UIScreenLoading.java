/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.asset.Assets;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIProgressBar;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIScreenLoading extends UIWindow {

    final UIImage background;
    final UIProgressBar progressBar;

    public UIScreenLoading() {
        setId("loading");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();

        background = new UIImage(Assets.getTexture("engine:menuBackground"));
        background.setVisible(true);

        progressBar = new UIProgressBar();
        progressBar.setSize(new Vector2f(256f, 15f));
        progressBar.setHorizontalAlign(EHorizontalAlign.CENTER);
        progressBar.setVerticalAlign(EVerticalAlign.BOTTOM);
        progressBar.setPosition(new Vector2f(0f, -80f));
        progressBar.setVisible(true);

        addDisplayElement(background);
        addDisplayElement(progressBar);
    }

    public void updateStatus(String string, float percent) {
        progressBar.setValue((int) percent);
        progressBar.setText(string);
    }
}
