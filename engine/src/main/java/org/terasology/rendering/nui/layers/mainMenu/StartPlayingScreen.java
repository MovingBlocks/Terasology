/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.i18n.TranslationSystem;
import org.terasology.module.Module;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.world.WorldSetupWrapper;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

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
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
