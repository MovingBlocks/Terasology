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


package org.terasology.rendering.gui.dialogs;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;

import java.io.ByteArrayOutputStream;

import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.*;
import org.terasology.rendering.gui.windows.UIMenuSingleplayer;
import org.terasology.world.TerrainPreviewGenerator;

import org.terasology.world.generator.core.PerlinTerrainGeneratorWithSetup;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.newdawn.slick.opengl.PNGDecoder;
import java.io.ByteArrayInputStream;


import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;


/*
 * Dialog for map setup
 *
 * @author MPratt "map gen setup" <marsmod@hotmail.com>
 * @version 0.2
 */

public class UIDialogSetUpMap extends UIDialog {

    private UIButton OKButton;
    private UIButton DeafaultsButton;
    private UIButton ReloadPreviewButton;

    private UILabel BaseTerrainFACTORLabel;
    private UISlider BaseTerrainFACTOR;
    private UISlider BaseTerrainslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel OceanTerrainFACTORLabel;
    private UISlider OceanTerrainFACTOR;
    private UISlider OceanTerrainslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel RiverTerrainFACTORLabel;
    private UISlider RiverTerrainFACTOR;
    private UISlider RiverTerrainslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel MountainFACTORLabel;
    private UISlider MountainFACTOR;
    private UISlider Mountainslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel HillDensityFACTORLabel;
    private UISlider HillDensityFACTOR;
    private UISlider HillDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel plateauAreaFACTORLabel;
    private UISlider plateauAreaFACTOR;
    private UISlider plateauAreaslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel caveDensityFACTORLabel;
    private UISlider caveDensityFACTOR;
    private UISlider caveDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel WorldTitleFACTORLabel;
    private UIText WorldTitleFACTOR;

    private UILabel DefaultSeedFACTORLabel;
    private UIText DefaultSeedFACTOR;

    private UILabel ForestGrassDensityFACTORLabel;
    private UISlider ForestGrassDensityFACTOR;
    private UISlider ForestGrassDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel PlainsGrassDensityFACTORLabel;
    private UISlider PlainsGrassDensityFACTOR;
    private UISlider PlainsGrassDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel SnowGrassDensityFACTORLabel;
    private UISlider SnowGrassDensityFACTOR;
    private UISlider SnowGrassDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel MountainGrassDensityFACTORLabel;
    private UISlider MountainGrassDensityFACTOR;
    private UISlider MountainGrassDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel DesertGrassDensityFACTORLabel;
    private UISlider DesertGrassDensityFACTOR;
    private UISlider DesertGrassDensityslider= new UISlider(new Vector2f(64f, 32f), 0, 0);

    private UILabel ZOOM_FACTORLabel;
    private UISlider ZOOM_FACTOR;
    private UISlider ZOOM_FACTORslider= new UISlider(new Vector2f(128f, 32f), 0, 0);


    private UILabel SIZE_XFACTORLabel;
    private UIImage MapPreview;

    private UILabel SAMPLE_RATE_3D_HORLabel;
    private UIComboBox SAMPLE_RATE_3D_HORFACTOR;

    private UILabel SAMPLE_RATE_3D_VERTLabel;
    private UIComboBox SAMPLE_RATE_3D_VERTFACTOR;

    public UIDialogSetUpMap() {

        super(new Vector2f(640f, 500f));
        setTitle("Setup Map");
       // UIDialogSetUpMap.this.setPosition(new Vector2f(UIDialogSetUpMap.this.getPosition().x, UIDialogSetUpMap.this.getPosition().y));
    }

