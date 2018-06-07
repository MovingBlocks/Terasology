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
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ResolutionResult;
import org.terasology.network.NetworkMode;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.world.WorldSetupWrapper;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

public class StartPlayingScreen extends CoreScreenLayer {

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private GameEngine gameEngine;

    @In
    private TranslationSystem translationSystem;

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:startPlayingScreen");
    private Texture texture;
    private WorldSetupWrapper world;
    private UniverseWrapper universeWrapper;

    @Override
    public void initialise() {

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "play", button -> {
            GameManifest gameManifest = new GameManifest();

            gameManifest.setTitle(universeWrapper.getGameName());

            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
            if (!result.isSuccess()) {
                MessagePopup errorMessagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                if (errorMessagePopup != null) {
                    errorMessagePopup.setMessage("Invalid Module Selection", "Please review your module seleciton and try again");
                }
                return;
            }
            for (Module module : result.getModules()) {
                gameManifest.addModule(module.getId(), module.getVersion());
            }

            SimpleUri uri = world.getWorldGeneratorInfo().getUri();
            float timeOffset = 0.25f + 0.025f;
            WorldInfo worldInfo = new WorldInfo(TerasologyConstants.MAIN_WORLD, world.getWorldGenerator().getWorldSeed(),
                    (long) (WorldTime.DAY_LENGTH * timeOffset), uri);
            gameManifest.addWorld(worldInfo);
            gameEngine.changeState(new StateLoading(gameManifest, (universeWrapper.getLoadingAsServer()) ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
        });
    }

    @Override
    public void onOpened() {
        UIImage previewImage = find("preview", UIImage.class);
        previewImage.setImage(texture);

        UILabel subitle = find("subtitle", UILabel.class);
        subitle.setText(translationSystem.translate("${engine:menu#start-playing}") + " in " + world.getWorldName().toString());
    }

    /**
     * This method is called before the screen comes to the forefront to set the world
     * in which the player is about to spawn.
     * @param targetWorld The world in which the player is going to spawn.
     * @param targetWorldTexture The world texture generated in {@link WorldPreGenerationScreen} to be displayed on this
     *                           screen.
     */
    public void setTargetWorld(WorldSetupWrapper targetWorld, Texture targetWorldTexture, Context context) {
        texture = targetWorldTexture;
        world = targetWorld;
        universeWrapper = context.get(UniverseWrapper.class);
    }
}
