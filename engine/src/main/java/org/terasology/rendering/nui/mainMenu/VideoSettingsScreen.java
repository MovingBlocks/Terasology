/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.mainMenu;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.systems.In;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class VideoSettingsScreen extends UIScreen {
    @In
    private NUIManager nuiManager;

    public VideoSettingsScreen() {
        ColumnLayout grid = new ColumnLayout();
        grid.setColumns(4);
        grid.addWidget(new UILabel("Graphics Quality:"));
        grid.addWidget(new UIButton("toggleQuality", "Nice"));
        grid.addWidget(new UILabel("Environment Effects:"));
        grid.addWidget(new UIButton("environmentEffects", "Off"));
        grid.addWidget(new UILabel("Viewing Distance:"));
        grid.addWidget(new UIButton("viewDistance", "Near"));
        grid.addWidget(new UILabel("Reflections:"));
        grid.addWidget(new UIButton("reflections", "Local Reflections (SSR)"));
        grid.addWidget(new UILabel("FOV:"));
        grid.addWidget(new UIButton("fov", "This is a slider"));
        grid.addWidget(new UILabel("Blur Intensity:"));
        grid.addWidget(new UIButton("blur", "Normal"));
        grid.addWidget(new UILabel("Bobbing:"));
        grid.addWidget(new UIButton("bobbing", "On"));
        grid.addWidget(new UILabel("Fullscreen:"));
        grid.addWidget(new UIButton("fullscreen", "Off"));
        grid.addWidget(new UILabel("Dynamic Shadows:"));
        grid.addWidget(new UIButton("shadows", "Off"));
        grid.addWidget(new UILabel("Outline:"));
        grid.addWidget(new UIButton("outline", "on"));
        grid.addWidget(new UILabel("VSync:"));
        grid.addWidget(new UIButton("vsync", "Off"));
        grid.setPadding(new Border(0, 0, 4, 4));
        grid.setFamily("option-grid");

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("Pre Alpha"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(560, 192), new Vector2f(0.5f, 0.6f));
        layout.addFixedWidget(new UIButton("close", "Back"), new Vector2i(280, 32), new Vector2f(0.5f, 0.95f));

        setContents(layout);
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
            }
        });
    }
}
