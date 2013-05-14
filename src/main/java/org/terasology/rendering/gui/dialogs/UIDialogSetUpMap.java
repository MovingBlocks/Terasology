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

import org.newdawn.slick.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
//MPratt "Map Gen Setup" start
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.lwjgl.input.Keyboard;
//MPratt "Map Gen Setup" end
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.world.chunks.Chunk;

import javax.vecmath.Vector2f;


/*
 * Dialog for map setup
 *
 * @author MPratt "Map Gen Setup" <marsmod@hotmail.com>
 * @version 0.1
 */

public class UIDialogSetUpMap extends UIDialog {

    private UIButton OKButton;

    private UILabel BaseTerrainFACTORLabel;
    private UIText BaseTerrainFACTOR;

    private UILabel OceanTerrainFACTORLabel;
    private UIText OceanTerrainFACTOR;

    private UILabel RiverTerrainFACTORLabel;
    private UIText RiverTerrainFACTOR;

    private UILabel MountainFACTORLabel;
    private UIText MountainFACTOR;

    private UILabel HillDensityFACTORLabel;
    private UIText HillDensityFACTOR;

    private UILabel plateauAreaFACTORLabel;
    private UIText plateauAreaFACTOR;

    private UILabel caveDensityFACTORLabel;
    private UIText caveDensityFACTOR;

    private UILabel SIZE_XFACTORLabel;
    private UIText SIZE_XFACTOR;

    private UILabel SIZE_YFACTORLabel;
    private UIText SIZE_YFACTOR;

    private UILabel SIZE_ZFACTORLabel;
    private UIText SIZE_ZFACTOR;

    private static final Logger logger = LoggerFactory.getLogger(UIDialogSetUpMap.class);

	private boolean troubleshoot= false;

    public UIDialogSetUpMap() {

        super(new Vector2f(512f, 480f));
        setTitle("Map Setup");

    }

