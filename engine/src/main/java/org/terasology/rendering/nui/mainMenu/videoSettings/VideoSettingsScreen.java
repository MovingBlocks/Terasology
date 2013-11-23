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
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.entitySystem.systems.In;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;
import org.terasology.rendering.world.ViewDistance;

import javax.vecmath.Vector2f;
import java.util.Arrays;

/**
 * @author Immortius
 */
public class VideoSettingsScreen extends UIScreen {
    @In
    private NUIManager nuiManager;

    @In
    private GameEngine engine;

    @In
    private Config config;

    public VideoSettingsScreen() {

    }

    public void initialise() {
        ColumnLayout grid = new ColumnLayout();
        grid.setColumns(4);
        grid.addWidget(new UILabel("Graphics Quality:"));
        grid.addWidget(new UIDropdown<>("quality"));
        grid.addWidget(new UILabel("Environment Effects:"));
        grid.addWidget(new UIDropdown<>("environmentEffects"));
        grid.addWidget(new UILabel("Viewing Distance:"));
        grid.addWidget(new UIDropdown<>("viewDistance"));
        grid.addWidget(new UILabel("Reflections:"));
        grid.addWidget(new UIDropdown<>("reflections"));
        grid.addWidget(new UILabel("FOV:"));
        grid.addWidget(new UISlider("fov"));
        grid.addWidget(new UILabel("Blur Intensity:"));
        grid.addWidget(new UIButton("blur", "Normal"));
        grid.addWidget(new UILabel("Bobbing:"));
        grid.addWidget(new UICheckbox("bobbing"));
        grid.addWidget(new UILabel("Fullscreen:"));
        grid.addWidget(new UICheckbox("fullscreen"));
        grid.addWidget(new UILabel("Dynamic Shadows:"));
        grid.addWidget(new UIButton("shadows", "Off"));
        grid.addWidget(new UILabel("Outline:"));
        grid.addWidget(new UICheckbox("outline"));
        grid.addWidget(new UILabel("VSync:"));
        grid.addWidget(new UICheckbox("vsync"));
        grid.setPadding(new Border(0, 0, 4, 4));
        grid.setFamily("option-grid");

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("Pre Alpha"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(720, 192), new Vector2f(0.5f, 0.6f));
        layout.addFixedWidget(new UIButton("close", "Back"), new Vector2i(280, 32), new Vector2f(0.5f, 0.95f));

        setContents(layout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setContents(UIWidget contents) {
        super.setContents(contents);

        UIDropdown<VideoQuality> videoQuality = find("quality", UIDropdown.class);
        videoQuality.setOptions(Lists.newArrayList(VideoQuality.NICE, VideoQuality.EPIC, VideoQuality.INSANE, VideoQuality.UBER));
        videoQuality.bindSelection(new VideoQualityBinding(config.getRendering()));

        UIDropdown<EnvironmentalEffects> environmentalEffects = find("environmentEffects", UIDropdown.class);
        environmentalEffects.setOptions(Lists.newArrayList(EnvironmentalEffects.OFF, EnvironmentalEffects.LOW, EnvironmentalEffects.MEDIUM, EnvironmentalEffects.HIGH));
        environmentalEffects.bindSelection(new EnvironmentEffectsBinding(config.getRendering()));

        UIDropdown<ViewDistance> viewDistance = find("viewDistance", UIDropdown.class);
        viewDistance.setOptions(Arrays.asList(ViewDistance.values()));
        viewDistance.bindSelection(BindHelper.bindBeanProperty("viewDistance", config.getRendering(), ViewDistance.class));

        UIDropdown<WaterReflection> waterReflection = find("reflections", UIDropdown.class);
        waterReflection.setOptions(Lists.newArrayList(WaterReflection.SKY, WaterReflection.LOCAL, WaterReflection.GLOBAL));
        waterReflection.bindSelection(new WaterReflectionBinding(config.getRendering()));

        UISlider fovSlider = find("fov", UISlider.class);
        fovSlider.setIncrement(5.0f);
        fovSlider.setPrecision(0);
        fovSlider.setMinimum(70);
        fovSlider.setRange(50);
        fovSlider.bindValue(BindHelper.bindBeanProperty("fieldOfView", config.getRendering(), Float.TYPE));

        find("fullscreen", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("fullscreen", engine, Boolean.TYPE));
        find("bobbing", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("cameraBobbing", config.getRendering(), Boolean.TYPE));
        find("outline", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("outline", config.getRendering(), Boolean.TYPE));
        find("vsync", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("vSync", config.getRendering(), Boolean.TYPE));
        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                CoreRegistry.get(ShaderManager.class).recompileAllShaders();
                TerasologyEngine te = (TerasologyEngine) engine;
                if (te.isFullscreen() != find("fullscreen", UICheckbox.class).isChecked()) {
                    te.setFullscreen(!te.isFullscreen());
                }
                nuiManager.popScreen();
            }
        });
    }
}
