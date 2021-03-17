// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.joml.Vector2i;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.RenderingModuleSettingScreen;
import org.terasology.engine.rendering.world.WorldSetupWrapper;
import org.terasology.engine.network.NetworkMode;
import org.terasology.nui.Canvas;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.time.WorldTime;

import java.util.List;

public class StartPlayingScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:startPlayingScreen");

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private GameEngine gameEngine;

    @In
    private TranslationSystem translationSystem;

    private Texture texture;
    private List<WorldSetupWrapper> worldSetupWrappers;
    private UniverseWrapper universeWrapper;
    private WorldSetupWrapper targetWorld;
    private Context subContext;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "play", button -> {
            universeWrapper.setTargetWorld(targetWorld);
            final GameManifest gameManifest = GameManifestProvider.createGameManifest(universeWrapper, moduleManager, config);
            if (gameManifest != null) {
                gameEngine.changeState(new StateLoading(gameManifest, (universeWrapper.getLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
            } else {
                getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", "Can't create new game!");
            }

            SimpleUri uri;
            WorldInfo worldInfo;
            //gameManifest.addWorld(worldInfo);
            int i = 0;
            for (WorldSetupWrapper world : worldSetupWrappers) {
                if (world != targetWorld) {
                    i++;
                    uri = world.getWorldGeneratorInfo().getUri();
                    worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD + i, world.getWorldName().toString(), world.getWorldGenerator().getWorldSeed(),
                            (long) (WorldTime.DAY_LENGTH * WorldTime.NOON_OFFSET), uri);
                    gameManifest.addWorld(worldInfo);
                    config.getUniverseConfig().addWorldManager(worldInfo);
                }

            }

            gameEngine.changeState(new StateLoading(gameManifest, (universeWrapper.getLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });

        WidgetUtil.trySubscribe(this, "renderingSettings", button -> {
            RenderingModuleSettingScreen renderingModuleSettingScreen = (RenderingModuleSettingScreen) getManager().getScreen(RenderingModuleSettingScreen.ASSET_URI);
            if (renderingModuleSettingScreen == null) {
                renderingModuleSettingScreen = getManager().createScreen(RenderingModuleSettingScreen.ASSET_URI, RenderingModuleSettingScreen.class);
                renderingModuleSettingScreen.setSubContext(this.subContext);
                renderingModuleSettingScreen.postInit();
            }
            triggerForwardAnimation(renderingModuleSettingScreen);
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        UIImage previewImage = find("preview", UIImage.class);
        previewImage.setImage(texture);

        UILabel subitle = find("subtitle", UILabel.class);
        subitle.setText(translationSystem.translate("${engine:menu#start-playing}") + " in " + targetWorld.getWorldName().toString());
    }

    /**
     * This method is called before the screen comes to the forefront to set the world
     * in which the player is about to spawn.
     * @param worldSetupWrapperList The world in which the player is going to spawn.
     * @param targetWorldTexture The world texture generated in {@link WorldPreGenerationScreen} to be displayed on this screen.
     */
    public void setTargetWorld(List<WorldSetupWrapper> worldSetupWrapperList, WorldSetupWrapper spawnWorld, Texture targetWorldTexture, Context context) {
        texture = targetWorldTexture;
        worldSetupWrappers = worldSetupWrapperList;
        universeWrapper = context.get(UniverseWrapper.class);
        targetWorld = spawnWorld;
        subContext = context;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i vector2i) {
        return vector2i;
    }
}
