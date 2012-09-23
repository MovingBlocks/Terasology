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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateSinglePlayer;
import org.terasology.game.types.FreeStyleType;
import org.terasology.game.types.GameType;
import org.terasology.game.types.SurvivalType;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.*;
import org.terasology.rendering.gui.windows.UIMenuSingleplayer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldInfo;
import org.terasology.world.generator.core.FlatTerrainGenerator;
import org.terasology.world.generator.core.FloraGenerator;
import org.terasology.world.generator.core.ForestGenerator;
import org.terasology.world.generator.core.PerlinTerrainGenerator;
import org.terasology.world.liquid.LiquidsGenerator;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/*
 * Dialog for generate new world
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */

public class UIDialogCreateNewWorld extends UIDialog {
    private UIButton _okButton;
    private UIButton _cancelButton;

    private UILabel _inputSeedLabel;
    private UIText _inputSeed;
    private UILabel _inputWorldTitleLabel;
    private UIText _inputWorldTitle;
    private UILabel _chunkGeneratorLabel;
    private UIComboBox _chunkGenerator;
    private UIComboBox typeOfGame;
    private UILabel typeOfGameLabel;

    public UIDialogCreateNewWorld() {
        super(new Vector2f(512f, 356f));
        setTitle("Create new world");
    }
    
    @Override
    protected void createDialogArea(UIDisplayContainer parent) {
        _inputSeed = new UIText();
        _inputSeed.setSize(new Vector2f(256f, 30f));
        //_inputSeed.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        _inputSeed.setVisible(true);

        _inputWorldTitle = new UIText();
        _inputWorldTitle.setSize(new Vector2f(256f, 30f));
        //_inputWorldTitle.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        _inputWorldTitle.setText(getWorldName());
        _inputWorldTitle.setVisible(true);

        _inputSeedLabel = new UILabel("Enter a seed (optional):");
        _inputSeedLabel.setColor(Color.darkGray);
        _inputSeedLabel.setSize(new Vector2f(0f, 16f));
        _inputSeedLabel.setVisible(true);

        _inputWorldTitleLabel = new UILabel("Enter a world name:");
        _inputWorldTitleLabel.setColor(Color.darkGray);
        _inputWorldTitleLabel.setSize(new Vector2f(0f, 16f));
        _inputWorldTitleLabel.setVisible(true);

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

        _chunkGeneratorLabel = new UILabel("Choose Chunk Generator:");
        _chunkGeneratorLabel.setColor(Color.darkGray);
        _chunkGeneratorLabel.setSize(new Vector2f(0f, 16f));
        _chunkGeneratorLabel.setVisible(true);

        _chunkGenerator = new UIComboBox(new Vector2f(176f, 22f), new Vector2f(176f, 48f));
        item = new UIListItem("Normal", new Integer(0));
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        _chunkGenerator.addItem(item);
        item = new UIListItem("Flat", new Integer(1));
        item.setTextColor(Color.black);
        item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        _chunkGenerator.addItem(item);
        _chunkGenerator.select(0);
        _chunkGenerator.setVisible(true);


        _inputWorldTitleLabel.setPosition(new Vector2f(15f, 32f));
        _inputWorldTitle.setPosition(new Vector2f(_inputWorldTitleLabel.getPosition().x, _inputWorldTitleLabel.getPosition().y + _inputWorldTitleLabel.getSize().y + 8f));
        _inputSeedLabel.setPosition(new Vector2f(_inputWorldTitle.getPosition().x, _inputWorldTitle.getPosition().y + _inputWorldTitle.getSize().y + 16f));
        _inputSeed.setPosition(new Vector2f(_inputSeedLabel.getPosition().x, _inputSeedLabel.getPosition().y + _inputSeedLabel.getSize().y + 8f));

        typeOfGameLabel.setPosition(new Vector2f(_inputSeed.getPosition().x, _inputSeed.getPosition().y + _inputSeed.getSize().y + 16f));
        typeOfGame.setPosition(new Vector2f(typeOfGameLabel.getPosition().x, typeOfGameLabel.getPosition().y + typeOfGameLabel.getSize().y + 8f));

        _chunkGeneratorLabel.setPosition(new Vector2f(typeOfGame.getPosition().x, typeOfGame.getPosition().y + typeOfGame.getSize().y + 16f));
        _chunkGenerator.setPosition(new Vector2f(_chunkGeneratorLabel.getPosition().x, _chunkGeneratorLabel.getPosition().y + _chunkGeneratorLabel.getSize().y + 8f));
        
        parent.addDisplayElement(_inputWorldTitleLabel);
        parent.addDisplayElement(_inputWorldTitle);
        parent.addDisplayElement(_inputSeedLabel);
        parent.addDisplayElement(_inputSeed);
        parent.addDisplayElement(_chunkGeneratorLabel);
        parent.addDisplayElement(_chunkGenerator);
        parent.addDisplayElement(typeOfGame);
        parent.addDisplayElement(typeOfGameLabel);
    }
    
    @Override
    protected void createButtons(UIDisplayContainer parent) {
        _okButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _okButton.getLabel().setText("Play");
        _okButton.setPosition(new Vector2f(getSize().x / 2 - _okButton.getSize().x - 16f, getSize().y - _okButton.getSize().y - 10));
        _okButton.setVisible(true);

        _okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                //validation of the input
                if (_inputWorldTitle.getText().isEmpty()) {
                    GUIManager.getInstance().showMessage("Error", "Please enter a world name");

                    return;
                } else if ((new File(PathManager.getInstance().getWorldSavePath(_inputWorldTitle.getText()), WorldInfo.DEFAULT_FILE_NAME)).exists()) {
                    GUIManager.getInstance().showMessage("Error", "A World with this name already exists");

                    return;
                }

                CoreRegistry.put(GameType.class, (GameType)typeOfGame.getSelection().getValue());

                //set the world settings
                if (_inputSeed.getText().length() > 0) {
                    Config.getInstance().setDefaultSeed(_inputSeed.getText());
                } else {
                    FastRandom random = new FastRandom();
                    Config.getInstance().setDefaultSeed(random.randomCharacterString(32));
                }

                if (_inputWorldTitle.getText().length() > 0) {
                    Config.getInstance().setWorldTitle(_inputWorldTitle.getText());
                } else {
                    Config.getInstance().setWorldTitle(getWorldName());
                }

                List<String> chunkList = new ArrayList<String>();
                switch (_chunkGenerator.getSelectionIndex()) {
                case 1:   //flat
                    chunkList.add(FlatTerrainGenerator.class.getName());
                    //if (checkboxFlora == selected) ... (pseudo code)
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
                Config.getInstance().setChunkGenerator(chunksListArr);

                CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(new WorldInfo(Config.getInstance().getWorldTitle(), Config.getInstance().getDefaultSeed(), Config.getInstance().getDayNightLengthInMs() / 4, chunksListArr, CoreRegistry.get(GameType.class).getClass().toString())));
            }
        });


        _cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _cancelButton.setPosition(new Vector2f(_okButton.getPosition().x + _okButton.getSize().x + 16f, _okButton.getPosition().y));
        _cancelButton.getLabel().setText("Cancel");
        _cancelButton.setVisible(true);

        _cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });
        
        parent.addDisplayElement(_okButton);
        parent.addDisplayElement(_cancelButton);
    }

    private String getWorldName() {
        UIMenuSingleplayer menu = (UIMenuSingleplayer) GUIManager.getInstance().getWindowById("singleplayer");
        return "World" + (menu.getWorldCount() + 1);
    }
}