    @Override
    protected void createDialogArea(final UIDisplayContainer parent) {
        final Config config = CoreRegistry.get(Config.class);
        setModal(true);



        BaseTerrainFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 100);
        BaseTerrainFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        BaseTerrainFACTOR.setVisible(true);
        BaseTerrainFACTOR.setMin(1);
        BaseTerrainFACTOR.setMax(99);
		BaseTerrainFACTOR.setValue(Math.round(config.getWorldGeneration().getBaseTerrainFACTOR()));
        BaseTerrainFACTOR.setText(String.valueOf((int)config.getWorldGeneration().getBaseTerrainFACTOR()));
        BaseTerrainFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                BaseTerrainslider = (UISlider) element;
                BaseTerrainslider.setText(String.valueOf(BaseTerrainslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setBaseTerrainFACTOR( (float)BaseTerrainFACTOR.getValue() );
            }
        });
        BaseTerrainFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                BaseTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                BaseTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {
                    if (BaseTerrainFACTOR.isFocused()) {
                    BaseTerrainFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        OceanTerrainFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 100);
        OceanTerrainFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        OceanTerrainFACTOR.setVisible(true);
        OceanTerrainFACTOR.setMin(1);
        OceanTerrainFACTOR.setMax(99);
        OceanTerrainFACTOR.setValue(Math.round(config.getWorldGeneration().getOceanTerrainFACTOR()));
        OceanTerrainFACTOR.setText(String.valueOf((int) config.getWorldGeneration().getOceanTerrainFACTOR()));
        OceanTerrainFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                OceanTerrainslider = (UISlider) element;
                OceanTerrainslider.setText(String.valueOf(OceanTerrainslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setOceanTerrainFACTOR( (float)OceanTerrainFACTOR.getValue() );
            }
        });
        OceanTerrainFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                OceanTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                OceanTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (OceanTerrainFACTOR.isFocused()) {
                    OceanTerrainFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });


        RiverTerrainFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 1000);
        RiverTerrainFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        RiverTerrainFACTOR.setVisible(true);
        RiverTerrainFACTOR.setMin(1);
        RiverTerrainFACTOR.setMax(999);
        RiverTerrainFACTOR.setValue(Math.round(config.getWorldGeneration().getRiverTerrainFACTOR()));
        RiverTerrainFACTOR.setText(String.valueOf((int)config.getWorldGeneration().getRiverTerrainFACTOR()));
        RiverTerrainFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                RiverTerrainslider = (UISlider) element;
                RiverTerrainslider.setText(String.valueOf(RiverTerrainslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setRiverTerrainFACTOR( (float)RiverTerrainFACTOR.getValue() );
            }
        });
        RiverTerrainFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                RiverTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                RiverTerrainFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (RiverTerrainFACTOR.isFocused()) {
                    RiverTerrainFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        MountainFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 100);
        MountainFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        MountainFACTOR.setVisible(true);
        MountainFACTOR.setMin(1);
        MountainFACTOR.setMax(99);
        MountainFACTOR.setValue(Math.round(config.getWorldGeneration().getMountainFACTOR()));
        MountainFACTOR.setText(String.valueOf((int)config.getWorldGeneration().getMountainFACTOR()));
        MountainFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                Mountainslider = (UISlider) element;
                Mountainslider.setText(String.valueOf(Mountainslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setMountainFACTOR( (float)MountainFACTOR.getValue() );
            }
        });
        MountainFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                MountainFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                MountainFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (MountainFACTOR.isFocused()) {
                    MountainFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        HillDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 100);
        HillDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        HillDensityFACTOR.setVisible(true);
        HillDensityFACTOR.setMin(1);
        HillDensityFACTOR.setMax(99);
        HillDensityFACTOR.setValue( Math.round(config.getWorldGeneration().getHillDensityFACTOR()));
        HillDensityFACTOR.setText(String.valueOf((int)config.getWorldGeneration().getHillDensityFACTOR()));
        HillDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                HillDensityslider = (UISlider) element;
                HillDensityslider.setText(String.valueOf(HillDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setHillDensityFACTOR( (float)HillDensityFACTOR.getValue() );
            }
        });
        HillDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                HillDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                HillDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (HillDensityFACTOR.isFocused()) {
                    HillDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        plateauAreaFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 1000);
        plateauAreaFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        plateauAreaFACTOR.setVisible(true);
        plateauAreaFACTOR.setMin(1);
        plateauAreaFACTOR.setMax(999);
        plateauAreaFACTOR.setValue(Math.round(config.getWorldGeneration().getplateauAreaFACTOR()));
        plateauAreaFACTOR.setText(String.valueOf((int)config.getWorldGeneration().getplateauAreaFACTOR()));
        plateauAreaFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                plateauAreaslider = (UISlider) element;
                plateauAreaslider.setText(String.valueOf(plateauAreaslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setplateauAreaFACTOR( (float)plateauAreaFACTOR.getValue() );
            }
        });
        plateauAreaFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                plateauAreaFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                plateauAreaFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (plateauAreaFACTOR.isFocused()) {
                    plateauAreaFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        caveDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, 1000);
        caveDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        caveDensityFACTOR.setVisible(true);
        caveDensityFACTOR.setMin(1);
        caveDensityFACTOR.setMax(999);
        caveDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getcaveDensityFACTOR()));
        caveDensityFACTOR.setText(String.valueOf(Math.round(config.getWorldGeneration().getcaveDensityFACTOR())));
        caveDensityFACTOR.addChangedListener(new ChangedListener() {
                @Override
                public void changed(UIDisplayElement element) {
                    caveDensityslider = (UISlider) element;
                    caveDensityslider.setText(String.valueOf(caveDensityslider.getValue()));
                    Config config = CoreRegistry.get(Config.class);
                    config.getWorldGeneration().setcaveDensityFACTOR( (float)caveDensityFACTOR.getValue() );
                }
            });
            caveDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
                @Override
                public void leave(UIDisplayElement element) {
                    caveDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
                }

                @Override
                public void hover(UIDisplayElement element) {

                }

                @Override
                public void enter(UIDisplayElement element) {
                    CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                    caveDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
                }

                @Override  //TODO I should just fix UI slider instead of override
                public void move(UIDisplayElement element) {

                    if (caveDensityFACTOR.isFocused()) {
                        caveDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                    }
                }
            });

        WorldTitleFACTOR = new UIText();
        WorldTitleFACTOR.setSize(new Vector2f(100f, 30f));
        WorldTitleFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        WorldTitleFACTOR.setVisible(true);
        UIMenuSingleplayer menu = (UIMenuSingleplayer) getGUIManager().getWindowById("singleplayer");
        WorldTitleFACTOR.setText("World" + (menu.getWorldCount() + 1));
       // WorldTitleFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        WorldTitleFACTOR.setSelectionColor(Color.gray);
        WorldTitleFACTOR.addChangedListener(new ChangedListener() {
                @Override
                public void changed(UIDisplayElement element) {
                    Config config = CoreRegistry.get(Config.class);
                    config.getWorldGeneration().setWorldTitle( WorldTitleFACTOR.getText() );
                }
            });
        DefaultSeedFACTOR = new UIText();
        DefaultSeedFACTOR.setSize(new Vector2f(270f, 30f));
        DefaultSeedFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        DefaultSeedFACTOR.setVisible(true);
        DefaultSeedFACTOR.setText(config.getWorldGeneration().getDefaultSeed());
        DefaultSeedFACTOR.setSelectionColor(Color.gray);
        DefaultSeedFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setDefaultSeed( DefaultSeedFACTOR.getText() );
            }
        });

        //private float forestGrassDensity = 0.3f;
        int range = Math.round(0.3f * 100000f *2f);
        ForestGrassDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, range);
        ForestGrassDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        ForestGrassDensityFACTOR.setVisible(true);
        ForestGrassDensityFACTOR.setMin(1);
        ForestGrassDensityFACTOR.setMax(range-1);
        ForestGrassDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getForestGrassDensity()*100000.0f));
        ForestGrassDensityFACTOR.setText(String.valueOf(Math.round(config.getWorldGeneration().getForestGrassDensity()*100000.0f)));
        ForestGrassDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                ForestGrassDensityslider = (UISlider) element;
                ForestGrassDensityslider.setText(String.valueOf(ForestGrassDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setForestGrassDensity((float)ForestGrassDensityFACTOR.getValue()/100000f);
            }
        });
        ForestGrassDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                ForestGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                ForestGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (ForestGrassDensityFACTOR.isFocused()) {
                    ForestGrassDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        //private float plainsGrassDensity = 0.2f;
        range = Math.round(0.2f * 100000f *2f);
        PlainsGrassDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, range);
        PlainsGrassDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        PlainsGrassDensityFACTOR.setVisible(true);
        PlainsGrassDensityFACTOR.setMin(1);
        PlainsGrassDensityFACTOR.setMax(range-1);
        PlainsGrassDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getPlainsGrassDensity()*100000.0f));
        PlainsGrassDensityFACTOR.setText(String.valueOf(Math.round(config.getWorldGeneration().getPlainsGrassDensity()*100000.0f)));
        PlainsGrassDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                PlainsGrassDensityslider = (UISlider) element;
                PlainsGrassDensityslider.setText(String.valueOf(PlainsGrassDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setPlainsGrassDensity((float)PlainsGrassDensityFACTOR.getValue()/100000f);
            }
        });
        PlainsGrassDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                PlainsGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                PlainsGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (PlainsGrassDensityFACTOR.isFocused()) {
                    PlainsGrassDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);               }
            }
        });

        //private float snowGrassDensity = 0.001f;
        range = Math.round(0.001f * 100000f *2f);
        SnowGrassDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, range);
        SnowGrassDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        SnowGrassDensityFACTOR.setVisible(true);
        SnowGrassDensityFACTOR.setMin(1);
        SnowGrassDensityFACTOR.setMax(range-1);
        SnowGrassDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getSnowGrassDensity()*100000.0f));
        SnowGrassDensityFACTOR.setText(String.valueOf(Math.round(config.getWorldGeneration().getSnowGrassDensity()*100000.0f)));
        SnowGrassDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                SnowGrassDensityslider = (UISlider) element;
                SnowGrassDensityslider.setText(String.valueOf(SnowGrassDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setSnowGrassDensity((float)SnowGrassDensityFACTOR.getValue()/100000f);
            }
        });
        SnowGrassDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                SnowGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                SnowGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (SnowGrassDensityFACTOR.isFocused()) {
                    SnowGrassDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

       //private float mountainGrassDensity = 0.2f;
        range = Math.round(0.2f * 100000f *2f);
        MountainGrassDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, range);
        MountainGrassDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        MountainGrassDensityFACTOR.setVisible(true);
        MountainGrassDensityFACTOR.setMin(1);
        MountainGrassDensityFACTOR.setMax(range-1);
        MountainGrassDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getMountainGrassDensity()*100000.0f));
        MountainGrassDensityFACTOR.setText(String.valueOf(Math.round(config.getWorldGeneration().getMountainGrassDensity()*100000.0f)));
        MountainGrassDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                MountainGrassDensityslider = (UISlider) element;
                MountainGrassDensityslider.setText(String.valueOf(MountainGrassDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setMountainGrassDensity((float)MountainGrassDensityFACTOR.getValue()/100000f);
            }
        });
        MountainGrassDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                MountainGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                MountainGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (MountainGrassDensityFACTOR.isFocused()) {
                    MountainGrassDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });

        //private float desertGrassDensity = 0.001f;
        range = Math.round(0.001f * 100000f *2f);
        DesertGrassDensityFACTOR = new UISlider(new Vector2f(64f, 32f), 0, range);
        DesertGrassDensityFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        DesertGrassDensityFACTOR.setVisible(true);
        DesertGrassDensityFACTOR.setMin(1);
        DesertGrassDensityFACTOR.setMax(range-1);
        DesertGrassDensityFACTOR.setValue(Math.round(config.getWorldGeneration().getDesertGrassDensity()*100000.0f));
        DesertGrassDensityFACTOR.setText(String.valueOf( Math.round(config.getWorldGeneration().getDesertGrassDensity()*100000.0f)));
        DesertGrassDensityFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                DesertGrassDensityslider = (UISlider) element;
                DesertGrassDensityslider.setText(String.valueOf(DesertGrassDensityslider.getValue()));
                Config config = CoreRegistry.get(Config.class);
                config.getWorldGeneration().setDesertGrassDensity((float)DesertGrassDensityFACTOR.getValue()/100000f);
            }
        });
        DesertGrassDensityFACTOR.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void leave(UIDisplayElement element) {
                DesertGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {
                CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                DesertGrassDensityFACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
            }

            @Override  //TODO I should just fix UI slider instead of override
            public void move(UIDisplayElement element) {

                if (DesertGrassDensityFACTOR.isFocused()) {
                    DesertGrassDensityFACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                }
            }
        });


            ZOOM_FACTOR = new UISlider(new Vector2f(128f, 16f), 0, 100);
            ZOOM_FACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
            ZOOM_FACTOR.setVisible(true);
            ZOOM_FACTOR.setMin(1);
            ZOOM_FACTOR.setBackgroundColor(Color.blue);
            ZOOM_FACTOR.setMax(100);
            ZOOM_FACTOR.setValue( Math.round(config.getWorldGeneration().getZOOM_FACTOR()*100000.0f));
            ZOOM_FACTOR.setText(String.valueOf( Math.round(config.getWorldGeneration().getZOOM_FACTOR()*100000.0f)));
            ZOOM_FACTOR.addChangedListener(new ChangedListener() {
                @Override
                public void changed(UIDisplayElement element) {
                    ZOOM_FACTORslider = (UISlider) element;
                    ZOOM_FACTORslider.setText(String.valueOf(ZOOM_FACTORslider.getValue()));
                    TerrainPreviewGenerator.setZOOM_FACTOR((float)ZOOM_FACTOR.getValue());
                }
            });
            ZOOM_FACTOR.addMouseMoveListener(new MouseMoveListener() {
                @Override
                public void leave(UIDisplayElement element) {
                    ZOOM_FACTOR.setBackgroundImage(new Vector2f(0f, 0f), new Vector2f(256f, 30f));
                 }

                @Override
                public void hover(UIDisplayElement element) {

                }

                @Override
                public void enter(UIDisplayElement element) {
                    CoreRegistry.get(AudioManager.class).playSound(Assets.getSound("engine:click"), 1.0f);
                    ZOOM_FACTOR.setBackgroundImage(new Vector2f(0f, 30f), new Vector2f(256f, 30f));
                }

                @Override  //TODO I should just fix UI slider instead of override
                public void move(UIDisplayElement element) {

                    if (ZOOM_FACTOR.isFocused()) {
                        ZOOM_FACTOR.changeSlider(new Vector2f( Mouse.getX()-UIDialogSetUpMap.this.getPosition().x,Display.getHeight() - Mouse.getY()).x);
                    }
                }
            });



        SAMPLE_RATE_3D_HORFACTOR = new UIComboBox(new Vector2f(44f, 22f), new Vector2f(44f, 56f));
        UIListItem item = new UIListItem("4", 0);
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));
        SAMPLE_RATE_3D_HORFACTOR.addItem(item);
        item = new UIListItem("8", 1);
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));
        SAMPLE_RATE_3D_HORFACTOR.addItem(item);
        item = new UIListItem("16", 2);
        item.setTextColor(Color.cyan);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));
        SAMPLE_RATE_3D_HORFACTOR.addItem(item);
        //brain may explode
        int num = (int) (Math.log(PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR)/Math.log(2)-2f);
        SAMPLE_RATE_3D_HORFACTOR.select(num);
        SAMPLE_RATE_3D_HORFACTOR.setVisible(true);
        SAMPLE_RATE_3D_HORFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
        SAMPLE_RATE_3D_HORFACTOR.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {

                if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 0){
                    PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=4;
                }else if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 1){
                    PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=8;
                }else if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 2){
                    PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=16;
                }
            }
        });

        SAMPLE_RATE_3D_VERTFACTOR = new UIComboBox(new Vector2f(44f, 22f), new Vector2f(44f, 56f));
        item = new UIListItem("4", 0);
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));
        SAMPLE_RATE_3D_VERTFACTOR.addItem(item);
        item = new UIListItem("8", 1);
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));
        SAMPLE_RATE_3D_VERTFACTOR.addItem(item);
        item = new UIListItem("16", 2);
        item.setTextColor(Color.cyan);
        item.setPadding(new Vector4f(1f, 1f, 1f, 1f));

        SAMPLE_RATE_3D_VERTFACTOR.addItem(item);
        int number = (int) (Math.log(PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT)/Math.log(2)-2f);
        SAMPLE_RATE_3D_VERTFACTOR.select(number);
        SAMPLE_RATE_3D_VERTFACTOR.setVisible(true);
        SAMPLE_RATE_3D_VERTFACTOR.setHorizontalAlign(EHorizontalAlign.CENTER);
            SAMPLE_RATE_3D_HORFACTOR.addChangedListener(new ChangedListener() {
                @Override
                public void changed(UIDisplayElement element) {
                    if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 0){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=4;
                    }else if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 1){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=8;
                    }else if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 2){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=16;
                    }
                }
            });


        SIZE_XFACTORLabel = new UILabel();
        SIZE_XFACTORLabel.setColor(Color.darkGray);
        SIZE_XFACTORLabel.setSize(new Vector2f(10f, 12f));
        SIZE_XFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        SIZE_XFACTORLabel.setVisible(true);

        MapPreview = new UIImage(Assets.getTexture("engine:Biomes"));
        MapPreview.setSize(new Vector2f(256f, 256f));
        MapPreview.setTextureOrigin(new Vector2f(0.0f, 0.0f));
        MapPreview.setTextureSize(new Vector2f(256.0f, 256.0f));
        MapPreview.setVisible(true);
        MapPreview.setHorizontalAlign(EHorizontalAlign.CENTER);

        BaseTerrainFACTORLabel = new UILabel();
        BaseTerrainFACTORLabel.setText("Base %:");
        BaseTerrainFACTORLabel.setColor(Color.darkGray);
        BaseTerrainFACTORLabel.setSize(new Vector2f(10f, 12f));
        BaseTerrainFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        BaseTerrainFACTORLabel.setVisible(true);

        OceanTerrainFACTORLabel = new UILabel("Ocean %:");
        OceanTerrainFACTORLabel.setColor(Color.darkGray);
        OceanTerrainFACTORLabel.setSize(new Vector2f(10f, 12f));
        OceanTerrainFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        OceanTerrainFACTORLabel.setVisible(true);

        RiverTerrainFACTORLabel = new UILabel("River %:");
        RiverTerrainFACTORLabel.setColor(Color.darkGray);
        RiverTerrainFACTORLabel.setSize(new Vector2f(10f, 12f));
        RiverTerrainFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        RiverTerrainFACTORLabel.setVisible(true);

        MountainFACTORLabel = new UILabel("Mountain %:");
        MountainFACTORLabel.setColor(Color.darkGray);
        MountainFACTORLabel.setSize(new Vector2f(10f, 12f));
        MountainFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        MountainFACTORLabel.setVisible(true);

        HillDensityFACTORLabel = new UILabel("Hill %:");
        HillDensityFACTORLabel.setColor(Color.darkGray);
        HillDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        HillDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        HillDensityFACTORLabel.setVisible(true);

        plateauAreaFACTORLabel = new UILabel("Plateau %:");
        plateauAreaFACTORLabel.setColor(Color.darkGray);
        plateauAreaFACTORLabel.setSize(new Vector2f(10f, 12f));
        plateauAreaFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        plateauAreaFACTORLabel.setVisible(true);

        caveDensityFACTORLabel = new UILabel("Cave %:");
        caveDensityFACTORLabel.setColor(Color.darkGray);
        caveDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        caveDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        caveDensityFACTORLabel.setVisible(true);

        WorldTitleFACTORLabel = new UILabel("WorldTitle:");
        WorldTitleFACTORLabel.setColor(Color.darkGray);
        WorldTitleFACTORLabel.setSize(new Vector2f(10f, 12f));
        WorldTitleFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        WorldTitleFACTORLabel.setVisible(true);

        DefaultSeedFACTORLabel = new UILabel("CurrentSeed:");
        DefaultSeedFACTORLabel.setColor(Color.darkGray);
        DefaultSeedFACTORLabel.setSize(new Vector2f(10f, 12f));
        DefaultSeedFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        DefaultSeedFACTORLabel.setVisible(true);

        ForestGrassDensityFACTORLabel = new UILabel("ForestGrassDensity:");
        ForestGrassDensityFACTORLabel.setColor(Color.darkGray);
        ForestGrassDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        ForestGrassDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        ForestGrassDensityFACTORLabel.setVisible(true);

        PlainsGrassDensityFACTORLabel = new UILabel("PlainsGrassDensity:");
        PlainsGrassDensityFACTORLabel.setColor(Color.darkGray);
        PlainsGrassDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        PlainsGrassDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        PlainsGrassDensityFACTORLabel.setVisible(true);

        SnowGrassDensityFACTORLabel = new UILabel("SnowGrassDensity:");
        SnowGrassDensityFACTORLabel.setColor(Color.darkGray);
        SnowGrassDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        SnowGrassDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        SnowGrassDensityFACTORLabel.setVisible(true);

        MountainGrassDensityFACTORLabel = new UILabel("MountainGrassDensity:");
        MountainGrassDensityFACTORLabel.setColor(Color.darkGray);
        MountainGrassDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        MountainGrassDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        MountainGrassDensityFACTORLabel.setVisible(true);

        DesertGrassDensityFACTORLabel = new UILabel("DesertGrassDensity:");
        DesertGrassDensityFACTORLabel.setColor(Color.darkGray);
        DesertGrassDensityFACTORLabel.setSize(new Vector2f(10f, 12f));
        DesertGrassDensityFACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        DesertGrassDensityFACTORLabel.setVisible(true);

        ZOOM_FACTORLabel = new UILabel("ZOOM FACTOR:");
        ZOOM_FACTORLabel.setColor(Color.darkGray);
        ZOOM_FACTORLabel.setSize(new Vector2f(10f, 12f));
        ZOOM_FACTORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        ZOOM_FACTORLabel.setVisible(true);

		SAMPLE_RATE_3D_HORLabel = new UILabel("H.Sample 4-16");
        SAMPLE_RATE_3D_HORLabel.setColor(Color.darkGray);
        SAMPLE_RATE_3D_HORLabel.setSize(new Vector2f(10f, 12f));
        SAMPLE_RATE_3D_HORLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        SAMPLE_RATE_3D_HORLabel.setVisible(true);

		SAMPLE_RATE_3D_VERTLabel = new UILabel("V.Sample 4-16");
        SAMPLE_RATE_3D_VERTLabel.setColor(Color.darkGray);
        SAMPLE_RATE_3D_VERTLabel.setSize(new Vector2f(10f, 12f));
        SAMPLE_RATE_3D_VERTLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        SAMPLE_RATE_3D_VERTLabel.setVisible(true);

        OKButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        OKButton.getLabel().setText(" OK ");
        OKButton.setVisible(true);
        OKButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        OKButton.addClickListener(new ClickListener() {
		
                @Override
                public void click(UIDisplayElement element, int button) {

                    Config config = CoreRegistry.get(Config.class);

                    if (BaseTerrainFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setBaseTerrainFACTOR( (float)BaseTerrainFACTOR.getValue() );
                    }
                    if (OceanTerrainFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setOceanTerrainFACTOR( (float)OceanTerrainFACTOR.getValue() );
                    }
                    if (RiverTerrainFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setRiverTerrainFACTOR( (float)RiverTerrainFACTOR.getValue() );
                    }
                    if (MountainFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setMountainFACTOR( (float)MountainFACTOR.getValue() );
                    }
                    if (HillDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setHillDensityFACTOR( (float)HillDensityFACTOR.getValue() );
                    }
                    if (plateauAreaFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setplateauAreaFACTOR( (float)plateauAreaFACTOR.getValue() );
                    }
                    if (caveDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setcaveDensityFACTOR( (float)caveDensityFACTOR.getValue() );
                    }
                    if (WorldTitleFACTOR.getText().length() > 0) {
                        config.getWorldGeneration().setWorldTitle( WorldTitleFACTOR.getText() );
                    }
                    if (DefaultSeedFACTOR.getText().length() > 0) {
                        config.getWorldGeneration().setDefaultSeed( DefaultSeedFACTOR.getText() );
                    }
                    if (ForestGrassDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setForestGrassDensity((float)ForestGrassDensityFACTOR.getValue()/100000f);
                    }
                    if (PlainsGrassDensityFACTOR.getValue() > 0) {
                         config.getWorldGeneration().setPlainsGrassDensity((float)PlainsGrassDensityFACTOR.getValue()/100000f);
                    }
                    if (SnowGrassDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setSnowGrassDensity((float)SnowGrassDensityFACTOR.getValue()/100000f);
                    }
                    if (MountainGrassDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setMountainGrassDensity((float)MountainGrassDensityFACTOR.getValue()/100000f);
                    }
                    if (DesertGrassDensityFACTOR.getValue() > 0) {
                        config.getWorldGeneration().setDesertGrassDensity((float)DesertGrassDensityFACTOR.getValue()/100000f);
                    }
                    if (ZOOM_FACTOR.getValue() > 0) {
                        config.getWorldGeneration().setZOOM_FACTOR((float)ZOOM_FACTOR.getValue()/100000f);
                        TerrainPreviewGenerator.setZOOM_FACTOR((float)ZOOM_FACTOR.getValue());
                    }
                    if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 0){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=4;
                    }else if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 1){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=8;
                    }else if(SAMPLE_RATE_3D_HORFACTOR.getSelectionIndex() == 2){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_HOR=16;
                    }
                    if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 0){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=4;
                    }else if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 1){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=8;
                    }else if(SAMPLE_RATE_3D_VERTFACTOR.getSelectionIndex() == 2){
                        PerlinTerrainGeneratorWithSetup.SAMPLE_RATE_3D_VERT=16;
                    }
                    close();
                }
            });


            DeafaultsButton = new UIButton(new Vector2f(200f, 32f), UIButton.ButtonType.NORMAL);
            DeafaultsButton.getLabel().setText(" Load Defaults ");
            DeafaultsButton.setVisible(true);
            DeafaultsButton.setHorizontalAlign(EHorizontalAlign.CENTER);

            DeafaultsButton.addClickListener(new ClickListener() {
                @Override
                public void click(UIDisplayElement element, int button) {
                    Config config = CoreRegistry.get(Config.class);

                    BaseTerrainFACTOR.setValue((int)(50.0f));
                    OceanTerrainFACTOR.setValue((int)(50.0f));
                    RiverTerrainFACTOR.setValue((int)(50.0f));
                    MountainFACTOR.setValue((int)(50.0f));
                    HillDensityFACTOR.setValue((int)(50.0f));
                    plateauAreaFACTOR.setValue((int)(50.0f));
                    caveDensityFACTOR.setValue((int)(50.0f));

                    UIMenuSingleplayer menu = (UIMenuSingleplayer) getGUIManager().getWindowById("singleplayer");
                    WorldTitleFACTOR.setText("World" + (menu.getWorldCount() + 1));

                    DefaultSeedFACTOR.setText( config.getWorldGeneration().getDefaultSeed());
                    //private float forestGrassDensity = 0.3f;
                    ForestGrassDensityFACTOR.setValue(Math.round(0.3f*100000f/2f));
                    ForestGrassDensityslider.setText(String.valueOf(Math.round(0.3f*100000f/2f)));
                    //private float plainsGrassDensity = 0.2f;
                    PlainsGrassDensityFACTOR.setValue(Math.round(0.2f*100000f/2f));
                    PlainsGrassDensityFACTOR.setText(String.valueOf(Math.round(0.2f*100000f/2f)));
                    //private float snowGrassDensity = 0.001f;
                    SnowGrassDensityFACTOR.setValue(Math.round(0.001f*100000f/2f));
                    SnowGrassDensityFACTOR.setText(String.valueOf(Math.round(0.001f*100000f/2f)));
                    //private float mountainGrassDensity = 0.2f;
                    MountainGrassDensityFACTOR.setValue(Math.round(0.2f*100000f/2f));
                    MountainGrassDensityFACTOR.setText(String.valueOf(Math.round(0.2f*100000f/2f)));
                    //private float desertGrassDensity = 0.001f;
                    DesertGrassDensityFACTOR.setValue(Math.round(0.001f*100000f/2f));
                    DesertGrassDensityFACTOR.setText(String.valueOf(Math.round(0.001f*100000f/2f)));
                    //ZOOM_FACTOR
                    ZOOM_FACTOR.setValue(8);
                    ZOOM_FACTOR.setText("8");
                }
            });

            ReloadPreviewButton = new UIButton(new Vector2f(100f, 32f), UIButton.ButtonType.NORMAL);
            ReloadPreviewButton.getLabel().setText("Preview");
            ReloadPreviewButton.setVisible(true);
            ReloadPreviewButton.setHorizontalAlign(EHorizontalAlign.CENTER);

            ReloadPreviewButton.addClickListener(new ClickListener() {
                @Override
                public void click(UIDisplayElement element, int button) {

                //update Terrain Preview
                Texture terrainpreview = UpdateTerrainPreview();
                AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrainpreview"), terrainpreview);

                MapPreview.setTexture(Assets.getTexture("engine:terrainpreview"));

            }


            });

