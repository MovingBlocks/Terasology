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
import org.terasology.game.types.GameType;
import org.terasology.game.types.GameTypeManager;
import org.terasology.game.paths.PathManager;
import org.terasology.game.types.GameTypeUri;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.layout.ColumnLayout;
import org.terasology.rendering.gui.widgets.*;
import org.terasology.rendering.gui.windows.UIMenuSingleplayer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldInfo;
import org.terasology.world.generator.MapGenerator;
import org.terasology.world.generator.MapGeneratorManager;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.world.generator.MapGeneratorUri;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.util.List;

/*
 * Dialog for generate new world
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */

public class UIDialogCreateNewWorld extends UIDialog {
    public static final float COMPONENT_WIDTH = 256f;
    public static final float COMPONENT_HEIGHT = 30f;
    private UIButton okButton;
    private UIButton cancelButton;

    private UILabel inputSeedLabel;
    private UIText inputSeed;
    private UILabel inputWorldTitleLabel;
    private UIText inputWorldTitle;
    private UILabel chunkGeneratorLabel;
    private UIComboBox chunkGenerator;
    private UIComboBox typeOfGame;
    private List<GameType> gameTypes;
    private UILabel typeOfGameLabel;

    private ModConfig modConfig;
    private UIButton mapSetupButton;
    private List<MapGenerator> mapGenerators;
    private UIButton modButton;

    public UIDialogCreateNewWorld() {
        super(new Vector2f(512f, 380f));
        setTitle("Create new world");

        modConfig = new ModConfig();
        modConfig.copy(CoreRegistry.get(Config.class).getDefaultModSelection());
    }

    @Override
    protected void createDialogArea(UIDisplayContainer parent) {
        createWorldTitleInput();
        createSeedInput();
        createTypeOfGameInput();
        createChunkGeneratorInput();

        ColumnLayout layout = new ColumnLayout();
        layout.setSpacingVertical(4);
        layout.setBorder(20);

        UIComposite content = new UIComposite();
        content.setVisible(true);
        content.setLayout(layout);
        content.addDisplayElement(inputWorldTitleLabel);
        content.addDisplayElement(inputWorldTitle);
        content.addDisplayElement(inputSeedLabel);
        content.addDisplayElement(inputSeed);
        content.addDisplayElement(typeOfGameLabel);
        content.addDisplayElement(typeOfGame);
        content.addDisplayElement(chunkGeneratorLabel);
        content.addDisplayElement(chunkGenerator);
        content.addDisplayElement(mapSetupButton);

        parent.addDisplayElement(content);

        content.orderDisplayElementTop(chunkGenerator);
        content.orderDisplayElementTop(typeOfGame);
        parent.layout();
    }

    @Override
    protected void createButtons(UIDisplayContainer parent) {
        createOkayButton();
        createCancelButton();
        createModButton();
//
        parent.addDisplayElement(modButton);
        parent.addDisplayElement(okButton);
        parent.addDisplayElement(cancelButton);
    }

