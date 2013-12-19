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
package org.terasology.rendering.nui.mainMenu.videoSettings;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.entitySystem.systems.In;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIScreenUtil;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.displayAdapting.DisplayStringAdapter;
import org.terasology.rendering.world.ViewDistance;

import java.util.Arrays;

/**
 * @author Immortius
 */
public class VideoSettingsScreen extends UIScreen {
    private static final Logger logger = LoggerFactory.getLogger(VideoSettingsScreen.class);

    @In
    private NUIManager nuiManager;

    @In
    private GameEngine engine;

    @In
    private Config config;

    public VideoSettingsScreen() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        UIDropdown<VideoQuality> videoQuality = find("quality", UIDropdown.class);
        if (videoQuality != null) {
            videoQuality.setOptions(Lists.newArrayList(VideoQuality.NICE, VideoQuality.EPIC, VideoQuality.INSANE, VideoQuality.UBER));
            videoQuality.bindSelection(new VideoQualityBinding(config.getRendering()));
        }

        UIDropdown<EnvironmentalEffects> environmentalEffects = find("environmentEffects", UIDropdown.class);
        if (environmentalEffects != null) {
            environmentalEffects.setOptions(Lists.newArrayList(EnvironmentalEffects.OFF, EnvironmentalEffects.LOW, EnvironmentalEffects.MEDIUM, EnvironmentalEffects.HIGH));
            environmentalEffects.bindSelection(new EnvironmentEffectsBinding(config.getRendering()));
        }

        UIDropdown<ViewDistance> viewDistance = find("viewDistance", UIDropdown.class);
        if (viewDistance != null) {
            viewDistance.setOptions(Arrays.asList(ViewDistance.values()));
            viewDistance.bindSelection(BindHelper.bindBeanProperty("viewDistance", config.getRendering(), ViewDistance.class));
        }

        UIDropdown<WaterReflection> waterReflection = find("reflections", UIDropdown.class);
        if (waterReflection != null) {
            waterReflection.setOptions(Lists.newArrayList(WaterReflection.SKY, WaterReflection.LOCAL, WaterReflection.GLOBAL));
            waterReflection.bindSelection(new WaterReflectionBinding(config.getRendering()));
        }

        UIDropdown<Integer> blur = find("blur", UIDropdown.class);
        if (blur != null) {
            blur.setOptions(Lists.newArrayList(0, 1, 2, 3));
            blur.bindSelection(BindHelper.bindBeanProperty("blurIntensity", config.getRendering(), Integer.TYPE));
            blur.setOptionAdapter(new DisplayStringAdapter<Integer>() {
                @Override
                public String convert(Integer value) {
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

        UISlider fovSlider = find("fov", UISlider.class);
        if (fovSlider != null) {
            fovSlider.setIncrement(5.0f);
            fovSlider.setPrecision(0);
            fovSlider.setMinimum(70);
            fovSlider.setRange(50);
            fovSlider.bindValue(BindHelper.bindBeanProperty("fieldOfView", config.getRendering(), Float.TYPE));
        }

        UIScreenUtil.tryBindCheckbox(this, "fullscreen", BindHelper.bindBeanProperty("fullscreen", engine, Boolean.TYPE));
        UIScreenUtil.tryBindCheckbox(this, "bobbing", BindHelper.bindBeanProperty("cameraBobbing", config.getRendering(), Boolean.TYPE));
        UIScreenUtil.tryBindCheckbox(this, "outline", BindHelper.bindBeanProperty("outline", config.getRendering(), Boolean.TYPE));
        UIScreenUtil.tryBindCheckbox(this, "vsync", BindHelper.bindBeanProperty("vSync", config.getRendering(), Boolean.TYPE));
        UIScreenUtil.trySubscribe(this, "close", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                logger.info("Video Settings: " + config.getRendering().toString());
                CoreRegistry.get(ShaderManager.class).recompileAllShaders();
                nuiManager.popScreen();
            }
        });
    }
}
