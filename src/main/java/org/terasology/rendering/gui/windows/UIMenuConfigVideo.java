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

import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.StateButtonAction;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIStateButton;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigVideo extends UIWindow {

    final UIImage _title;

    private final UIStateButton _graphicsQualityButton;
    private final UIStateButton _viewingDistanceButton;
    private final UISlider _fovButton;
    private final UIStateButton _animateGrassButton;
    private final UIStateButton _reflectiveWaterButton;
    private final UIStateButton _blurIntensityButton;
    private final UIStateButton _bobbingButton;
    private final UIButton _backToConfigMenuButton;

    final UIText _version;
    
    private final ClickListener clickAction = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {
            UIStateButton b = (UIStateButton) element;
            
            if (button == 0)
                b.nextState();
            else if (button == 1)
                b.previousState();
        }
    };

    public UIMenuConfigVideo() {
        setId("config:video");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        _title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Video Settings");
        _version.setHorizontalAlign(EHorizontalAlign.CENTER);
        _version.setPosition(new Vector2f(0f, 230f));
        _version.setVisible(true);

        _graphicsQualityButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction graphicsQualityStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                switch (button.getState()) {
                case 0:
                    Config.getInstance().setEnablePostProcessingEffects(false);
                    Config.getInstance().setFlickeringLight(false);
                    break;
                case 1:
                    Config.getInstance().setEnablePostProcessingEffects(false);
                    Config.getInstance().setFlickeringLight(true);
                    break;
                case 2:
                    Config.getInstance().setEnablePostProcessingEffects(true);
                    Config.getInstance().setFlickeringLight(true);
                    break;
                }
                
                ShaderManager.getInstance().recompileAllShaders();
            }
        };
        _graphicsQualityButton.addState("Graphics Quality: Ugly", graphicsQualityStateAction);
        _graphicsQualityButton.addState("Graphics Quality: Nice", graphicsQualityStateAction);
        _graphicsQualityButton.addState("Graphics Quality: Epic", graphicsQualityStateAction);
        _graphicsQualityButton.addClickListener(clickAction);
        _graphicsQualityButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _graphicsQualityButton.setPosition(new Vector2f(-_graphicsQualityButton.getSize().x / 2f - 10f, 300f));
        _graphicsQualityButton.setVisible(true);

        _viewingDistanceButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction viewingDistanceStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                Config.getInstance().setViewingDistanceById(button.getState());
            }
        };
        _viewingDistanceButton.addState("Viewing Distance: Near", viewingDistanceStateAction);
        _viewingDistanceButton.addState("Viewing Distance: Moderate", viewingDistanceStateAction);
        _viewingDistanceButton.addState("Viewing Distance: Far", viewingDistanceStateAction);
        _viewingDistanceButton.addState("Viewing Distance: Ultra", viewingDistanceStateAction);
        _viewingDistanceButton.addClickListener(clickAction);
        _viewingDistanceButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _viewingDistanceButton.setPosition(new Vector2f(-_viewingDistanceButton.getSize().x / 2f - 10f, 300f + 40f));
        _viewingDistanceButton.setVisible(true);

        _fovButton = new UISlider(new Vector2f(256f, 32f), 75, 120);
        _fovButton.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider)element;
                slider.setText("FOV: " + String.valueOf(slider.getValue()));
                Config.getInstance().setFov(slider.getValue());
            }
        });
        _fovButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _fovButton.setPosition(new Vector2f(-_fovButton.getSize().x / 2f - 10f, 300f + 2* 40f));
        _fovButton.setVisible(true);
        
        _bobbingButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction bobbingStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                if (button.getState() == 0)
                    Config.getInstance().setCameraBobbing(false);
                else
                    Config.getInstance().setCameraBobbing(true);
            }
        };
        _bobbingButton.addState("Bobbing: Off", bobbingStateAction);
        _bobbingButton.addState("Bobbing: On", bobbingStateAction);
        _bobbingButton.addClickListener(clickAction);
        _bobbingButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _bobbingButton.setPosition(new Vector2f(-_bobbingButton.getSize().x / 2f - 10f, 300f + 3 * 40f));
        _bobbingButton.setVisible(true);

        _animateGrassButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction animateGrassStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                if (button.getState() == 0)
                    Config.getInstance().setAnimatedGrass(false);
                else
                    Config.getInstance().setAnimatedGrass(true);

                ShaderManager.getInstance().recompileAllShaders();
            }
        };
        _animateGrassButton.addState("Animate Grass: Off", animateGrassStateAction);
        _animateGrassButton.addState("Animate Grass: On", animateGrassStateAction);
        _animateGrassButton.addClickListener(clickAction);
        _animateGrassButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _animateGrassButton.setPosition(new Vector2f(_animateGrassButton.getSize().x / 2f, 300f));
        _animateGrassButton.setVisible(true);

        _reflectiveWaterButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction reflectiveWaterStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                if (button.getState() == 0)
                    Config.getInstance().setComplexWater(false);
                else
                    Config.getInstance().setComplexWater(true);

                ShaderManager.getInstance().recompileAllShaders();
            }
        };
        _reflectiveWaterButton.addState("Reflective water: Off", reflectiveWaterStateAction);
        _reflectiveWaterButton.addState("Reflective water: On", reflectiveWaterStateAction);
        _reflectiveWaterButton.addClickListener(clickAction);
        _reflectiveWaterButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _reflectiveWaterButton.setPosition(new Vector2f(_reflectiveWaterButton.getSize().x / 2f, 300f + 40f));
        _reflectiveWaterButton.setVisible(true);

        _blurIntensityButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction blurIntensityStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton)element;
                Config.getInstance().setBlurIntensity(button.getState());
            }
        };
        _blurIntensityButton.addState("Blur intensity: Off", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Some", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Normal", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Max",blurIntensityStateAction);
        _blurIntensityButton.addClickListener(clickAction);
        _blurIntensityButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _blurIntensityButton.setPosition(new Vector2f(_blurIntensityButton.getSize().x / 2f, 300f + 2 * 40f));
        _blurIntensityButton.setVisible(true);
        
        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        _backToConfigMenuButton.setVisible(true);
        _backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().openWindow("config");
            }
        });

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_graphicsQualityButton);
        addDisplayElement(_fovButton);
        addDisplayElement(_viewingDistanceButton);
        addDisplayElement(_animateGrassButton);
        addDisplayElement(_reflectiveWaterButton);
        addDisplayElement(_blurIntensityButton);
        addDisplayElement(_bobbingButton);
        addDisplayElement(_backToConfigMenuButton);
    }
    
    public void setup() {
        _fovButton.setValue((int)Config.getInstance().getFov());
        _viewingDistanceButton.setState(Config.getInstance().getActiveViewingDistanceId());
        _blurIntensityButton.setState(Config.getInstance().getBlurIntensity());
        
        if (Config.getInstance().isEnablePostProcessingEffects() && Config.getInstance().isFlickeringLight())
            _graphicsQualityButton.setState(2);
        else if (!Config.getInstance().isEnablePostProcessingEffects() && Config.getInstance().isFlickeringLight())
            _graphicsQualityButton.setState(1);
        else
            _graphicsQualityButton.setState(0);
        
        if (Config.getInstance().isAnimatedGrass()) {
            _animateGrassButton.setState(1);
        } else {
            _animateGrassButton.setState(0);
        }

        if (Config.getInstance().isComplexWater()) {
            _reflectiveWaterButton.setState(1);
        } else {
            _reflectiveWaterButton.setState(0);
        }

        if (Config.getInstance().isCameraBobbing()) {
            _bobbingButton.setState(1);
        } else {
            _bobbingButton.setState(0);
        }
    }
}