    private void createWorldTitleInput( ) {
        inputWorldTitleLabel = createLabel("Enter a world name:");

        inputWorldTitle = new UIText();
        inputWorldTitle.setSize(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT));
        //inputWorldTitle.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        inputWorldTitle.setText(getWorldName());
        inputWorldTitle.setVisible(true);
    }

    private void createSeedInput() {
        inputSeedLabel = createLabel("Enter a seed (optional):");

        inputSeed = new UIText();
        inputSeed.setSize(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT));
        //inputSeed.setBackgroundImage("engine:gui_menu", new Vector2f(0f, 90f), new Vector2f(256f, 30f));
        inputSeed.setVisible(true);
    }

    private void createTypeOfGameInput() {
        typeOfGameLabel = createLabel("Choose type of game:");

        typeOfGame = new UIComboBox(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT), new Vector2f(COMPONENT_WIDTH, 2*COMPONENT_HEIGHT));
        gameTypes = CoreRegistry.get(GameTypeManager.class).listItems();
        GameTypeUri defaultGameType = CoreRegistry.get(Config.class).getWorldGeneration().getDefaultGameType();

        int index = 0;
        int defaultIndex = 0;
        for (GameType gameType : gameTypes) {
            if( gameType.uri().equals(defaultGameType) ) {
                defaultIndex = index;
            }
            UIListItem item = new UIListItem(gameType.name(), index);
            item.setTextColor(Color.black);
            item.setPadding(new Vector4f(5f, 5f, 5f, 5f));
            typeOfGame.addItem(item);

            index ++;
        }

        typeOfGame.setVisible(true);
        typeOfGame.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                ModConfig gameTypeModConfig = getSelectedGameType().defaultModConfig();

                if( gameTypeModConfig!=null ) {
                    modConfig.copy(gameTypeModConfig);
                    modConfig.addMod(CoreRegistry.get(GameTypeManager.class).getMod(getSelectedGameType().uri()));
                    if( modButton!=null ) {
                        modButton.setVisible(false);
                    }
                } else {
                    if( modButton!=null ) {
                        modButton.setVisible(true);
                    }
                }

                MapGeneratorUri mapGeneratorUri = getSelectedGameType().defaultMapGenerator();
                if( mapGeneratorUri!=null ) {
                    int index = 0;
                    for (MapGenerator generator : mapGenerators) {
                        if( generator.uri().equals(mapGeneratorUri) ) {
                            chunkGenerator.select(index);
                            break;
                        }
                        index++;
                    }
                }
            }
        });
        typeOfGame.select(defaultIndex);
    }

    private void createChunkGeneratorInput() {
        mapSetupButton = new UIButton(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT), UIButton.ButtonType.NORMAL);
        mapSetupButton.setVisible(false);
        mapSetupButton.getLabel().setText("Setup...");
        mapSetupButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                MapGenerator generator = getSelectedMapGenerator();
                if (generator.hasSetup()) {
                    UIDialog dialog = generator.createSetupDialog();
                    dialog.open();
                }
            }
        }
        );

        chunkGeneratorLabel = createLabel("Choose Map Generator:");

        final MapGeneratorManager mapGeneratorManager = CoreRegistry.get(MapGeneratorManager.class);
        mapGenerators = mapGeneratorManager.listItems();
        chunkGenerator = new UIComboBox(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT), new Vector2f(COMPONENT_WIDTH, 2*COMPONENT_HEIGHT));
        MapGeneratorUri defaultMapGenerator = CoreRegistry.get(Config.class).getWorldGeneration().getDefaultMapGenerator();
        UIListItem item;
        int index = 0;
        int defaultIndex = 0;
        for (MapGenerator mapGenerator : mapGenerators) {
            if( mapGenerator.uri().equals(defaultMapGenerator) ) {
                defaultIndex = index;
            }
            item = new UIListItem(mapGenerator.name(), index);
            item.setTextColor(Color.black);
            item.setPadding(new Vector4f(2f, 2f, 2f, 2f));
            chunkGenerator.addItem(item);

            index++;
        }

        chunkGenerator.setVisible(true);
        chunkGenerator.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                MapGenerator mapGenerator = getSelectedMapGenerator();
                mapSetupButton.setVisible(mapGenerator.hasSetup());
                String mod = mapGeneratorManager.getMod(mapGenerator.uri());
                if( mod!=null ) {
                    modConfig.addMod(mod);
                }
            }
        });
        chunkGenerator.select(defaultIndex);
    }

    private UILabel createLabel( String text ) {
        UILabel label;
        label = new UILabel(text);
        label.setColor(Color.darkGray);
        label.setSize(new Vector2f(0f, 32));
        label.setMargin(new Vector4f(16, 0, 0, 0));
        label.setVisible(true);
        return label;
    }


    private void createOkayButton() {
        okButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        okButton.getLabel().setText("Play");
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

                CoreRegistry.get(Config.class).getDefaultModSelection().copy(modConfig);
                CoreRegistry.get(Config.class).save();

                GameType gameType = getSelectedGameType();
                MapGenerator mapGenerator = getSelectedMapGenerator();

                CoreRegistry.get(GameEngine.class).changeState(new StateLoading(new WorldInfo(config.getWorldGeneration().getWorldTitle(), config.getWorldGeneration().getDefaultSeed(), config.getSystem().getDayNightLengthInMs() / 4, mapGenerator.uri(), gameType.uri(), modConfig)));
            }
        });
    }

    private void createCancelButton() {
        cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
//        cancelButton.setPosition(new Vector2f(okButton.getPosition().x + okButton.getSize().x + 16f, okButton.getPosition().y));
        cancelButton.getLabel().setText("Cancel");
        cancelButton.setVisible(true);

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });
    }

    private UIButton createModButton() {
        modButton = new UIButton(new Vector2f(128, 32), UIButton.ButtonType.NORMAL);
//        modButton.setPosition(new Vector2f(chunkGenerator.getPosition().x, chunkGenerator.getPosition().y + chunkGenerator.getSize().y + 58f));
        modButton.setVisible(true);
        modButton.getLabel().setText("Mods...");
        modButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIDialogMods dialog = new UIDialogMods(modConfig);
                dialog.open();
            }
        });
        return modButton;
    }

    private MapGenerator getSelectedMapGenerator() {
        int index = chunkGenerator.getSelectionIndex();
        return mapGenerators.get(index);
    }
    private GameType getSelectedGameType() {
        int index = typeOfGame.getSelectionIndex();
        return gameTypes.get(index);
    }

    private String getWorldName() {
        UIMenuSingleplayer menu = (UIMenuSingleplayer) getGUIManager().getWindowById("singleplayer");
        return "World" + (menu.getWorldCount() + 1);
    }
}
