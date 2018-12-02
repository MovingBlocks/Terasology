/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.Keyboard;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.world.viewDistance.ViewDistance;

import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class VideoSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:VideoMenuScreen");

    private static final Logger logger = LoggerFactory.getLogger(VideoSettingsScreen.class);
    private static final long RESOLUTION_REVERT_TIME_MS = 15000;

    @In
    private GameEngine engine;

    @In
    private Config config;

    @In
    private DisplayDevice displayDevice;

    @In
    private LocalPlayer localPlayer;

    @In
    private TranslationSystem translationSystem;

    @In
    private Time time;

    public VideoSettingsScreen() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        UIDropdown<Preset> videoQuality = find("graphicsPreset", UIDropdown.class);
        if (videoQuality != null) {
            videoQuality.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            videoQuality.setOptions(Lists.newArrayList(Preset.CUSTOM, Preset.MINIMAL, Preset.LOW, Preset.MEDIUM, Preset.HIGH, Preset.ULTRA));
            videoQuality.bindSelection(new PresetBinding(config.getRendering()));
        }

        UIDropdown<ViewDistance> viewDistance = find("viewDistance", UIDropdown.class);
        if (viewDistance != null) {
            viewDistance.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            viewDistance.setOptions(Arrays.asList(ViewDistance.values()));
            viewDistance.bindSelection(BindHelper.bindBeanProperty("viewDistance", config.getRendering(), ViewDistance.class));
        }

        UIDropdown<WaterReflection> waterReflection = find("reflections", UIDropdown.class);
        if (waterReflection != null) {
            waterReflection.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            waterReflection.setOptions(Lists.newArrayList(WaterReflection.SKY, WaterReflection.GLOBAL, WaterReflection.LOCAL));
            waterReflection.bindSelection(new WaterReflectionBinding(config.getRendering()));
        }

        UIDropdown<ScreenshotSize> screenshotSize = find("screenshotSize", UIDropdown.class);
        if (screenshotSize != null) {
            screenshotSize.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            screenshotSize.setOptions(Arrays.asList(ScreenshotSize.values()));
            screenshotSize.bindSelection(BindHelper.bindBeanProperty("screenshotSize", config.getRendering(), ScreenshotSize.class));
        }

        UIDropdown<String> screenshotFormat = find("screenshotFormat", UIDropdown.class);
        if (screenshotFormat != null) {
            screenshotFormat.setOptions(Arrays.asList(ImageIO.getWriterFileSuffixes()));
            screenshotFormat.bindSelection(BindHelper.bindBeanProperty("screenshotFormat", config.getRendering(), String.class));
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
                            return translationSystem.translate("${engine:menu#camera-blur-some}");
                        case 2:
                            return translationSystem.translate("${engine:menu#camera-blur-normal}");
                        case 3:
                            return translationSystem.translate("${engine:menu#camera-blur-max}");
                        default:
                            return translationSystem.translate("${engine:menu#camera-blur-off}");
                    }
                }
            });
        }

        UIDropdown<DynamicShadows> dynamicShadows = find("shadows", UIDropdown.class);
        if (dynamicShadows != null) {
            dynamicShadows.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
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

        final UISlider frameLimitSlider = find("frameLimit", UISlider.class);
        if (frameLimitSlider != null) {
            frameLimitSlider.setIncrement(5.0f);
            frameLimitSlider.setPrecision(0);
            frameLimitSlider.setMinimum(30);
            frameLimitSlider.setRange(175); // Goes up to 205 (which is off)
            // Frame limit > 200 is just displayed and treated as "off"
            frameLimitSlider.setLabelFunction(input -> {
                if (input > 200) {
                    return " Off "; // Spaces to get wider than "200" (otherwise the display jumps around)
                } else {
                    return String.valueOf(input.intValue());
                }
            });
            frameLimitSlider.bindValue(new Binding<Float>() {
                @Override
                public Float get() {
                    if (config.getRendering().getFrameLimit() == -1) {
                        return 205f;
                    } else {
                        return (float) config.getRendering().getFrameLimit();
                    }
                }

                @Override
                public void set(Float value) {
                    int frameLimit = value.intValue();
                    if (frameLimit > 200) {
                        config.getRendering().setFrameLimit(-1);
                    } else {
                        config.getRendering().setFrameLimit(frameLimit);
                    }
                }
            });
        }

        final UISlider particleEffectLimitSlider = find("particleEffectLimit", UISlider.class);

        if (particleEffectLimitSlider != null) {
            particleEffectLimitSlider.setIncrement(1.0f);
            particleEffectLimitSlider.setPrecision(0);
            particleEffectLimitSlider.setMinimum(0);
            particleEffectLimitSlider.setRange(50);

            particleEffectLimitSlider.setLabelFunction(input -> {
                if (input == 0) {
                    return " Off ";
                } else {
                    return String.valueOf(input.intValue());
                }
            });
            particleEffectLimitSlider.bindValue(new Binding<Float>() {
                @Override
                public Float get() {
                    return (float) config.getRendering().getParticleEffectLimit();
                }

                @Override
                public void set(Float value) {
                    int particleEffectLimit = value.intValue();
                    config.getRendering().setParticleEffectLimit(particleEffectLimit);
                }
            });
        }

        final UISlider fboScaleSlider = find("fboScale", UISlider.class);
        if (fboScaleSlider != null) {
            fboScaleSlider.setIncrement(5.0f);
            fboScaleSlider.setPrecision(0);
            fboScaleSlider.setMinimum(25);
            fboScaleSlider.setRange(200);
            fboScaleSlider.setLabelFunction(input -> String.valueOf(input.intValue()) + "%");
            fboScaleSlider.bindValue(new Binding<Float>() {
                @Override
                public Float get() {
                    return (float) config.getRendering().getFboScale();
                }

                @Override
                public void set(Float value) {
                    config.getRendering().setFboScale(value.intValue());
                }
            });
        }

        UIDropdown<CameraSetting> cameraSetting = find("camera", UIDropdown.class);
        if (cameraSetting != null) {
            cameraSetting.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            cameraSetting.setOptions(Arrays.asList(CameraSetting.values()));
            cameraSetting.bindSelection(new CameraSettingBinding(config.getRendering()));
        }

        UIDropdown<DisplayModeSetting> displaySetting = find("displayModeSetting", UIDropdown.class);
        if (displaySetting != null) {
            displaySetting.setOptionRenderer(new ToStringTextRenderer<>(translationSystem));
            displaySetting.setOptions(Arrays.asList(DisplayModeSetting.values()));
            displaySetting.bindSelection(BindHelper.bindBeanProperty("displayModeSetting", displayDevice, DisplayModeSetting.class));
        }

        UIDropdown<Resolution> resolution = find("resolution", UIDropdown.class);
        if (resolution != null) {
            resolution.setOptions(displayDevice.getResolutions());
            resolution.bindSelection(new Binding<Resolution>() {

                @Override
                public Resolution get() {
                    return displayDevice.getResolution();
                }

                @Override
                public void set(Resolution value) {
                    onResolutionChange(value);
                }
            });
        }

        WidgetUtil.tryBindCheckbox(this, "menu-animations", BindHelper.bindBeanProperty("animatedMenu", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "oculusVrSupport", BindHelper.bindBeanProperty("vrSupport", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "animateGrass", BindHelper.bindBeanProperty("animateGrass", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "animateWater", BindHelper.bindBeanProperty("animateWater", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "volumetricFog", BindHelper.bindBeanProperty("volumetricFog", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "inscattering", BindHelper.bindBeanProperty("inscattering", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "cloudShadow", BindHelper.bindBeanProperty("cloudShadows", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "extraLighting", BindHelper.bindBeanProperty("normalMapping", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "parallax", BindHelper.bindBeanProperty("parallaxMapping", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "filmGrain", BindHelper.bindBeanProperty("filmGrain", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "motionBlur", BindHelper.bindBeanProperty("motionBlur", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "bobbing", BindHelper.bindBeanProperty("cameraBobbing", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "outline", BindHelper.bindBeanProperty("outline", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "vsync", BindHelper.bindBeanProperty("vSync", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "eyeAdaptation", BindHelper.bindBeanProperty("eyeAdaptation", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "ssao", BindHelper.bindBeanProperty("ssao", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "clampLighting", BindHelper.bindBeanProperty("clampLighting", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "bloom", BindHelper.bindBeanProperty("bloom", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "lightShafts", BindHelper.bindBeanProperty("lightShafts", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "vignette", BindHelper.bindBeanProperty("vignette", config.getRendering(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "flickeringLight", BindHelper.bindBeanProperty("flickeringLight", config.getRendering(), Boolean.TYPE));

        if (fovSlider != null) {
            WidgetUtil.trySubscribe(this, "fovReset", widget -> fovSlider.setValue(100.0f));
        }

        WidgetUtil.trySubscribe(this, "close", button -> saveSettings());
    }


    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.ESCAPE) {
            saveSettings();
        }
        return false;
    }

    public void saveSettings() {
        logger.info("Video Settings: {}", config.renderConfigAsJson(config.getRendering()));
        // TODO: add a dirty flag that checks if recompiling is needed
        CoreRegistry.get(ShaderManager.class).recompileAllShaders();
        triggerBackAnimation();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void onFullScreenResolutionChange(Resolution oldResolution) {
        Callable<Resolution> revertOperation = () -> {
            Thread.sleep(RESOLUTION_REVERT_TIME_MS);
            return oldResolution;
        };

        @SuppressWarnings("unchecked")
        WaitPopup<Resolution> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);

        popup.startOperation(revertOperation, true);
        popup.onSuccess(resolution -> displayDevice.setResolution(resolution));
        popup.setTitleText(translationSystem.translate("${engine:menu#video-resolution-popup-title}"));
        popup.setCancelText(translationSystem.translate("${engine:menu#video-resolution-popup-cancel}"));

        long revertAtMs = time.getGameTimeInMs() + RESOLUTION_REVERT_TIME_MS;
        String message = translationSystem.translate("${engine:menu#video-resolution-popup-message}");

        popup.bindMessageText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                long remaining = TimeUnit.MILLISECONDS.toSeconds(revertAtMs - time.getGameTimeInMs());
                return message + ": " + remaining + "s";
            }
        });
    }

    private void onResolutionChange(Resolution newResolution) {
        Resolution oldResolution = displayDevice.getResolution();
        displayDevice.setResolution(newResolution);
        if (DisplayModeSetting.FULLSCREEN == displayDevice.getDisplayModeSetting()) {
            onFullScreenResolutionChange(oldResolution);
        }
    }
}
