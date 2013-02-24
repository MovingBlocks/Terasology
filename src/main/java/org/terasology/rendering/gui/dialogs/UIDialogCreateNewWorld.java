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
import org.terasology.config.Config;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateLoading;
import org.terasology.game.types.FreeStyleType;
import org.terasology.game.types.GameType;
import org.terasology.game.types.SurvivalType;
import org.terasology.logic.manager.PathManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComboBox;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.windows.UIMenuSingleplayer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldInfo;
import org.terasology.world.generator.core.BasicHMTerrainGenerator;
import org.terasology.world.generator.core.FlatTerrainGenerator;
import org.terasology.world.generator.core.FloraGenerator;
import org.terasology.world.generator.core.ForestGenerator;
import org.terasology.world.generator.core.MultiTerrainGenerator;
import org.terasology.world.generator.core.PerlinTerrainGenerator;
import org.terasology.world.liquid.LiquidsGenerator;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Dialog for generate new world
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */

public class UIDialogCreateNewWorld extends UIDialog {
    private UIButton okButton;
    private UIButton cancelButton;

    private UILabel inputSeedLabel;
    private UIText inputSeed;
    private UILabel inputWorldTitleLabel;
    private UIText inputWorldTitle;
    private UILabel chunkGeneratorLabel;
    private UIComboBox chunkGenerator;
    private UIComboBox typeOfGame;
    private UILabel typeOfGameLabel;

    private ModConfig modConfig;

    public UIDialogCreateNewWorld() {
        super(new Vector2f(512f, 380f));
        setTitle("Create new world");

        modConfig = new ModConfig();
        modConfig.copy(CoreRegistry.get(Config.class).getDefaultModSelection());
    }

