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
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.StateButtonAction;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIStateButton;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.world.ViewDistance;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigVideo extends UIWindow {

    final UIImage title;

    private final UIStateButton graphicsQualityButton;
    private final UIStateButton viewingDistanceButton;
    private final UISlider fovButton;
    private final UIStateButton animateGrassButton;
    private final UIStateButton reflectiveWaterButton;
    private final UIStateButton blurIntensityButton;
    private final UIStateButton bobbingButton;
    private final UIStateButton fullscreenButton;
    private final UIStateButton outlineButton;
    private final UIStateButton shadowButton;
    private final UIStateButton volumetricFogButton;
    private final UIStateButton vSyncButton;
    private final UIButton backToConfigMenuButton;

    private final Config config = CoreRegistry.get(Config.class);

    private final UILabel version;

    private final ClickListener clickAction = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {
            UIStateButton b = (UIStateButton) element;

            if (button == 0) {
                b.nextState();
            } else if (button == 1) {
                b.previousState();
            }
        }
    };

    public UIMenuConfigVideo() {
        setId("config:video");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();

        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 128f));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        version = new UILabel("Video Settings");
        version.setHorizontalAlign(EHorizontalAlign.CENTER);
        version.setPosition(new Vector2f(0f, 230f));
        version.setVisible(true);

        graphicsQualityButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction graphicsQualityStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                switch (button.getState()) {
                    case 0:
                        config.getRendering().setFlickeringLight(true);
                        config.getRendering().setVignette(true);
                        config.getRendering().setEyeAdaptation(true);
                        config.getRendering().setFilmGrain(true);

                        config.getRendering().setBloom(false);
                        config.getRendering().setMotionBlur(false);
                        config.getRendering().setSsao(false);
                        config.getRendering().setLightShafts(false);
                        config.getRendering().setAnimateWater(false);
                        config.getRendering().setCloudShadows(false);
                        break;
                    case 1:
                        config.getRendering().setFlickeringLight(true);
                        config.getRendering().setVignette(true);
                        config.getRendering().setEyeAdaptation(true);
                        config.getRendering().setFilmGrain(true);
                        config.getRendering().setBloom(true);

                        config.getRendering().setSsao(false);
                        config.getRendering().setMotionBlur(false);
                        config.getRendering().setLightShafts(false);
                        config.getRendering().setAnimateWater(false);
                        config.getRendering().setCloudShadows(false);
                        break;
                    case 2:
                        config.getRendering().setFlickeringLight(true);
                        config.getRendering().setVignette(true);
                        config.getRendering().setEyeAdaptation(true);
                        config.getRendering().setFilmGrain(true);
                        config.getRendering().setBloom(true);
                        config.getRendering().setMotionBlur(true);
                        config.getRendering().setLightShafts(true);
                        config.getRendering().setCloudShadows(true);

                        config.getRendering().setSsao(false);
                        config.getRendering().setAnimateWater(false);
                        break;
                    case 3:
                        config.getRendering().setFlickeringLight(true);
                        config.getRendering().setVignette(true);
                        config.getRendering().setEyeAdaptation(true);
                        config.getRendering().setFilmGrain(true);
                        config.getRendering().setBloom(true);
                        config.getRendering().setMotionBlur(true);
                        config.getRendering().setSsao(true);
                        config.getRendering().setLightShafts(true);
                        config.getRendering().setAnimateWater(true);
                        config.getRendering().setCloudShadows(true);
                        break;
                }
            }
        };
        graphicsQualityButton.addState("Graphics Quality: Nice", graphicsQualityStateAction);
        graphicsQualityButton.addState("Graphics Quality: Epic", graphicsQualityStateAction);
        graphicsQualityButton.addState("Graphics Quality: Insane", graphicsQualityStateAction);
        graphicsQualityButton.addState("Graphics Quality: Uber", graphicsQualityStateAction);
        graphicsQualityButton.addClickListener(clickAction);
        graphicsQualityButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        graphicsQualityButton.setPosition(new Vector2f(-graphicsQualityButton.getSize().x / 2f - 10f, 300f));
        graphicsQualityButton.setVisible(true);

        viewingDistanceButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction viewingDistanceStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setViewDistance(ViewDistance.forIndex(button.getState()));
            }
        };
        viewingDistanceButton.addState("Viewing Distance: Near", viewingDistanceStateAction);
        viewingDistanceButton.addState("Viewing Distance: Moderate", viewingDistanceStateAction);
        viewingDistanceButton.addState("Viewing Distance: Far", viewingDistanceStateAction);
        viewingDistanceButton.addState("Viewing Distance: Ultra", viewingDistanceStateAction);
        viewingDistanceButton.addClickListener(clickAction);
        viewingDistanceButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        viewingDistanceButton.setPosition(new Vector2f(-viewingDistanceButton.getSize().x / 2f - 10f, 300f + 40f));
        viewingDistanceButton.setVisible(true);

        fovButton = new UISlider(new Vector2f(256f, 32f), 75, 130);
        fovButton.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                slider.setText("FOV: " + String.valueOf(slider.getValue()));
                config.getRendering().setFieldOfView(slider.getValue());
            }
        });
        fovButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        fovButton.setPosition(new Vector2f(-fovButton.getSize().x / 2f - 10f, 300f + 2 * 40f));
        fovButton.setVisible(true);

        bobbingButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction bobbingStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setCameraBobbing(button.getState() != 0);
            }
        };
        bobbingButton.addState("Bobbing: Off", bobbingStateAction);
        bobbingButton.addState("Bobbing: On", bobbingStateAction);
        bobbingButton.addClickListener(clickAction);
        bobbingButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        bobbingButton.setPosition(new Vector2f(-bobbingButton.getSize().x / 2f - 10f, 300f + 3 * 40f));
        bobbingButton.setVisible(true);

        animateGrassButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction animateGrassStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setAnimateGrass(button.getState() != 0);
            }
        };
        animateGrassButton.addState("Animate Grass: Off", animateGrassStateAction);
        animateGrassButton.addState("Animate Grass: On", animateGrassStateAction);
        animateGrassButton.addClickListener(clickAction);
        animateGrassButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        animateGrassButton.setPosition(new Vector2f(animateGrassButton.getSize().x / 2f, 300f));
        animateGrassButton.setVisible(true);

        reflectiveWaterButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction reflectiveWaterStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setReflectiveWater(button.getState() != 0);
            }
        };
        reflectiveWaterButton.addState("Water World Reflection: Off", reflectiveWaterStateAction);
        reflectiveWaterButton.addState("Water World Reflection: On", reflectiveWaterStateAction);
        reflectiveWaterButton.addClickListener(clickAction);
        reflectiveWaterButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        reflectiveWaterButton.setPosition(new Vector2f(reflectiveWaterButton.getSize().x / 2f, 300f + 40f));
        reflectiveWaterButton.setVisible(true);

        blurIntensityButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction blurIntensityStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setBlurIntensity(button.getState());
            }
        };
        blurIntensityButton.addState("Blur Intensity: Off", blurIntensityStateAction);
        blurIntensityButton.addState("Blur Intensity: Some", blurIntensityStateAction);
        blurIntensityButton.addState("Blur Intensity: Normal", blurIntensityStateAction);
        blurIntensityButton.addState("Blur Intensity: Max", blurIntensityStateAction);
        blurIntensityButton.addClickListener(clickAction);
        blurIntensityButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        blurIntensityButton.setPosition(new Vector2f(blurIntensityButton.getSize().x / 2f, 300f + 2 * 40f));
        blurIntensityButton.setVisible(true);

        fullscreenButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction fullscreenStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                TerasologyEngine te = (TerasologyEngine) CoreRegistry.get(GameEngine.class);

                if (button.getState() == 0) {
                    te.setFullscreen(false);
                } else {
                    te.setFullscreen(true);
                }
            }
        };
        fullscreenButton.addState("Fullscreen: Off", fullscreenStateAction);
        fullscreenButton.addState("Fullscreen: On", fullscreenStateAction);
        fullscreenButton.addClickListener(clickAction);
        fullscreenButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        fullscreenButton.setPosition(new Vector2f(fullscreenButton.getSize().x / 2f, 300f + 3 * 40f));
        fullscreenButton.setVisible(true);

        outlineButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction outlineStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setOutline(button.getState() != 0);
            }
        };
        outlineButton.addState("Outline: Off", outlineStateAction);
        outlineButton.addState("Outline: On", outlineStateAction);
        outlineButton.addClickListener(clickAction);
        outlineButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        outlineButton.setPosition(new Vector2f(outlineButton.getSize().x / 2f, 300f + 4 * 40f));
        outlineButton.setVisible(true);

        shadowButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction shadowStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                switch (button.getState()) {
                    case 0:
                        config.getRendering().setDynamicShadowsPcfFiltering(false);
                        config.getRendering().setDynamicShadows(false);
                        break;
                    case 1:
                        config.getRendering().setDynamicShadowsPcfFiltering(false);
                        config.getRendering().setDynamicShadows(true);

                        break;
                    case 2:
                        config.getRendering().setDynamicShadowsPcfFiltering(true);
                        config.getRendering().setDynamicShadows(true);
                        break;
                }
            }
        };
        shadowButton.addState("Dynamic Shadows: Off", shadowStateAction);
        shadowButton.addState("Dynamic Shadows: On", shadowStateAction);
        shadowButton.addState("Dynamic Shadows: On (PCF)", shadowStateAction);
        shadowButton.addClickListener(clickAction);
        shadowButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        shadowButton.setPosition(new Vector2f(-shadowButton.getSize().x / 2f - 10f, 300f + 4 * 40f));
        shadowButton.setVisible(true);

        volumetricFogButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction volumetricFogStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                config.getRendering().setVolumetricFog(button.getState() != 0);
            }
        };
        volumetricFogButton.addState("Volumetric Fog: Off", volumetricFogStateAction);
        volumetricFogButton.addState("Volumetric Fog: On", volumetricFogStateAction);
        volumetricFogButton.addClickListener(clickAction);
        volumetricFogButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        volumetricFogButton.setPosition(new Vector2f(-volumetricFogButton.getSize().x / 2f - 10f, 300f + 5 * 40f));
        volumetricFogButton.setVisible(true);

        vSyncButton = new UIStateButton(new Vector2f(256f, 32f));
        StateButtonAction vSyncStateAction = new StateButtonAction() {
            @Override
            public void action(UIDisplayElement element) {
                UIStateButton button = (UIStateButton) element;
                TerasologyEngine te = (TerasologyEngine) CoreRegistry.get(GameEngine.class);

                if (button.getState() == 0) {
                    te.setVSync(false);
                } else {
                    te.setVSync(true);
                }
            }
        };
        vSyncButton.addState("VSync: Off", vSyncStateAction);
        vSyncButton.addState("VSync: On", vSyncStateAction);
        vSyncButton.addClickListener(clickAction);
        vSyncButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        vSyncButton.setPosition(new Vector2f(vSyncButton.getSize().x / 2f, 300f + 5 * 40f));
        vSyncButton.setVisible(true);

        backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        backToConfigMenuButton.getLabel().setText("Back");
        backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        backToConfigMenuButton.setVisible(true);
        backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(ShaderManager.class).recompileAllShaders();

                getGUIManager().openWindow("config");
            }
        });

        addDisplayElement(title);
        addDisplayElement(version);

        addDisplayElement(graphicsQualityButton);
        addDisplayElement(fovButton);
        addDisplayElement(viewingDistanceButton);
        addDisplayElement(animateGrassButton);
        addDisplayElement(reflectiveWaterButton);
        addDisplayElement(blurIntensityButton);
        addDisplayElement(bobbingButton);
        addDisplayElement(backToConfigMenuButton);
        addDisplayElement(fullscreenButton);
        addDisplayElement(outlineButton);
        addDisplayElement(shadowButton);
        addDisplayElement(volumetricFogButton);
        addDisplayElement(vSyncButton);

        setup();
    }

    public void setup() {
        fovButton.setValue((int) config.getRendering().getFieldOfView());
        viewingDistanceButton.setState(config.getRendering().getViewDistance().getIndex());
        blurIntensityButton.setState(config.getRendering().getBlurIntensity());

        if (config.getRendering().isAnimateWater()) {
            graphicsQualityButton.setState(3);
        } else if (config.getRendering().isLightShafts()) {
            graphicsQualityButton.setState(2);
        } else if (config.getRendering().isBloom()) {
            graphicsQualityButton.setState(1);
        } else {
            graphicsQualityButton.setState(0);
        }

        if (config.getRendering().isAnimateGrass()) {
            animateGrassButton.setState(1);
        } else {
            animateGrassButton.setState(0);
        }

        if (config.getRendering().isReflectiveWater()) {
            reflectiveWaterButton.setState(1);
        } else {
            reflectiveWaterButton.setState(0);
        }

        if (config.getRendering().isCameraBobbing()) {
            bobbingButton.setState(1);
        } else {
            bobbingButton.setState(0);
        }

        if (config.getRendering().isFullscreen()) {
            fullscreenButton.setState(1);
        } else {
            fullscreenButton.setState(0);
        }

        if (config.getRendering().isOutline()) {
            outlineButton.setState(1);
        } else {
            outlineButton.setState(0);
        }

        if (config.getRendering().isDynamicShadowsPcfFiltering()) {
            shadowButton.setState(2);
        } else if (config.getRendering().isDynamicShadows()) {
            shadowButton.setState(1);
        } else {
            shadowButton.setState(0);
        }

        if (config.getRendering().isVolumetricFog()) {
            volumetricFogButton.setState(1);
        } else {
            volumetricFogButton.setState(0);
        }

        if (config.getRendering().isVSync()) {
            vSyncButton.setState(1);
        } else {
            vSyncButton.setState(0);
        }
    }
}