    @Override
    protected void createDialogArea(final UIDisplayContainer parent) {
        Config config = CoreRegistry.get(Config.class);
        CoreRegistry.get(Config.class).getDefaultModSelection();
        CoreRegistry.get(Config.class).save();

        BaseTerrainFACTOR = new UIText();
        BaseTerrainFACTOR.setSize(new Vector2f(60f, 30f));
        BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        BaseTerrainFACTOR.setVisible(true);
        BaseTerrainFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        BaseTerrainFACTOR.setSelectionColor(Color.red);
        BaseTerrainFACTOR.setText("1");
        BaseTerrainFACTOR.setText(config.getWorldGeneration().getBaseTerrainFACTOR());
        BaseTerrainFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setBaseTerrainFACTOR(BaseTerrainFACTOR.getText());
                        BaseTerrainFACTOR.setText(config.getWorldGeneration().getBaseTerrainFACTOR());
                        if(troubleshoot) logger.error("value of BaseTerrainFACTOR GUI = " + BaseTerrainFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

        OceanTerrainFACTOR = new UIText();
        OceanTerrainFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        OceanTerrainFACTOR.setVisible(true);
        OceanTerrainFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        OceanTerrainFACTOR.setSelectionColor(Color.red);
        OceanTerrainFACTOR.setText("1");
        OceanTerrainFACTOR.setText(config.getWorldGeneration().getOceanTerrainFACTOR());
        OceanTerrainFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setBaseTerrainFACTOR(OceanTerrainFACTOR.getText());
                        OceanTerrainFACTOR.setText(config.getWorldGeneration().getOceanTerrainFACTOR());
                        if(troubleshoot) logger.error("value of OceanTerrainFACTOR GUI = " + OceanTerrainFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });
        RiverTerrainFACTOR = new UIText();
        RiverTerrainFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        RiverTerrainFACTOR.setVisible(true);
        RiverTerrainFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        RiverTerrainFACTOR.setSelectionColor(Color.red);
        RiverTerrainFACTOR.setText("1");
        RiverTerrainFACTOR.setText(config.getWorldGeneration().getRiverTerrainFACTOR());
        RiverTerrainFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setRiverTerrainFACTOR(RiverTerrainFACTOR.getText());
                        RiverTerrainFACTOR.setText(config.getWorldGeneration().getRiverTerrainFACTOR());
                        if(troubleshoot) logger.error("value of RiverTerrainFACTOR GUI = " + RiverTerrainFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

        MountainFACTOR = new UIText();
        MountainFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        MountainFACTOR.setVisible(true);
        MountainFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        MountainFACTOR.setSelectionColor(Color.red);
        MountainFACTOR.setText("1");
        MountainFACTOR.setText(config.getWorldGeneration().getMountainFACTOR());
        MountainFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setMountainFACTOR(MountainFACTOR.getText());
                        MountainFACTOR.setText(config.getWorldGeneration().getMountainFACTOR());
                        if(troubleshoot) logger.error("value of MountainFACTOR GUI = " + MountainFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });
        HillDensityFACTOR = new UIText();
        HillDensityFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        HillDensityFACTOR.setVisible(true);
        HillDensityFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        HillDensityFACTOR.setSelectionColor(Color.red);
        HillDensityFACTOR.setText("1");
        HillDensityFACTOR.setText(config.getWorldGeneration().getHillDensityFACTOR());
        HillDensityFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setHillDensityFACTOR(HillDensityFACTOR.getText());
                        HillDensityFACTOR.setText(config.getWorldGeneration().getHillDensityFACTOR());
                        if(troubleshoot) logger.error("value of HillDensityFACTOR GUI = " + HillDensityFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });
        plateauAreaFACTOR = new UIText();
        plateauAreaFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        plateauAreaFACTOR.setVisible(true);
        plateauAreaFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        plateauAreaFACTOR.setSelectionColor(Color.red);
        plateauAreaFACTOR.setText("1");
        plateauAreaFACTOR.setText(config.getWorldGeneration().getplateauAreaFACTOR());
        plateauAreaFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setBaseTerrainFACTOR(plateauAreaFACTOR.getText());
                        plateauAreaFACTOR.setText(config.getWorldGeneration().getBaseTerrainFACTOR());
                        if(troubleshoot) logger.error("value of plateauAreaFACTOR GUI = " + plateauAreaFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

        caveDensityFACTOR = new UIText();
        caveDensityFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        caveDensityFACTOR.setVisible(true);
        caveDensityFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        caveDensityFACTOR.setSelectionColor(Color.red);
        caveDensityFACTOR.setText("1");
        caveDensityFACTOR.setText(config.getWorldGeneration().getcaveDensityFACTOR());
        caveDensityFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        config.getWorldGeneration().setcaveDensityFACTOR(caveDensityFACTOR.getText());
                        caveDensityFACTOR.setText(config.getWorldGeneration().getcaveDensityFACTOR());
                        if(troubleshoot) logger.error("value of caveDensityFACTOR GUI = " + caveDensityFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

        SIZE_XFACTOR = new UIText();
        SIZE_XFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        SIZE_XFACTOR.setVisible(true);
        SIZE_XFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
        SIZE_XFACTOR.setSelectionColor(Color.red);
        SIZE_XFACTOR.setText("16");
        SIZE_XFACTOR.setText( Integer.toString(Chunk.SIZE_X));
        SIZE_XFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        SIZE_XFACTOR.setText( Integer.toString(Chunk.SIZE_X));
                        Chunk.SIZE_X=Integer.parseInt(SIZE_XFACTOR.getText());
                        if(troubleshoot) logger.error("value of SIZE_XFACTOR GUI = " + SIZE_XFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

       SIZE_YFACTOR = new UIText();
       SIZE_YFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
       SIZE_YFACTOR.setVisible(true);
       SIZE_YFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
       SIZE_YFACTOR.setSelectionColor(Color.red);
       SIZE_YFACTOR.setText( Integer.toString(Chunk.SIZE_Y));
       SIZE_YFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        SIZE_YFACTOR.setText( Integer.toString(Chunk.SIZE_Y));
                        Chunk.SIZE_Y=Integer.parseInt(SIZE_YFACTOR.getText());
                        if(troubleshoot) logger.error("value of SIZE_YFACTOR GUI = " + SIZE_YFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

       SIZE_ZFACTOR = new UIText();
       SIZE_ZFACTOR.setSize(new Vector2f(60f, 30f));
        //BaseTerrainFACTOR.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
       SIZE_ZFACTOR.setVisible(true);
       SIZE_ZFACTOR.setBackgroundColor(new Color(55, 255, 255, 200));
       SIZE_ZFACTOR.setSelectionColor(Color.red);
       SIZE_ZFACTOR.setText("16");
       SIZE_ZFACTOR.setText( Integer.toString(Chunk.SIZE_Z));
       SIZE_ZFACTOR.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        SIZE_ZFACTOR.setText( Integer.toString(Chunk.SIZE_Z));
                        Chunk.SIZE_Z=Integer.parseInt(SIZE_ZFACTOR.getText());
                        if(troubleshoot) logger.error("value of SIZE_ZFACTOR GUI = " + SIZE_ZFACTOR.getText());

                    }  else if (event.getKey() == Keyboard.KEY_TAB) {
//MPratt "Map Gen Setup" TODO Change focus
                    }
                }
            }

        });

        BaseTerrainFACTORLabel = new UILabel("Base Terrain 0-100:");
        BaseTerrainFACTORLabel.setColor(Color.darkGray);
        BaseTerrainFACTORLabel.setSize(new Vector2f(0f, 12f));
        BaseTerrainFACTORLabel.setVisible(true);

        OceanTerrainFACTORLabel = new UILabel("Ocean Terrain 0-100:");
        OceanTerrainFACTORLabel.setColor(Color.darkGray);
        OceanTerrainFACTORLabel.setSize(new Vector2f(0f, 12f));
        OceanTerrainFACTORLabel.setVisible(true);

        RiverTerrainFACTORLabel = new UILabel("River Terrain 0-100:");
        RiverTerrainFACTORLabel.setColor(Color.darkGray);
        RiverTerrainFACTORLabel.setSize(new Vector2f(0f, 12f));
        RiverTerrainFACTORLabel.setVisible(true);

        MountainFACTORLabel = new UILabel("Mountain 0-100:");
        MountainFACTORLabel.setColor(Color.darkGray);
        MountainFACTORLabel.setSize(new Vector2f(0f, 12f));
        MountainFACTORLabel.setVisible(true);

        HillDensityFACTORLabel = new UILabel("Hill Density 0-100:");
        HillDensityFACTORLabel.setColor(Color.darkGray);
        HillDensityFACTORLabel.setSize(new Vector2f(0f, 12f));
        HillDensityFACTORLabel.setVisible(true);

        plateauAreaFACTORLabel = new UILabel("Plateau Area 0-100:");
        plateauAreaFACTORLabel.setColor(Color.darkGray);
        plateauAreaFACTORLabel.setSize(new Vector2f(0f, 12f));
        plateauAreaFACTORLabel.setVisible(true);

        caveDensityFACTORLabel = new UILabel("Cave Density 0-100:");
        caveDensityFACTORLabel.setColor(Color.darkGray);
        caveDensityFACTORLabel.setSize(new Vector2f(0f, 12f));
        caveDensityFACTORLabel.setVisible(true);

        SIZE_XFACTORLabel = new UILabel("Chunk Size X 0-256:");
        SIZE_XFACTORLabel.setColor(Color.darkGray);
        SIZE_XFACTORLabel.setSize(new Vector2f(0f, 12f));
        SIZE_XFACTORLabel.setVisible(true);

        SIZE_YFACTORLabel = new UILabel("Chunk Size Y  0-256:");
        SIZE_YFACTORLabel.setColor(Color.darkGray);
        SIZE_YFACTORLabel.setSize(new Vector2f(0f, 12f));
        SIZE_YFACTORLabel.setVisible(true);

        SIZE_ZFACTORLabel = new UILabel("Chunk Size Z  0-256:");
        SIZE_ZFACTORLabel.setColor(Color.darkGray);
        SIZE_ZFACTORLabel.setSize(new Vector2f(0f, 12f));
        SIZE_ZFACTORLabel.setVisible(true);

        BaseTerrainFACTORLabel.setPosition(new Vector2f(15f, 48f));
        BaseTerrainFACTOR.setPosition(new Vector2f(BaseTerrainFACTORLabel.getPosition().x, BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + 6f));

        OceanTerrainFACTORLabel.setPosition(new Vector2f(BaseTerrainFACTOR.getPosition().x, BaseTerrainFACTOR.getPosition().y + BaseTerrainFACTOR.getSize().y + 6f));
        OceanTerrainFACTOR.setPosition(new Vector2f(OceanTerrainFACTORLabel.getPosition().x, OceanTerrainFACTORLabel.getPosition().y + OceanTerrainFACTORLabel.getSize().y + 6f));

        RiverTerrainFACTORLabel.setPosition(new Vector2f(OceanTerrainFACTOR.getPosition().x, OceanTerrainFACTOR.getPosition().y + OceanTerrainFACTOR.getSize().y + 6f));
        RiverTerrainFACTOR.setPosition(new Vector2f(RiverTerrainFACTORLabel.getPosition().x, RiverTerrainFACTORLabel.getPosition().y + RiverTerrainFACTORLabel.getSize().y + 6f));

        MountainFACTORLabel.setPosition(new Vector2f(RiverTerrainFACTOR.getPosition().x, RiverTerrainFACTOR.getPosition().y + RiverTerrainFACTOR.getSize().y + 6f));
        MountainFACTOR.setPosition(new Vector2f(MountainFACTORLabel.getPosition().x, MountainFACTORLabel.getPosition().y + MountainFACTORLabel.getSize().y + 6f));

        HillDensityFACTORLabel.setPosition(new Vector2f(MountainFACTOR.getPosition().x, MountainFACTOR.getPosition().y + MountainFACTOR.getSize().y + 6f));
        HillDensityFACTOR.setPosition(new Vector2f(HillDensityFACTORLabel.getPosition().x, HillDensityFACTORLabel.getPosition().y + HillDensityFACTORLabel.getSize().y + 6f));

        plateauAreaFACTORLabel.setPosition(new Vector2f(HillDensityFACTOR.getPosition().x, HillDensityFACTOR.getPosition().y + HillDensityFACTOR.getSize().y + 6f));
        plateauAreaFACTOR.setPosition(new Vector2f(plateauAreaFACTORLabel.getPosition().x, plateauAreaFACTORLabel.getPosition().y + plateauAreaFACTORLabel.getSize().y +6f));

        caveDensityFACTORLabel.setPosition(new Vector2f(plateauAreaFACTOR.getPosition().x, plateauAreaFACTOR.getPosition().y + plateauAreaFACTOR.getSize().y + 6f));
        caveDensityFACTOR.setPosition(new Vector2f(caveDensityFACTORLabel.getPosition().x, caveDensityFACTORLabel.getPosition().y + caveDensityFACTORLabel.getSize().y + 6f));

		SIZE_XFACTORLabel.setPosition(new Vector2f(15f +250f, 48f));
        SIZE_XFACTOR.setPosition(new Vector2f(BaseTerrainFACTORLabel.getPosition().x +250f, BaseTerrainFACTORLabel.getPosition().y + BaseTerrainFACTORLabel.getSize().y + 6f));

        SIZE_YFACTORLabel.setPosition(new Vector2f(BaseTerrainFACTOR.getPosition().x +250f, BaseTerrainFACTOR.getPosition().y + BaseTerrainFACTOR.getSize().y + 6f));
        SIZE_YFACTOR.setPosition(new Vector2f(OceanTerrainFACTORLabel.getPosition().x +250f, OceanTerrainFACTORLabel.getPosition().y + OceanTerrainFACTORLabel.getSize().y + 6f));

        SIZE_ZFACTORLabel.setPosition(new Vector2f(OceanTerrainFACTOR.getPosition().x +250f, OceanTerrainFACTOR.getPosition().y + OceanTerrainFACTOR.getSize().y + 6f));
        SIZE_ZFACTOR.setPosition(new Vector2f(RiverTerrainFACTORLabel.getPosition().x +250f, RiverTerrainFACTORLabel.getPosition().y + RiverTerrainFACTORLabel.getSize().y + 6f));

        parent.addDisplayElement(BaseTerrainFACTORLabel);
        parent.addDisplayElement(BaseTerrainFACTOR);

        parent.addDisplayElement(OceanTerrainFACTORLabel);
        parent.addDisplayElement(OceanTerrainFACTOR);

        parent.addDisplayElement(RiverTerrainFACTORLabel);
        parent.addDisplayElement(RiverTerrainFACTOR);

        parent.addDisplayElement(MountainFACTORLabel);
        parent.addDisplayElement(MountainFACTOR);

        parent.addDisplayElement(HillDensityFACTORLabel);
        parent.addDisplayElement(HillDensityFACTOR);

        parent.addDisplayElement(plateauAreaFACTORLabel);
        parent.addDisplayElement(plateauAreaFACTOR);

        parent.addDisplayElement(caveDensityFACTORLabel);
        parent.addDisplayElement(caveDensityFACTOR);

        parent.addDisplayElement(SIZE_XFACTORLabel);
        parent.addDisplayElement(SIZE_XFACTOR);
		
        parent.addDisplayElement(SIZE_YFACTORLabel);
        parent.addDisplayElement(SIZE_YFACTOR);
		
        parent.addDisplayElement(SIZE_ZFACTORLabel);
        parent.addDisplayElement(SIZE_ZFACTOR);

        parent.layout();
    }

        protected void createButtons(UIDisplayContainer parent) {

        OKButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        OKButton.setPosition(new Vector2f(caveDensityFACTOR.getPosition().x + caveDensityFACTOR.getSize().x + 16f, caveDensityFACTOR.getPosition().y));
        OKButton.getLabel().setText(" OK ");
        OKButton.setVisible(true);

        OKButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

                Config config = CoreRegistry.get(Config.class);
                CoreRegistry.get(Config.class).getDefaultModSelection();
                CoreRegistry.get(Config.class).save();

//MPratt "Map Gen Setup" TODO actually validate

                if (BaseTerrainFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setBaseTerrainFACTOR(BaseTerrainFACTOR.getText());
                    BaseTerrainFACTOR.setText(config.getWorldGeneration().getBaseTerrainFACTOR());
                    if(troubleshoot) logger.error("clicked OK  BaseTerrainFACTOR");
                }
                if (OceanTerrainFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setOceanTerrainFACTOR(OceanTerrainFACTOR.getText());
                    OceanTerrainFACTOR.setText(config.getWorldGeneration().getOceanTerrainFACTOR());
                    if(troubleshoot) logger.error("clicked OK OceanTerrainFACTOR");
                }
                if (RiverTerrainFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setRiverTerrainFACTOR(RiverTerrainFACTOR.getText());
                    RiverTerrainFACTOR.setText(config.getWorldGeneration().getRiverTerrainFACTOR());
                    if(troubleshoot) logger.error("clicked OK RiverTerrainFACTOR");
                }
                if (MountainFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setMountainFACTOR(MountainFACTOR.getText());
                    MountainFACTOR.setText(config.getWorldGeneration().getMountainFACTOR());
                    if(troubleshoot) logger.error("clicked OK MountainFACTOR");
                }
                if (HillDensityFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setHillDensityFACTOR(HillDensityFACTOR.getText());
                    HillDensityFACTOR.setText(config.getWorldGeneration().getHillDensityFACTOR());
                    if(troubleshoot) logger.error("clicked OK HillDensityFACTOR");
                }
                if (plateauAreaFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setplateauAreaFACTOR(plateauAreaFACTOR.getText());
                    plateauAreaFACTOR.setText(config.getWorldGeneration().getplateauAreaFACTOR());
                    if(troubleshoot) logger.error("clicked OK plateauAreaFACTOR");
                }
                if (caveDensityFACTOR.getText().length() > 0) {
                    config.getWorldGeneration().setcaveDensityFACTOR(caveDensityFACTOR.getText());
                    caveDensityFACTOR.setText(config.getWorldGeneration().getcaveDensityFACTOR());
                    if(troubleshoot) logger.error("clicked OK caveDensityFACTOR");
                }
                if (SIZE_XFACTOR.getText().length() > 0) {
                    Chunk.SIZE_X=Integer.parseInt(SIZE_XFACTOR.getText());
                    if(troubleshoot) logger.error("value of SIZE_XFACTOR GUI = " + SIZE_XFACTOR.getText());
                }
				 if (SIZE_YFACTOR.getText().length() > 0) {
                     Chunk.SIZE_Y=Integer.parseInt(SIZE_YFACTOR.getText());
                     if(troubleshoot) logger.error("value of SIZE_YFACTOR GUI = " + SIZE_YFACTOR.getText());
                }
				if (SIZE_ZFACTOR.getText().length() > 0) {
                    Chunk.SIZE_Z=Integer.parseInt(SIZE_ZFACTOR.getText());
                    if(troubleshoot) logger.error("value of SIZE_ZFACTOR GUI = " + SIZE_ZFACTOR.getText());
                }

                close();
            }
        });

        parent.addDisplayElement(OKButton);
    }


}