//Vertical spacing
        float offsethorz = 40f;
        float offsethorz2 = 5f;
//Horizontal Spacing
        float offsetone= -200f;     float offsetone2= -33f;
        float offsettwo= -100f;     float offsettwo2= -33f;
        float offsetthird = 100f;   float offsetthird2= -33f;
        float offsetfour = 200f;    float offsetfour2= -33f;

//Label Background is 640,400
        SIZE_XFACTORLabel.setPosition(new Vector2f(-320, 0));

//First Column
        BaseTerrainFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + BaseTerrainFACTORLabel.getText().length(), offsethorz));
        BaseTerrainFACTOR.setPosition(new Vector2f(offsetone, BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + offsethorz2));

        OceanTerrainFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + OceanTerrainFACTORLabel.getText().length() , BaseTerrainFACTOR.getPosition().y + BaseTerrainFACTOR.getSize().y + offsethorz2));
        OceanTerrainFACTOR.setPosition(new Vector2f(offsetone, OceanTerrainFACTORLabel.getPosition().y + OceanTerrainFACTORLabel.getSize().y + offsethorz2));

        RiverTerrainFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + BaseTerrainFACTORLabel.getText().length(), OceanTerrainFACTOR.getPosition().y + OceanTerrainFACTOR.getSize().y + offsethorz2));
        RiverTerrainFACTOR.setPosition(new Vector2f(offsetone, RiverTerrainFACTORLabel.getPosition().y + RiverTerrainFACTORLabel.getSize().y + offsethorz2));

        MountainFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + BaseTerrainFACTORLabel.getText().length(), RiverTerrainFACTOR.getPosition().y + RiverTerrainFACTOR.getSize().y + offsethorz2));
        MountainFACTOR.setPosition(new Vector2f(offsetone, MountainFACTORLabel.getPosition().y + MountainFACTORLabel.getSize().y + offsethorz2));

        HillDensityFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + BaseTerrainFACTORLabel.getText().length(), MountainFACTOR.getPosition().y + MountainFACTOR.getSize().y + offsethorz2));
        HillDensityFACTOR.setPosition(new Vector2f(offsetone, HillDensityFACTORLabel.getPosition().y + HillDensityFACTORLabel.getSize().y + offsethorz2));

        plateauAreaFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + BaseTerrainFACTORLabel.getText().length(), HillDensityFACTOR.getPosition().y + HillDensityFACTOR.getSize().y + offsethorz2));
        plateauAreaFACTOR.setPosition(new Vector2f(offsetone, plateauAreaFACTORLabel.getPosition().y + plateauAreaFACTORLabel.getSize().y +offsethorz2));

        caveDensityFACTORLabel.setPosition(new Vector2f(offsetone + offsetone2 + caveDensityFACTORLabel.getText().length(), plateauAreaFACTOR.getPosition().y + plateauAreaFACTOR.getSize().y + offsethorz2));
        caveDensityFACTOR.setPosition(new Vector2f(offsetone, caveDensityFACTORLabel.getPosition().y + caveDensityFACTORLabel.getSize().y + offsethorz2));

