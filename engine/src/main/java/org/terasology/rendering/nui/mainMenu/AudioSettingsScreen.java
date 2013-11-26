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
import org.terasology.config.Config;
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
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class AudioSettingsScreen extends UIScreen {

    @In
    private NUIManager nuiManager;

    @In
    private Config config;

    public void initialise() {
        ColumnLayout grid = new ColumnLayout();
        grid.setColumns(2);
        grid.addWidget(new UILabel("Sound Volume:"));
        grid.addWidget(new UISlider("sound"));
        grid.addWidget(new UILabel("Music Volume:"));
        grid.addWidget(new UISlider("music"));
        grid.setPadding(new Border(4, 4, 4, 4));
        grid.setFamily("option-grid");

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("title", "title", "Audio Settings"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(500, 192), new Vector2f(0.45f, 0.6f));
        layout.addFixedWidget(new UIButton("close", "Back"), new Vector2i(280, 32), new Vector2f(0.5f, 0.95f));

        setContents(layout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setContents(UIWidget contents) {
        super.setContents(contents);

        UISlider sound = find("sound", UISlider.class);
        sound.setIncrement(0.05f);
        sound.setPrecision(2);
        sound.setMinimum(0);
        sound.setRange(1.0f);
        sound.bindValue(BindHelper.bindBeanProperty("soundVolume", config.getAudio(), Float.TYPE));

        UISlider music = find("music", UISlider.class);
        music.setIncrement(0.05f);
        music.setPrecision(2);
        music.setMinimum(0);
        music.setRange(1.0f);
        music.bindValue(BindHelper.bindBeanProperty("musicVolume", config.getAudio(), Float.TYPE));

        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
            }
        });
    }
}
