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

import org.lwjgl.input.Keyboard;
import org.terasology.asset.Assets;
import org.terasology.config.AudioConfig;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigAudio extends UIWindow {

    final UIImage title;
    final UILabel version;

    private final UISlider soundOptionSlider;
    private final UISlider musicOptionSlider;
    private final UIButton backToConfigMenuButton;
    private final AudioConfig config;

    public UIMenuConfigAudio() {
        config = CoreRegistry.get(Config.class).getAudio();
        setId("config:audio");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        setCloseBinds(new String[]{});
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});
        maximize();

        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 128f));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        version = new UILabel("Audio Settings");
        version.setHorizontalAlign(EHorizontalAlign.CENTER);
        version.setPosition(new Vector2f(0f, 230f));
        version.setVisible(true);

        soundOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        soundOptionSlider.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                if (slider.getValue() > 0)
                    slider.setText("Sound Volume: " + String.valueOf(slider.getValue()));
                else
                    slider.setText("Sound Volume: Off");

                config.setSoundVolume(slider.getValue() / 100f);
            }
        });
        soundOptionSlider.setHorizontalAlign(EHorizontalAlign.CENTER);
        soundOptionSlider.setPosition(new Vector2f(0f, 300f));
        soundOptionSlider.setVisible(true);

        musicOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        musicOptionSlider.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                if (slider.getValue() > 0)
                    slider.setText("Music Volume: " + String.valueOf(slider.getValue()));
                else
                    slider.setText("Music Volume: Off");

                config.setMusicVolume(slider.getValue() / 100f);
            }
        });
        musicOptionSlider.setHorizontalAlign(EHorizontalAlign.CENTER);
        musicOptionSlider.setPosition(new Vector2f(0f, 300f + 40f));
        musicOptionSlider.setVisible(true);

        backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        backToConfigMenuButton.getLabel().setText("Back");
        backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });
        backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        backToConfigMenuButton.setVisible(true);

        addDisplayElement(title);
        addDisplayElement(version);

        addDisplayElement(soundOptionSlider);
        addDisplayElement(musicOptionSlider);
        addDisplayElement(backToConfigMenuButton);

        setup();
    }

    public void setup() {
        soundOptionSlider.setValue(Math.round(config.getSoundVolume() * 100));
        musicOptionSlider.setValue(Math.round(config.getMusicVolume() * 100));
    }
}
