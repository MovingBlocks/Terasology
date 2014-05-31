/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import com.google.common.collect.Lists;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.input.BindableButton;
import org.terasology.logic.console.Console;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.*;
import org.terasology.rendering.world.ViewDistance;

import java.util.Arrays;

/**
 * @author Immortius
 */
public class VideoSettingsScreen extends CoreScreenLayer {
    private static final Logger logger = LoggerFactory.getLogger(VideoSettingsScreen.class);

    @In
    private GameEngine engine;


    UICheckbox checkbox;
    @In
    private Config config;

    int windowHeight;
    int windowWidth;

    public VideoSettingsScreen() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {

        final UITooltip tooltip = new UITooltip();

       boolean testBoolean = true;
        windowHeight = config.getRendering().getWindowHeight();
        windowWidth = config.getRendering().getWindowWidth();



        UIDropdown<Preset> videoQuality = find("graphicsPreset", UIDropdown.class);
        if (videoQuality != null) {
            videoQuality.setOptions(Lists.newArrayList(Preset.CUSTOM, Preset.MINIMAL,Preset.NICE, Preset.EPIC, Preset.INSANE, Preset.UBER));
            videoQuality.bindSelection(new PresetBinding(config.getRendering()));
        }

        UIDropdown<EnvironmentalEffects> environmentalEffects = find("environmentEffects", UIDropdown.class);
        if (environmentalEffects != null) {
            environmentalEffects.setOptions(Lists.newArrayList(EnvironmentalEffects.OFF, EnvironmentalEffects.LOW, EnvironmentalEffects.HIGH));
            environmentalEffects.bindSelection(new EnvironmentEffectsBinding(config.getRendering()));
        }

        UIDropdown<ViewDistance> viewDistance = find("viewDistance", UIDropdown.class);
        if (viewDistance != null) {
            viewDistance.setOptions(Arrays.asList(ViewDistance.values()));
            viewDistance.bindSelection(BindHelper.bindBeanProperty("viewDistance", config.getRendering(), ViewDistance.class));
        }

        UIDropdown<WaterReflection> waterReflection = find("reflections", UIDropdown.class);
        if (waterReflection != null) {
            waterReflection.setOptions(Lists.newArrayList(WaterReflection.SKY, WaterReflection.GLOBAL, WaterReflection.LOCAL));
            waterReflection.bindSelection(new WaterReflectionBinding(config.getRendering()));
        }

        UIDropdown<Integer> blur = find("blur", UIDropdown.class);
        if (blur != null) {
            blur.setOptions(Lists.newArrayList(0, 1, 2, 3));
            blur.bindSelection(BindHelper.bindBeanProperty("blurIntensity", config.getRendering(), Integer.TYPE));
            blur.setOptionRenderer(new StringTextRenderer<Integer>() {

                @Override
                public String getString(Integer value) {
                    switch (value) {
                        case 1:
                            return "Some";
                        case 2:
                            return "Normal";
                        case 3:
                            return "Max";
                        default:
                            return "Off";
                    }
                }
            });
        }

        UIDropdown<DynamicShadows> dynamicShadows = find("shadows", UIDropdown.class);
        if (dynamicShadows != null) {
            dynamicShadows.setOptions(Arrays.asList(DynamicShadows.values()));
            dynamicShadows.bindSelection(new DynamicShadowsBinding(config.getRendering()));
        }

        final UISlider fovSlider = find("fov", UISlider.class);
        if (fovSlider != null) {
            fovSlider.setIncrement(5.0f);
            fovSlider.setPrecision(0);
            fovSlider.setMinimum(70);
            fovSlider.setRange(50);
            fovSlider.bindValue(BindHelper.bindBeanProperty("fieldOfView", config.getRendering(), Float.TYPE));
        }

        UIDropdown<CameraSetting> cameraSetting = find("camera", UIDropdown.class);
        if (cameraSetting != null) {
            cameraSetting.setOptions(Arrays.asList(CameraSetting.values()));
            cameraSetting.bindSelection(new CameraSettingBinding(config.getRendering()));
        }
        WidgetUtil.tryBindCheckbox(this, "oculusVrSupport", BindHelper.bindBeanProperty("oculusVrSupport", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "cloudShadow", BindHelper.bindBeanProperty("cloudShadows", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "parallax", BindHelper.bindBeanProperty("parallaxMapping", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "filmGrain", BindHelper.bindBeanProperty("filmGrain", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "motionBlur", BindHelper.bindBeanProperty("motionBlur", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "bobbing", BindHelper.bindBeanProperty("cameraBobbing", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "outLine", BindHelper.bindBeanProperty("outline", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "vsync", BindHelper.bindBeanProperty("vSync", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "eyeAdaptation", BindHelper.bindBeanProperty("eyeAdaptation", config.getRendering(), Boolean.TYPE));
        WidgetUtil.trySubscribe(this, "fovReset", new ActivateEventListener() {

            @Override
            public void onActivated(UIWidget widget) {
                CameraSettingBinding cam;
                fovSlider.setValue(100.0f);

            }
        });
        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });

    }

    @Override
    public void onClosed() {
        logger.info("Video Settings: " + config.getRendering().toString());
        CoreRegistry.get(ShaderManager.class).recompileAllShaders();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
