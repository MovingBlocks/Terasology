// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.engine.rendering.world.WorldSetupWrapper;
import org.terasology.math.TeraMath;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UISlider;
import org.terasology.nui.widgets.UISliderOnChangeTriggeredListener;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.zones.Zone;

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

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:worldPreGenerationScreen");

    private static final Logger logger = LoggerFactory.getLogger(WorldPreGenerationScreen.class);

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
    private WorldSetupWrapper selectedWorld;
    private List<String> worldNames;
    private int seedNumber;
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
        selectedWorld = context.get(UniverseSetupScreen.class).getSelectedWorld();
        worldNames = context.get(UniverseSetupScreen.class).worldNames();

        ensureWorldGeneratorIsSet();
        selectedWorld.getWorldGenerator().setWorldSeed(createSeed(selectedWorld.getWorldName().toString()));

        worldGenerator = selectedWorld.getWorldGenerator();
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
                return selectedWorld.getWorldName().toString();
            }

            @Override
            public void set(String value) {
                selectedWorld.setWorldName(new Name(value));
                try {
                    if (selectedWorld.getWorldGenerator() == null) {
                        worldGenerator = WorldGeneratorManager.createWorldGenerator(selectedWorld
                                .getWorldGeneratorInfo().getUri(), context, environment);
                        selectedWorld.setWorldGenerator(worldGenerator);
                    } else {
                        worldGenerator = selectedWorld.getWorldGenerator();
                    }
                    if (worldGenerator.getWorldSeed() == null) {
                        worldGenerator.setWorldSeed(createSeed(selectedWorld.getWorldName().toString()));
                    }
                    previewGen = new FacetLayerPreview(environment, worldGenerator);
                    updatePreview();
                } catch (UnresolvedWorldGeneratorException e) {
                    e.printStackTrace();
                }
            }
        });

        WidgetUtil.trySubscribe(this, "reRoll", button -> {
            worldGenerator.setWorldSeed(createSeed(selectedWorld.getWorldName().toString()));
            updatePreview();
        });

        StartPlayingScreen startPlayingScreen = getManager().createScreen(StartPlayingScreen.ASSET_URI, StartPlayingScreen.class);
        WidgetUtil.trySubscribe(this, "continue", button -> {
            startPlayingScreen.setTargetWorld(selectedWorld, texture, context);
            triggerForwardAnimation(startPlayingScreen);
        });

        WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);
        WidgetUtil.trySubscribe(this, "config", button -> {
            try {
                if (!selectedWorld.getWorldName().isEmpty()) {
                    worldSetupScreen.setWorld(context, selectedWorld, worldsDropdown);
                    triggerForwardAnimation(worldSetupScreen);
                } else {
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                            .setMessage("Worlds List Empty!", "No world found to configure.");
                }
            } catch (UnresolvedWorldGeneratorException e) {
                e.printStackTrace();
            }
        });

        WidgetUtil.trySubscribe(this, "close", button -> {
            final UniverseSetupScreen universeSetupScreen =
                    getManager().createScreen(UniverseSetupScreen.ASSET_URI, UniverseSetupScreen.class);
            UIDropdownScrollable worldsDropdownOfUniverse = universeSetupScreen.find("worlds", UIDropdownScrollable.class);
            universeSetupScreen.refreshWorldDropdown(worldsDropdownOfUniverse);
            triggerBackAnimation();
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        try {
            if (selectedWorld.getWorldGenerator() == null) {
                worldGenerator = WorldGeneratorManager.createWorldGenerator(selectedWorld
                        .getWorldGeneratorInfo().getUri(), context, environment);
                selectedWorld.setWorldGenerator(worldGenerator);
            } else {
                worldGenerator = selectedWorld.getWorldGenerator();
            }
            if (worldGenerator.getWorldSeed().isEmpty()) {
                worldGenerator.setWorldSeed(createSeed(selectedWorld.getWorldName().toString()));
            }
            previewGen = new FacetLayerPreview(environment, worldGenerator);
            updatePreview();
        } catch (UnresolvedWorldGeneratorException e) {
            e.printStackTrace();
        }
    }

    //TODO: this does not actually only set it when configure is called from WorldPreGenerationScreen, but also if called from UniverseSetupScreen
    /**
     * Set seletedWorld when configure from WorldPreGenerationScreen
     * @param newNameToSet
     */
    public void setName(Name newNameToSet) {
        selectedWorld.setWorldName(newNameToSet);
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
     * Creates a unique world seed by appending the world name with an incrementing number, on top of the universe seed.
     *
     * @param world {@link WorldSetupWrapper} object whose seed is to be set.
     * @return The seed as a string.
     */
    private String createSeed(String world) {
        String seed = context.get(UniverseWrapper.class).getSeed();
        return seed + world + seedNumber++;
    }

    private void ensureWorldGeneratorIsSet() {
        if (selectedWorld.getWorldGenerator() == null) {
            try {
                selectedWorld.setWorldGenerator(WorldGeneratorManager.createWorldGenerator(
                        selectedWorld.getWorldGeneratorInfo().getUri(), context, environment));
            } catch (UnresolvedWorldGeneratorException e) {
                //TODO: this will likely fail at game creation time later-on due to lack of world generator - don't just ignore this
                e.printStackTrace();
            }
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
