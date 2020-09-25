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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.math.TeraMath;
import org.terasology.module.ModuleEnvironment;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UISliderOnChangeTriggeredListener;
import org.terasology.rendering.world.WorldSetupWrapper;
import org.terasology.utilities.Assets;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.zones.Zone;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class lets the user preview different worlds added in the
 * {@link UniverseSetupScreen}. Each world is still configurable and its seed
 * can be changed by the re-roll button. Note that each world has a unique seed.
 */
public class WorldPreGenerationScreen extends CoreScreenLayer implements UISliderOnChangeTriggeredListener {

    private static final Logger logger = LoggerFactory.getLogger(WorldPreGenerationScreen.class);

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:worldPreGenerationScreen");

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    private ModuleEnvironment environment;
    private WorldGenerator worldGenerator;
    private Texture texture;
    private UIImage previewImage;
    private Context context;
    private PreviewGenerator previewGen;
    private List<WorldSetupWrapper> worldList;
    private String selectedWorld;
    private List<String> worldNames;
    private int seedNumber = 0;
    private UISlider zoomSlider;

    /**
     * A function called before the screen comes to the forefront to setup the environment
     * and extract necessary objects from the new Context.
     *
     * @param subContext The new environment created in {@link UniverseSetupScreen}
     * @throws UnresolvedWorldGeneratorException The creation of a world generator can throw this Exception
     */
    public void setEnvironment(Context subContext) throws UnresolvedWorldGeneratorException {

        context = subContext;
        environment = context.get(ModuleEnvironment.class);
        context.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment, context));
        worldList = context.get(UniverseSetupScreen.class).getWorldsList();
        selectedWorld = context.get(UniverseSetupScreen.class).getSelectedWorld();
        worldNames = context.get(UniverseSetupScreen.class).worldNames();

        setWorldGenerators();

        worldGenerator = findWorldByName(selectedWorld).getWorldGenerator();
        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        worldsDropdown.setOptions(worldNames);
        genTexture();

        List<Zone> previewZones = Lists.newArrayList(worldGenerator.getZones())
                .stream()
                .filter(z -> !z.getPreviewLayers().isEmpty())
                .collect(Collectors.toList());
        if (previewZones.isEmpty()) {
            previewGen = new FacetLayerPreview(environment, worldGenerator);
        }
    }

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        zoomSlider = find("zoomSlider", UISlider.class);
        if (zoomSlider != null) {
            zoomSlider.setValue(2f);
            zoomSlider.setUiSliderOnChangeTriggeredListener(this);
        }


        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        worldsDropdown.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return selectedWorld;
            }

            @Override
            public void set(String value) {
                selectedWorld = value;
                try {
                    if (findWorldByName(selectedWorld).getWorldGenerator() == null) {
                        worldGenerator = WorldGeneratorManager.createWorldGenerator(findWorldByName(selectedWorld).getWorldGeneratorInfo().getUri(), context, environment);
                        findWorldByName(selectedWorld).setWorldGenerator(worldGenerator);
                    } else {
                        worldGenerator = findWorldByName(selectedWorld).getWorldGenerator();
                    }
                    if (worldGenerator.getWorldSeed() == null) {
                        worldGenerator.setWorldSeed(createSeed(selectedWorld));
                    }
                    previewGen = new FacetLayerPreview(environment, worldGenerator);
                    updatePreview();
                } catch (UnresolvedWorldGeneratorException e) {
                    e.printStackTrace();
                }
            }
        });

        WidgetUtil.trySubscribe(this, "reRoll", button -> {
            worldGenerator.setWorldSeed(createSeed(selectedWorld));
            updatePreview();
        });

        StartPlayingScreen startPlayingScreen = getManager().createScreen(StartPlayingScreen.ASSET_URI, StartPlayingScreen.class);
        WidgetUtil.trySubscribe(this, "continue", button -> {
            startPlayingScreen.setTargetWorld(worldList, findWorldByName(selectedWorld), texture, context);
            triggerForwardAnimation(startPlayingScreen);
        });

        WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);
        WidgetUtil.trySubscribe(this, "config", button -> {
            try {
                if (!selectedWorld.isEmpty()) {
                    worldSetupScreen.setWorld(context, findWorldByName(selectedWorld));
                    triggerForwardAnimation(worldSetupScreen);
                } else {
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Worlds List Empty!", "No world found to configure.");
                }
            } catch (UnresolvedWorldGeneratorException e) {
                e.printStackTrace();
            }
        });

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        try {
            if (findWorldByName(selectedWorld).getWorldGenerator() == null) {
                worldGenerator = WorldGeneratorManager.createWorldGenerator(findWorldByName(selectedWorld).getWorldGeneratorInfo().getUri(), context, environment);
                findWorldByName(selectedWorld).setWorldGenerator(worldGenerator);
            } else {
                worldGenerator = findWorldByName(selectedWorld).getWorldGenerator();
            }
            if (worldGenerator.getWorldSeed().isEmpty()) {
                worldGenerator.setWorldSeed(createSeed(selectedWorld));
            }
            previewGen = new FacetLayerPreview(environment, worldGenerator);
            updatePreview();
        } catch (UnresolvedWorldGeneratorException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a texture and sets it to the image view, thus previewing the world.
     */
    private void genTexture() {
        int imgWidth = 384;
        int imgHeight = 384;
        ByteBuffer buffer = ByteBuffer.allocateDirect(imgWidth * imgHeight * Integer.BYTES);
        ByteBuffer[] data = new ByteBuffer[]{buffer};
        ResourceUrn uri = new ResourceUrn("engine:terrainPreview");
        TextureData texData = new TextureData(imgWidth, imgHeight, data, Texture.WrapMode.CLAMP, Texture.FilterMode.LINEAR);
        texture = Assets.generateAsset(uri, texData, Texture.class);

        previewImage = find("preview", UIImage.class);
        previewImage.setImage(texture);
    }

    /**
     * Updates the preview according to any changes made to the configurator.
     * Also pops up a message and keeps track of percentage world preview prepared.
     */
    private void updatePreview() {

        final NUIManager manager = context.get(NUIManager.class);
        final WaitPopup<TextureData> popup = manager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Updating Preview", "Please wait ...");

        ProgressListener progressListener = progress ->
                popup.setMessage("Updating Preview", String.format("Please wait ... %d%%", (int) (progress * 100f)));

        Callable<TextureData> operation = () -> {
            int zoom = TeraMath.floorToInt(zoomSlider.getValue());
            TextureData data = texture.getData();

            previewGen.render(data, zoom, progressListener);

            return data;
        };

        popup.onSuccess(texture::reload);
        popup.startOperation(operation, true);
    }

    /**
     * This method takes the name of the world selected in the worldsDropdown
     * as String and return the corresponding WorldSetupWrapper object.
     *
     * @return {@link WorldSetupWrapper} object of the selected world.
     */
    private WorldSetupWrapper findWorldByName(String searchWorld) {
        for (WorldSetupWrapper world : worldList) {
            if (world.getWorldName().toString().equals(searchWorld)) {
                return world;
            }
        }
        return null;
    }

    /**
     * Creates a unique world seed by appending the world name with an incrementing number, on top of the universe seed.
     *
     * @param world {@link WorldSetupWrapper} object whose seed is to be set.
     * @return The seed as a string.
     */
    private String createSeed(String world) {
        String seed = context.get(UniverseWrapper.class).getSeed();
        return seed + world + seedNumber++;
    }

    private void setWorldGenerators() {
        for (WorldSetupWrapper worldSetupWrapper : worldList) {
            if (worldSetupWrapper.getWorldGenerator() == null) {
                try {
                    worldSetupWrapper.setWorldGenerator(WorldGeneratorManager.createWorldGenerator(findWorldByName(worldSetupWrapper.getWorldName().toString()).getWorldGeneratorInfo().getUri(), context, environment));
                } catch (UnresolvedWorldGeneratorException e) {
                    e.printStackTrace();
                }
            }
            worldSetupWrapper.getWorldGenerator().setWorldSeed(createSeed(worldSetupWrapper.getWorldName().toString()));
        }
    }

    @Override
    public void onSliderValueChanged(float val) {
        updatePreview();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
