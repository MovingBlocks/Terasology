/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.config.ModuleConfig;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleSelection;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.DialogListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.layout.ColumnLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComboBox;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.windows.UIMenuSelectWorld;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generator.WorldGeneratorInfo;
import org.terasology.world.generator.WorldGeneratorManager;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.nio.file.Files;
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
    private UILabel worldGeneratorLabel;
    private UIComboBox worldGenerator;

    private UIButton modButton;

    private boolean createServerGame;
    private ModuleSelection selection;

    public UIDialogCreateNewWorld(boolean createServer) {
        super(new Vector2f(512f, 380f));
        setTitle("Create new world");

        this.createServerGame = createServer;

    }

    @Override
    protected void createDialogArea(UIDisplayContainer parent) {
        selection = new ModuleSelection(CoreRegistry.get(ModuleManager.class));
        for (String moduleId : CoreRegistry.get(Config.class).getDefaultModSelection().listModules()) {
            ModuleSelection newSelection = selection.add(moduleId);
            if (newSelection.isValid()) {
                selection = newSelection;
            }
        }

        createWorldTitleInput();
        createSeedInput();
        createWorldGeneratorInput();

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
        content.addDisplayElement(worldGeneratorLabel);
        content.addDisplayElement(worldGenerator);

        parent.addDisplayElement(content);

        content.orderDisplayElementTop(worldGenerator);
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

    private void createWorldTitleInput() {
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

    private void createWorldGeneratorInput() {
        worldGeneratorLabel = createLabel("Choose World Generator:");

        final WorldGeneratorManager worldGeneratorManager = CoreRegistry.get(WorldGeneratorManager.class);
        List<WorldGeneratorInfo> worldGenerators = worldGeneratorManager.getWorldGenerators();
        worldGenerator = new UIComboBox(new Vector2f(COMPONENT_WIDTH, COMPONENT_HEIGHT), new Vector2f(COMPONENT_WIDTH, 2 * COMPONENT_HEIGHT));
        SimpleUri defaultMapGenerator = CoreRegistry.get(Config.class).getWorldGeneration().getDefaultGenerator();
        UIListItem item;
        int index = 0;
        int defaultIndex = 0;
        for (WorldGeneratorInfo generator : worldGenerators) {
            if (generator.getUri().equals(defaultMapGenerator)) {
                defaultIndex = index;
            }
            item = new UIListItem(generator.getDisplayName(), index);
            item.setTextColor(Color.black);
            item.setPadding(new Vector4f(2f, 2f, 2f, 2f));
            item.setValue(generator);
            worldGenerator.addItem(item);

            index++;
        }

        worldGenerator.setVisible(true);
        worldGenerator.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                // TODO: Only show valid world generators based on current selection
                WorldGeneratorInfo generator = getSelectedWorldGenerator();
                ModuleSelection newSelection = selection.add(generator.getUri().getModuleName());
                if (newSelection.isValid()) {
                    selection = newSelection;
                }
            }
        });
        worldGenerator.select(defaultIndex);
    }

    private UILabel createLabel(String text) {
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
                } else if (Files.isRegularFile(PathManager.getInstance().getSavePath(inputWorldTitle.getText()).resolve(GameManifest.DEFAULT_FILE_NAME))) {
                    getGUIManager().showMessage("Error", "A World with this name already exists");

                    return;
                }

                //set the world settings
                if (inputSeed.getText().length() > 0) {
                    config.getWorldGeneration().setDefaultSeed(inputSeed.getText());
                } else {
                    FastRandom random = new FastRandom();
                    config.getWorldGeneration().setDefaultSeed(random.nextString(32));
                }

                if (inputWorldTitle.getText().length() > 0) {
                    config.getWorldGeneration().setWorldTitle(inputWorldTitle.getText());
                } else {
                    config.getWorldGeneration().setWorldTitle(getWorldName());
                }

                ModuleConfig moduleConfig = CoreRegistry.get(Config.class).getDefaultModSelection();
                moduleConfig.clear();
                for (Module module : selection.getSelection()) {
                    moduleConfig.addModule(module.getId());
                }
                CoreRegistry.get(Config.class).save();

                WorldGeneratorInfo worldGeneratorInfo = getSelectedWorldGenerator();

                GameManifest gameManifest = new GameManifest();
                gameManifest.setTitle(config.getWorldGeneration().getWorldTitle());
                gameManifest.setSeed(config.getWorldGeneration().getDefaultSeed());
                for (Module module : selection.getSelection()) {
                    gameManifest.addModule(module.getId(), module.getVersion());
                }

                WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, config.getWorldGeneration().getDefaultSeed(),
                        (long) (WorldTime.DAY_LENGTH * 0.025f), worldGeneratorInfo.getUri());
                gameManifest.addWorld(worldInfo);

                CoreRegistry.get(GameEngine.class).changeState(new StateLoading(gameManifest, (createServerGame) ? NetworkMode.SERVER : NetworkMode.NONE));
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
                UIDialogModules dialog = new UIDialogModules(selection);
                dialog.addDialogListener(new DialogListener() {
                    @Override
                    public void close(UIDisplayElement closingDialog, EReturnCode returnCode, Object returnValue) {
                        if (returnCode == EReturnCode.OK && returnValue instanceof ModuleSelection) {
                            UIDialogCreateNewWorld.this.selection = (ModuleSelection) returnValue;
                        }
                    }
                });
                dialog.open();
            }
        });
        return modButton;
    }

    private WorldGeneratorInfo getSelectedWorldGenerator() {
        return (WorldGeneratorInfo) worldGenerator.getSelection().getValue();
    }

    private String getWorldName() {
        UIMenuSelectWorld menu = (UIMenuSelectWorld) getGUIManager().getWindowById("selectworld");
        return "Game" + (menu.getWorldCount() + 1);
    }
}
