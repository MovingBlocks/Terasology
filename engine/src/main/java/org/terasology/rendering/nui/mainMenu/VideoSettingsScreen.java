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

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
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
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;

import javax.vecmath.Vector2f;

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
        layout.addFixedWidget(grid, new Vector2i(560, 192), new Vector2f(0.5f, 0.6f));
        layout.addFixedWidget(new UIButton("close", "Back"), new Vector2i(280, 32), new Vector2f(0.5f, 0.95f));

        setContents(layout);
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        UIDropdown<VideoQuality> quality = find("quality", UIDropdown.class);
        quality.setOptions(Lists.newArrayList(VideoQuality.NICE, VideoQuality.EPIC, VideoQuality.INSANE, VideoQuality.UBER));
        quality.bindSelection(new VideoQualityBinding(config.getRendering()));

        find("fullscreen", UICheckbox.class).bindChecked(new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return ((TerasologyEngine) engine).isFullscreen();
            }

            @Override
            public void set(Boolean value) {
                ((TerasologyEngine) engine).setFullscreen(value);
            }
        });
        find("bobbing", UICheckbox.class).bindChecked(new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return config.getRendering().isCameraBobbing();
            }

            @Override
            public void set(Boolean value) {
                config.getRendering().setCameraBobbing(value);
            }
        });
        find("outline", UICheckbox.class).bindChecked(new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return config.getRendering().isOutline();
            }

            @Override
            public void set(Boolean value) {
                config.getRendering().setOutline(value);
            }
        });
        find("vsync", UICheckbox.class).bindChecked(new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return config.getRendering().isVSync();
            }

            @Override
            public void set(Boolean value) {
                config.getRendering().setVSync(value);
            }
        });
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

    private enum VideoQuality {
        NICE("Nice") {
            @Override
            public void apply(RenderingConfig renderConfig) {
                renderConfig.setVolumetricLighting(false);
                renderConfig.setVolumetricFog(false);
                renderConfig.setAnimateGrass(false);
                renderConfig.setAnimateWater(false);
            }
        },
        EPIC("Epic") {
            @Override
            public void apply(RenderingConfig renderConfig) {
                renderConfig.setVolumetricLighting(false);
                renderConfig.setVolumetricFog(true);
                renderConfig.setAnimateGrass(true);
                renderConfig.setAnimateWater(false);
            }
        },
        INSANE("Insane") {
            @Override
            public void apply(RenderingConfig renderConfig) {
                renderConfig.setVolumetricLighting(false);
                renderConfig.setVolumetricFog(true);
                renderConfig.setAnimateGrass(true);
                renderConfig.setAnimateWater(true);
            }
        },
        UBER("Uber") {
            @Override
            public void apply(RenderingConfig renderConfig) {
                renderConfig.setVolumetricLighting(true);
                renderConfig.setVolumetricFog(true);
                renderConfig.setAnimateGrass(true);
                renderConfig.setAnimateWater(true);
            }
        },
        CUSTOM("Custom") {
            @Override
            public void apply(RenderingConfig renderConfig) {
            }
        };

        private String displayName;

        private VideoQuality(String displayName) {
            this.displayName = displayName;
        }

        public abstract void apply(RenderingConfig renderConfig);

        @Override
        public String toString() {
            return displayName;
        }
    }

    private class VideoQualityBinding implements Binding<VideoQuality> {

        private RenderingConfig config;

        public VideoQualityBinding(RenderingConfig config) {
            this.config = config;
        }

        @Override
        public VideoQuality get() {
            if (config.isVolumetricLighting()) {
                if (config.isAnimateWater() && config.isAnimateGrass() && config.isVolumetricFog()) {
                    return VideoQuality.UBER;
                }
            } else if (config.isAnimateWater()) {
                if (config.isAnimateGrass() && config.isVolumetricFog()) {
                    return VideoQuality.INSANE;
                }
            } else if (config.isAnimateGrass()) {
                if (config.isVolumetricFog()) {
                    return VideoQuality.EPIC;
                }
            } else if (!config.isVolumetricFog()) {
                return VideoQuality.NICE;
            }
            return VideoQuality.CUSTOM;
        }

        @Override
        public void set(VideoQuality value) {
            value.apply(config);
        }
    }
}
