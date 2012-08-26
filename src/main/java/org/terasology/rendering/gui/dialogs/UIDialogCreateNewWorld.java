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
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.*;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldInfo;
import org.terasology.world.generator.core.FlatTerrainGenerator;
import org.terasology.world.generator.core.FloraGenerator;
import org.terasology.world.generator.core.ForestGenerator;
import org.terasology.world.generator.core.PerlinTerrainGenerator;
import org.terasology.world.liquid.LiquidsGenerator;

import javax.vecmath.Vector2f;

/*
 * Dialog for generate new world
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */

public class UIDialogCreateNewWorld extends UIDialogBox {
    private UIButton _okButton;
    private UIButton _cancelButton;

    private UIText _inputSeedLabel;
    private UIInput _inputSeed;
    private UIText _inputWorldTitleLabel;
    private UIInput _inputWorldTitle;
    private UIText _chunkGeneratorLabel;
    private UIComboBox _chunkGenerator;

    public UIDialogCreateNewWorld(String title, Vector2f size) {
        super(title, size);
        setModal(true);

        _inputSeed = new UIInput(new Vector2f(256f, 30f));
        _inputSeed.setVisible(true);

        _inputWorldTitle = new UIInput(new Vector2f(256f, 30f));
        _inputWorldTitle.setVisible(true);
        
        _inputSeedLabel = new UIText("Enter a seed (optional):");
        _inputSeedLabel.setColor(Color.darkGray);
        _inputSeedLabel.setSize(new Vector2f(0f, 16f));
        _inputSeedLabel.setVisible(true);

        _inputWorldTitleLabel = new UIText("Enter a world name:");
        _inputWorldTitleLabel.setColor(Color.darkGray);
        _inputWorldTitleLabel.setSize(new Vector2f(0f, 16f));
        _inputWorldTitleLabel.setVisible(true);
        
        _chunkGeneratorLabel = new UIText("Choose Chunk Generator:");
        _chunkGeneratorLabel.setColor(Color.darkGray);
        _chunkGeneratorLabel.setSize(new Vector2f(0f, 16f));
        _chunkGeneratorLabel.setVisible(true);

        _chunkGenerator = new UIComboBox(new Vector2f(176f, 22f), new Vector2f(175f, 64f));
        _chunkGenerator.addItem("Normal", new Integer(0));
        _chunkGenerator.addItem("Flat", new Integer(1));
        _chunkGenerator.setSelectedItemIndex(0);
        _chunkGenerator.setVisible(true);


        _inputWorldTitleLabel.setPosition(new Vector2f(15f, 32f));
        _inputWorldTitle.setPosition(new Vector2f(_inputWorldTitleLabel.getPosition().x, _inputWorldTitleLabel.getPosition().y + _inputWorldTitleLabel.getSize().y + 8f));
        _inputSeedLabel.setPosition(new Vector2f(_inputWorldTitle.getPosition().x, _inputWorldTitle.getPosition().y + _inputWorldTitle.getSize().y + 16f));
        _inputSeed.setPosition(new Vector2f(_inputSeedLabel.getPosition().x, _inputSeedLabel.getPosition().y + _inputSeedLabel.getSize().y + 8f));
        
        _chunkGeneratorLabel.setPosition(new Vector2f(_inputSeed.getPosition().x, _inputSeed.getPosition().y + _inputSeed.getSize().y + 16f));
        _chunkGenerator.setPosition(new Vector2f(_chunkGeneratorLabel.getPosition().x, _chunkGeneratorLabel.getPosition().y + _chunkGeneratorLabel.getSize().y + 8f));

        _okButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _okButton.getLabel().setText("Play");
        _okButton.setPosition(new Vector2f(size.x / 2 - _okButton.getSize().x - 16f, size.y - _okButton.getSize().y - 10));
        _okButton.setVisible(true);

        _okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                //validation of the input
                if (_inputWorldTitle.getValue().isEmpty()) {
                    GUIManager.getInstance().showMessage("Error", "Please enter a world name");
                    
                    return;
                } else if ((new File(PathManager.getInstance().getWorldSavePath(_inputWorldTitle.getValue()), WorldInfo.DEFAULT_FILE_NAME)).exists()) {
                    GUIManager.getInstance().showMessage("Error", "A World with this name already exists");
                    
                    return;
                }
                
                //set the world settings
                if (_inputSeed.getValue().length() > 0) {
                    Config.getInstance().setDefaultSeed(_inputSeed.getValue());
                } else {
                    FastRandom random = new FastRandom();
                    Config.getInstance().setDefaultSeed(random.randomCharacterString(32));
                }

                if (_inputWorldTitle.getValue().length() > 0) {
                    Config.getInstance().setWorldTitle(_inputWorldTitle.getValue());
                } else {
                    Config.getInstance().setWorldTitle(getWorldName());
                }
                
                List<String> chunkList = new ArrayList<String>();
                switch (_chunkGenerator.getSelectedItemIndex()) {
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
                
                CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer(new WorldInfo(Config.getInstance().getWorldTitle(), Config.getInstance().getDefaultSeed(), Config.getInstance().getDayNightLengthInMs() / 4, chunksListArr)));
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

        addDisplayElement(_inputWorldTitleLabel, "inputWorldTitleLabel");
        addDisplayElement(_inputWorldTitle, "inputWorldTitle");
        addDisplayElement(_inputSeedLabel, "inputSeedLabel");
        addDisplayElement(_inputSeed, "inputSeed");
        addDisplayElement(_chunkGeneratorLabel, "chunkGeneratorLabel");
        addDisplayElement(_okButton, "okButton");
        addDisplayElement(_cancelButton, "cancelButton");
        addDisplayElement(_chunkGenerator, "chunkGenerator");
    }

    public String getWorldName() {
        UIList list = (UIList) GUIManager.getInstance().getWindowById("selectWorld").getElementById("list");
        return "World" + (list.size() + 1);
    }
}