//Buttons referenced from bottom of column one
        OKButton.setPosition(new Vector2f(-100, caveDensityFACTOR.getPosition().y+80f));
        DeafaultsButton.setPosition(new Vector2f(OKButton.getPosition().x + 200f, OKButton.getPosition().y));

//Second Column
        WorldTitleFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + WorldTitleFACTORLabel.getText().length(), offsethorz));
        WorldTitleFACTOR.setPosition(new Vector2f(offsettwo+20, BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + offsethorz2));

        ForestGrassDensityFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + ForestGrassDensityFACTORLabel.getText().length(), BaseTerrainFACTOR.getPosition().y + BaseTerrainFACTOR.getSize().y + offsethorz2));
        ForestGrassDensityFACTOR.setPosition(new Vector2f(offsettwo, OceanTerrainFACTORLabel.getPosition().y + OceanTerrainFACTORLabel.getSize().y + offsethorz2));

        PlainsGrassDensityFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + PlainsGrassDensityFACTORLabel.getText().length(), OceanTerrainFACTOR.getPosition().y + OceanTerrainFACTOR.getSize().y + offsethorz2));
        PlainsGrassDensityFACTOR.setPosition(new Vector2f(offsettwo, RiverTerrainFACTORLabel.getPosition().y + RiverTerrainFACTORLabel.getSize().y + offsethorz2));

        SnowGrassDensityFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + SnowGrassDensityFACTORLabel.getText().length(), RiverTerrainFACTOR.getPosition().y + RiverTerrainFACTOR.getSize().y + offsethorz2));
        SnowGrassDensityFACTOR.setPosition(new Vector2f(offsettwo, MountainFACTORLabel.getPosition().y + MountainFACTORLabel.getSize().y + offsethorz2));

        MountainGrassDensityFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + MountainGrassDensityFACTORLabel.getText().length(), MountainFACTOR.getPosition().y + MountainFACTOR.getSize().y + offsethorz2));
        MountainGrassDensityFACTOR.setPosition(new Vector2f(offsettwo, HillDensityFACTORLabel.getPosition().y + HillDensityFACTORLabel.getSize().y + offsethorz2));

        DesertGrassDensityFACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + DesertGrassDensityFACTORLabel.getText().length(), HillDensityFACTOR.getPosition().y + HillDensityFACTOR.getSize().y + offsethorz2));
        DesertGrassDensityFACTOR.setPosition(new Vector2f(offsettwo, plateauAreaFACTORLabel.getPosition().y + plateauAreaFACTORLabel.getSize().y +offsethorz2));

         ZOOM_FACTORLabel.setPosition(new Vector2f(offsettwo + offsettwo2 + ZOOM_FACTORLabel.getText().length(), plateauAreaFACTOR.getPosition().y + plateauAreaFACTOR.getSize().y + offsethorz2));
         ZOOM_FACTOR.setPosition(new Vector2f(offsettwo+32, caveDensityFACTORLabel.getPosition().y + caveDensityFACTORLabel.getSize().y + offsethorz2));

