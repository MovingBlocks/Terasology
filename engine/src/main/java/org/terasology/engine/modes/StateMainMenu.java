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
package org.terasology.engine.modes;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISpace;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinBuilder;
import org.terasology.rendering.nui.skin.UISkinData;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

/**
 * The class implements the main game menu.
 * <p/>
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @version 0.3
 */
public class StateMainMenu implements GameState {
    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;
    private GUIManager guiManager;
    private NUIManager nuiManager;
    private InputSystem inputSystem;

    private String messageOnLoad = "";

    public StateMainMenu() {
    }

    public StateMainMenu(String showMessageOnLoad) {
        messageOnLoad = showMessageOnLoad;
    }


    @Override
    public void init(GameEngine gameEngine) {

        //lets get the entity event system running
        entityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModuleManager.class), CoreRegistry.get(NetworkSystem.class), CoreRegistry.get(ReflectFactory.class));
        eventSystem = CoreRegistry.get(EventSystem.class);

        guiManager = CoreRegistry.get(GUIManager.class);
        nuiManager = CoreRegistry.get(NUIManager.class);
        ((NUIManagerInternal)nuiManager).refreshWidgetsLibrary();

        componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);

        CameraTargetSystem cameraTargetSystem = new CameraTargetSystem();
        CoreRegistry.put(CameraTargetSystem.class, cameraTargetSystem);
        componentSystemManager.register(cameraTargetSystem, "engine:CameraTargetSystem");

        eventSystem.registerEventHandler(guiManager);
        eventSystem.registerEventHandler(CoreRegistry.get(NUIManager.class));
        inputSystem = CoreRegistry.get(InputSystem.class);
        componentSystemManager.register(inputSystem, "engine:InputSystem");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());

        LocalPlayer localPlayer = CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();

        playBackgroundMusic();

        //guiManager.openWindow("main");
        openMainMenu();
        if (!messageOnLoad.isEmpty()) {
            guiManager.showMessage("", messageOnLoad);
        }
    }

    private void openMainMenu() {
        /*UISkinData skinData = new UISkinBuilder()
                .setTextShadowed(true)

                .setWidgetClass(ArbitraryLayout.class)
                .setBackgroundMode(ScaleMode.SCALE_FILL)
                .setBackground(Assets.getTexture("engine:menuBackground"))

                .setWidgetClass(UILabel.class)
                .setTextVerticalAlignment(VerticalAlign.TOP)

                .setWidgetClass(UIImage.class)
                .setTextureScaleMode(ScaleMode.SCALE_FIT)

                .setWidgetClass(UIButton.class)
                .setBackground(Assets.getTexture("engine", "button"))
                .setTextHorizontalAlignment(HorizontalAlign.CENTER)
                .setTextVerticalAlignment(VerticalAlign.MIDDLE)
                .setBackgroundBorder(new Border(2, 2, 2, 2))
                .setMargin(new Border(4, 4, 4, 4))
                .setTextureScaleMode(ScaleMode.SCALE_FIT)

                .setWidgetMode("hover")
                .setBackground(Assets.getTexture("engine", "buttonOver"))

                .setWidgetMode("down")
                .setBackground(Assets.getTexture("engine", "buttonDown"))
                .setTextColor(Color.YELLOW)
                .build();

        UISkin skin = Assets.generateAsset(new AssetUri(AssetType.UI_SKIN, "engine:defaultSkin"), skinData, UISkin.class);    */
        UISkin skin = Assets.getSkin("engine:mainmenu");

        ColumnLayout grid = new ColumnLayout();
        grid.addWidget(new UIButton("Single Player"));
        grid.addWidget(new UIButton("Host Game"));
        grid.addWidget(new UIButton("Join Game"));
        grid.addWidget(new UIButton("Settings"));
        grid.addWidget(new UISpace());
        grid.addWidget(new UIButton("Exit"));
        grid.setPadding(new Border(0, 0, 4, 4));

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("Pre Alpha"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(280, 192), new Vector2f(0.5f, 0.7f));

        UIScreen mainMenu = new UIScreen(layout, skin);
        CoreRegistry.get(NUIManager.class).pushScreen(mainMenu);
    }

    @Override
    public void dispose() {
        eventSystem.process();

        componentSystemManager.shutdown();
        stopBackgroundMusic();
        guiManager.closeAllWindows();

        entityManager.clear();
        CoreRegistry.clear();
    }

    private void playBackgroundMusic() {
        CoreRegistry.get(AudioManager.class).playMusic(Assets.getMusic("engine:MenuTheme"));
    }

    private void stopBackgroundMusic() {
        CoreRegistry.get(AudioManager.class).stopAllSounds();
    }

    @Override
    public void handleInput(float delta) {
        inputSystem.update(delta);
    }

    @Override
    public void update(float delta) {
        updateUserInterface(delta);

        eventSystem.process();
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        renderUserInterface();
        nuiManager.render();
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

    public void renderUserInterface() {
        guiManager.render();
    }

    private void updateUserInterface(float delta) {
        guiManager.update();
        nuiManager.update(delta);
    }
}