    @Override
    protected void createDialogArea(UIDisplayContainer parent) {
        inputSeed = new UIText();
        inputSeed.setSize(new Vector2f(256f, 30f));
        //inputSeed.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        inputSeed.setVisible(true);

        inputWorldTitle = new UIText();
        inputWorldTitle.setSize(new Vector2f(256f, 30f));
        //inputWorldTitle.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        inputWorldTitle.setText(getWorldName());
        inputWorldTitle.setVisible(true);

        inputSeedLabel = new UILabel("Enter a seed (optional):");
        inputSeedLabel.setColor(Color.darkGray);
        inputSeedLabel.setSize(new Vector2f(0f, 16f));
        inputSeedLabel.setVisible(true);

        inputWorldTitleLabel = new UILabel("Enter a world name:");
        inputWorldTitleLabel.setColor(Color.darkGray);
        inputWorldTitleLabel.setSize(new Vector2f(0f, 16f));
        inputWorldTitleLabel.setVisible(true);

        typeOfGameLabel = new UILabel("Choose type of game:");
        typeOfGameLabel.setColor(Color.darkGray);
        typeOfGameLabel.setSize(new Vector2f(0f, 16f));
        typeOfGameLabel.setVisible(true);

        typeOfGame = new UIComboBox(new Vector2f(176f, 22f), new Vector2f(176f, 50f));
        UIListItem item = new UIListItem(new SurvivalType().getName(), new SurvivalType());
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        typeOfGame.addItem(item);

        item = new UIListItem(new FreeStyleType().getName(), new FreeStyleType());
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        typeOfGame.addItem(item);
        typeOfGame.select(0);
        typeOfGame.setVisible(true);

        chunkGeneratorLabel = new UILabel("Choose Chunk Generator:");
        chunkGeneratorLabel.setColor(Color.darkGray);
        chunkGeneratorLabel.setSize(new Vector2f(0f, 16f));
        chunkGeneratorLabel.setVisible(true);

        chunkGenerator = new UIComboBox(new Vector2f(176f, 22f), new Vector2f(176f, 48f));
        item = new UIListItem("Perlin", new Integer(0));
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        chunkGenerator.addItem(item);
        item = new UIListItem("Flat", new Integer(1));
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        chunkGenerator.addItem(item);
        item = new UIListItem("Multi", new Integer(2));
        item.setTextColor(Color.cyan);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        chunkGenerator.addItem(item);
        item = new UIListItem("Heigthmap Generator", new Integer(3));
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        chunkGenerator.addItem(item);
        chunkGenerator.select(0);
        chunkGenerator.setVisible(true);


        inputWorldTitleLabel.setPosition(new Vector2f(15f, 32f));
        inputWorldTitle.setPosition(new Vector2f(inputWorldTitleLabel.getPosition().x, inputWorldTitleLabel.getPosition().y + inputWorldTitleLabel.getSize().y + 8f));
        inputSeedLabel.setPosition(new Vector2f(inputWorldTitle.getPosition().x, inputWorldTitle.getPosition().y + inputWorldTitle.getSize().y + 16f));
        inputSeed.setPosition(new Vector2f(inputSeedLabel.getPosition().x, inputSeedLabel.getPosition().y + inputSeedLabel.getSize().y + 8f));

        typeOfGameLabel.setPosition(new Vector2f(inputSeed.getPosition().x, inputSeed.getPosition().y + inputSeed.getSize().y + 16f));
        typeOfGame.setPosition(new Vector2f(typeOfGameLabel.getPosition().x, typeOfGameLabel.getPosition().y + typeOfGameLabel.getSize().y + 8f));

        chunkGeneratorLabel.setPosition(new Vector2f(typeOfGame.getPosition().x, typeOfGame.getPosition().y + typeOfGame.getSize().y + 16f));
        chunkGenerator.setPosition(new Vector2f(chunkGeneratorLabel.getPosition().x, chunkGeneratorLabel.getPosition().y + chunkGeneratorLabel.getSize().y + 8f));


        UIButton modButton = new UIButton(new Vector2f(80, 30), UIButton.ButtonType.NORMAL);
        modButton.setPosition(new Vector2f(chunkGenerator.getPosition().x, chunkGenerator.getPosition().y + chunkGenerator.getSize().y + 58f));
        modButton.setVisible(true);
        modButton.getLabel().setText("Mods...");
        modButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIDialogMods dialog = new UIDialogMods(modConfig);
                dialog.open();
            }
        });
        parent.addDisplayElement(inputWorldTitleLabel);
        parent.addDisplayElement(inputWorldTitle);
        parent.addDisplayElement(inputSeedLabel);
        parent.addDisplayElement(inputSeed);
        parent.addDisplayElement(chunkGeneratorLabel);
        parent.addDisplayElement(chunkGenerator);
        parent.addDisplayElement(typeOfGame);
        parent.addDisplayElement(typeOfGameLabel);
        parent.addDisplayElement(modButton);
        parent.layout();
    }

    @Override
    protected void createButtons(UIDisplayContainer parent) {
        okButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        okButton.getLabel().setText("Play");
        okButton.setPosition(new Vector2f(getSize().x / 2 - okButton.getSize().x - 16f, getSize().y - okButton.getSize().y - 10));
        okButton.setVisible(true);

        okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                Config config = CoreRegistry.get(Config.class);

                //validation of the input
                if (inputWorldTitle.getText().isEmpty()) {
                    getGUIManager().showMessage("Error", "Please enter a world name");

                    return;
                } else if ((new File(PathManager.getInstance().getWorldSavePath(inputWorldTitle.getText()), WorldInfo.DEFAULT_FILE_NAME)).exists()) {
                    getGUIManager().showMessage("Error", "A World with this name already exists");

                    return;
                }

                CoreRegistry.put(GameType.class, (GameType) typeOfGame.getSelection().getValue());

                //set the world settings
                if (inputSeed.getText().length() > 0) {
                    config.getWorldGeneration().setDefaultSeed(inputSeed.getText());
                } else {
                    FastRandom random = new FastRandom();
                    config.getWorldGeneration().setDefaultSeed(random.randomCharacterString(32));
                }

                if (inputWorldTitle.getText().length() > 0) {
                    config.getWorldGeneration().setWorldTitle(inputWorldTitle.getText());
                } else {
                    config.getWorldGeneration().setWorldTitle(getWorldName());
                }

                List<String> chunkList = new ArrayList<String>();
                switch (chunkGenerator.getSelectionIndex()) {
                    case 1:   //flat
                        chunkList.add(FlatTerrainGenerator.class.getName());
                        //if (checkboxFlora == selected) ... (pseudo code)
                        chunkList.add(FloraGenerator.class.getName());
                        chunkList.add(LiquidsGenerator.class.getName());
                        chunkList.add(ForestGenerator.class.getName());
                        break;

                    case 2:   //multiworld
                        chunkList.add(MultiTerrainGenerator.class.getName());
                        chunkList.add(FloraGenerator.class.getName());
                        chunkList.add(LiquidsGenerator.class.getName());
                        chunkList.add(ForestGenerator.class.getName());
                        break;
                    case 3:   //Nym
                        chunkList.add(BasicHMTerrainGenerator.class.getName());
                        chunkList.add(FloraGenerator.class.getName());
                        chunkList.add(LiquidsGenerator.class.getName());
                        chunkList.add(ForestGenerator.class.getName());
                        break;

                    default:  //normal
                        chunkList.add(PerlinTerrainGenerator.class.getName());
                        chunkList.add(FloraGenerator.class.getName());
                        chunkList.add(LiquidsGenerator.class.getName());
                        chunkList.add(ForestGenerator.class.getName());
                        break;
                }

                String[] chunksListArr = chunkList.toArray(new String[chunkList.size()]);
                CoreRegistry.get(Config.class).getDefaultModSelection().copy(modConfig);
                CoreRegistry.get(Config.class).save();

                CoreRegistry.get(GameEngine.class).changeState(new StateLoading(new WorldInfo(config.getWorldGeneration().getWorldTitle(), config.getWorldGeneration().getDefaultSeed(), config.getSystem().getDayNightLengthInMs() / 4, chunksListArr, CoreRegistry.get(GameType.class).getClass().toString(), modConfig)));
            }
        });


        cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        cancelButton.setPosition(new Vector2f(okButton.getPosition().x + okButton.getSize().x + 16f, okButton.getPosition().y));
        cancelButton.getLabel().setText("Cancel");
        cancelButton.setVisible(true);

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });

        parent.addDisplayElement(okButton);
        parent.addDisplayElement(cancelButton);
    }

    private String getWorldName() {
        UIMenuSingleplayer menu = (UIMenuSingleplayer) getGUIManager().getWindowById("singleplayer");
        return "World" + (menu.getWorldCount() + 1);
    }
}