//Third Column
        SAMPLE_RATE_3D_HORLabel.setPosition(new Vector2f(offsetthird + offsetthird2 + SAMPLE_RATE_3D_HORLabel.getText().length(), offsethorz));
        SAMPLE_RATE_3D_HORFACTOR.setPosition(new Vector2f(offsetthird, BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + offsethorz2));

        DefaultSeedFACTORLabel.setPosition(new Vector2f(offsetthird + offsetthird2 + DefaultSeedFACTORLabel.getText().length(),  BaseTerrainFACTOR.getPosition().y + BaseTerrainFACTOR.getSize().y + offsethorz2));
        DefaultSeedFACTOR.setPosition(new Vector2f(offsetthird+60, OceanTerrainFACTORLabel.getPosition().y + OceanTerrainFACTORLabel.getSize().y + offsethorz2));

        MapPreview.setPosition(new Vector2f(offsetthird+60, OceanTerrainFACTOR.getPosition().y + OceanTerrainFACTOR.getSize().y + offsethorz2));

        ReloadPreviewButton.setPosition(new Vector2f(offsetthird+60, plateauAreaFACTOR.getPosition().y + plateauAreaFACTOR.getSize().y + offsethorz2+50));

//Fourth Column
        SAMPLE_RATE_3D_VERTLabel.setPosition(new Vector2f(offsetfour + offsetfour2 + SAMPLE_RATE_3D_VERTLabel.getText().length(), offsethorz));
        SAMPLE_RATE_3D_VERTFACTOR.setPosition(new Vector2f(offsetfour , BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + offsethorz2));

