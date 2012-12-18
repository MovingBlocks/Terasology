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
import org.terasology.logic.manager.Config;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigAudio extends UIWindow {

    final UIImage _title;
    final UILabel _version;
    
    private final UISlider _soundOptionSlider;
    private final UISlider _musicOptionSlider;
    private final UIButton _backToConfigMenuButton;

    public UIMenuConfigAudio() {
        setId("config:audio");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        setCloseBinds(new String[] {});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        maximize();

        _title = new UIImage(Assets.getTexture("engine:terasology"));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UILabel("Audio Settings");
        _version.setHorizontalAlign(EHorizontalAlign.CENTER);
        _version.setPosition(new Vector2f(0f, 230f));
        _version.setVisible(true);

        _soundOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        _soundOptionSlider.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider)element;
                if (slider.getValue() > 0)
                    slider.setText("Sound Volume: " + String.valueOf(slider.getValue()));
                else
                    slider.setText("Sound Volume: Off");
                
                Config.getInstance().setSoundVolume(slider.getValue());
            }
        });
        _soundOptionSlider.setHorizontalAlign(EHorizontalAlign.CENTER);
        _soundOptionSlider.setPosition(new Vector2f(0f, 300f));
        _soundOptionSlider.setVisible(true);

        _musicOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        _musicOptionSlider.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider)element;
                if (slider.getValue() > 0)
                    slider.setText("Music Volume: " + String.valueOf(slider.getValue()));
                else
                    slider.setText("Music Volume: Off");
                
                Config.getInstance().setMusicVolume(slider.getValue());
            }
        });
        _musicOptionSlider.setHorizontalAlign(EHorizontalAlign.CENTER);
        _musicOptionSlider.setPosition(new Vector2f(0f, 300f + 40f));
        _musicOptionSlider.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });
        _backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        _backToConfigMenuButton.setVisible(true);

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_soundOptionSlider);
        addDisplayElement(_musicOptionSlider);
        addDisplayElement(_backToConfigMenuButton);
        
        setup();
    }

    public void setup() {
        _soundOptionSlider.setValue(Config.getInstance().getSoundVolume());
        _musicOptionSlider.setValue(Config.getInstance().getMusicVolume());
    }
}