//add to display
        addDisplayElement(BaseTerrainFACTORLabel);
        addDisplayElement(BaseTerrainFACTOR);

        addDisplayElement(OceanTerrainFACTORLabel);
        addDisplayElement(OceanTerrainFACTOR);

        addDisplayElement(RiverTerrainFACTORLabel);
        addDisplayElement(RiverTerrainFACTOR);

        addDisplayElement(MountainFACTORLabel);
        addDisplayElement(MountainFACTOR);

        addDisplayElement(HillDensityFACTORLabel);
        addDisplayElement(HillDensityFACTOR);

        addDisplayElement(plateauAreaFACTORLabel);
        addDisplayElement(plateauAreaFACTOR);

        addDisplayElement(caveDensityFACTORLabel);
        addDisplayElement(caveDensityFACTOR);
   //column 2
        addDisplayElement(WorldTitleFACTORLabel);
        addDisplayElement(WorldTitleFACTOR);

        addDisplayElement(DefaultSeedFACTORLabel);
        addDisplayElement(DefaultSeedFACTOR);

        addDisplayElement(ForestGrassDensityFACTORLabel);
        addDisplayElement(ForestGrassDensityFACTOR);

        addDisplayElement(PlainsGrassDensityFACTORLabel);
        addDisplayElement(PlainsGrassDensityFACTOR);

        addDisplayElement(SnowGrassDensityFACTORLabel);
        addDisplayElement(SnowGrassDensityFACTOR);

        addDisplayElement(MountainGrassDensityFACTORLabel);
        addDisplayElement(MountainGrassDensityFACTOR);

        addDisplayElement(DesertGrassDensityFACTORLabel);
        addDisplayElement(DesertGrassDensityFACTOR);

        addDisplayElement(ZOOM_FACTORLabel);
        addDisplayElement(ZOOM_FACTOR);


  //column 3
        addDisplayElement(SAMPLE_RATE_3D_HORLabel);
        addDisplayElement(SAMPLE_RATE_3D_HORFACTOR);

        addDisplayElement(SAMPLE_RATE_3D_VERTLabel);
        addDisplayElement(SAMPLE_RATE_3D_VERTFACTOR);

  //version text
        addDisplayElement(SIZE_XFACTORLabel);

  //buttons
        addDisplayElement(OKButton);
        addDisplayElement(DeafaultsButton);
        addDisplayElement(ReloadPreviewButton);

  //preview

        addDisplayElement(MapPreview);


       layout();
}
 public Texture UpdateTerrainPreview(){

      final TerrainPreviewGenerator gen = new TerrainPreviewGenerator(DefaultSeedFACTOR.getText());
      //create the image
      gen.generateMap(org.terasology.world.TerrainPreviewGenerator.MapStyle.BIOMES, "Biomes.png");
      //write the image to texture
      ByteBuffer[] data = new ByteBuffer[2];

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      try {
          ImageIO.write(gen.image, "png", bos);
          PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(bos.toByteArray()));
          ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
          decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
          buf.flip();
          buf.rewind();
          data[1] = buf;
      } catch (IOException e) {
          //logger.error("Failed to create atlas texture");
      }

     return new Texture(data, 512, 512, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);

     //SIZE_XFACTORLabel.setText(String.valueOf(terrainpreview.getWidth()));
  }
}