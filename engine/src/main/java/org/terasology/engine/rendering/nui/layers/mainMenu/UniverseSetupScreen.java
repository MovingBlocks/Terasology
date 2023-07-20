// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.modes.StateLoading;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.advancedGameSetupScreen.AdvancedGameSetupScreen;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.engine.rendering.world.WorldSetupWrapper;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.family.BlockFamilyLibrary;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.shapes.BlockShapeImpl;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.world.block.tiles.BlockTile;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorInfo;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.zones.Zone;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.autoreload.AutoReloadAssetTypeManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.naming.Name;
import org.terasology.math.TeraMath;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UISlider;
import org.terasology.nui.widgets.UISliderOnChangeTriggeredListener;
import org.terasology.nui.widgets.UIText;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Sets up the Universe for a user. Displays a list of {@link WorldGenerator}
 * for a particular game template.
 */
public class UniverseSetupScreen extends CoreScreenLayer implements UISliderOnChangeTriggeredListener {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:universeSetupScreen");

    private static final Logger logger = LoggerFactory.getLogger(UniverseSetupScreen.class);

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private GameEngine gameEngine;

    private ModuleEnvironment environment;
    private ModuleAwareAssetTypeManager assetTypeManager;
    private Context context;
    private UniverseWrapper universeWrapper;
    private UISlider zoomSlider;
    private Texture texture;
    private PreviewGenerator previewGen;
    private UIImage previewImage;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        final UIDropdownScrollable<WorldGeneratorInfo> worldGenerators = find("worldGenerators", UIDropdownScrollable.class);
        if (worldGenerators != null) {
            worldGenerators.bindOptions(new ReadOnlyBinding<List<WorldGeneratorInfo>>() {
                @Override
                public List<WorldGeneratorInfo> get() {
                    // grab all the module names and their dependencies
                    // This grabs modules from `config.getDefaultModSelection()` which is updated in AdvancedGameSetupScreen
                    final Set<Name> enabledModuleNames = new HashSet<>(getAllEnabledModuleNames());
                    final List<WorldGeneratorInfo> result = Lists.newArrayList();
                    for (WorldGeneratorInfo option : worldGeneratorManager.getWorldGenerators()) {
                        //TODO: There should not be a reference from the engine to some module.
                        //      The engine must be agnostic to what modules may do.
                        if (enabledModuleNames.contains(option.getUri().getModuleName())
                                && !option.getUri().toString().equals("CoreWorlds:heightMap")) {
                            result.add(option);
                        }
                    }

                    return result;
                }
            });
            worldGenerators.setVisibleOptions(worldGenerators.getOptions().size());
            worldGenerators.bindSelection(new Binding<WorldGeneratorInfo>() {
                @Override
                public WorldGeneratorInfo get() {
                    // get the default generator from the config. This is likely to have a user triggered selection.
                    WorldGeneratorInfo info = worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
                    if (info != null && getAllEnabledModuleNames().contains(info.getUri().getModuleName())) {
                        set(info);
                        return info;
                    }

                    // just use the first available generator
                    for (WorldGeneratorInfo worldGenInfo : worldGeneratorManager.getWorldGenerators()) {
                        if (getAllEnabledModuleNames().contains(worldGenInfo.getUri().getModuleName())) {
                            set(worldGenInfo);
                            return worldGenInfo;
                        }
                    }

                    return null;
                }

                @Override
                public void set(WorldGeneratorInfo value) {
                    if (value != null) {
                        if (universeWrapper.getTargetWorld() == null || value != universeWrapper.getTargetWorld().getWorldGeneratorInfo()) {
                            config.getWorldGeneration().setDefaultGenerator(value.getUri());
                            addNewWorld(value);
                        }
                    }
                }
            });
            worldGenerators.setOptionRenderer(new StringTextRenderer<WorldGeneratorInfo>() {
                @Override
                public String getString(WorldGeneratorInfo value) {
                    if (value != null) {
                        return value.getDisplayName();
                    }
                    return "";
                }
            });
        }

        final UIText seedField = find("seed", UIText.class);
        seedField.bindText(new Binding<String>() {
            @Override
            public String get() {
                return universeWrapper.getSeed();
            }

            @Override
            public void set(String value) {
                universeWrapper.setSeed(value);
            }
        });

        zoomSlider = find("zoomSlider", UISlider.class);
        if (zoomSlider != null) {
            zoomSlider.setValue(2f);
            zoomSlider.setUiSliderOnChangeTriggeredListener(this);
        }

        final UIText worldName = find("worldName", UIText.class);
        worldName.bindText(new Binding<String>() {
            @Override
            public String get() {
                if (universeWrapper.getTargetWorld() == null) {
                    return "";
                }
                return universeWrapper.getTargetWorld().getWorldName().toString();
            }

            @Override
            public void set(String value) {
                // no-op
                // field should be read-only
            }
        });

        WidgetUtil.trySubscribe(this, "reRoll", button -> {
            if (universeWrapper.getTargetWorld() != null) {
                universeWrapper.setSeed(createRandomSeed());
                universeWrapper.getTargetWorld().getWorldGenerator().setWorldSeed(universeWrapper.getSeed());
                updatePreview();
            } else {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("No world generator selected!", "Please select a world generator first!");
            }
        });

        WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);
        WidgetUtil.trySubscribe(this, "config", button -> {
            if (universeWrapper.getTargetWorld() != null) {
                try {
                    if (!universeWrapper.getTargetWorld().getWorldName().isEmpty()) {
                        worldSetupScreen.setWorld(context, universeWrapper.getTargetWorld());
                        triggerForwardAnimation(worldSetupScreen);
                    } else {
                        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                                .setMessage("Worlds List Empty!", "No world found to configure.");
                    }
                } catch (UnresolvedWorldGeneratorException e) {
                    e.printStackTrace();
                }
            } else {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("No world generator selected!", "Please select a world generator first!");
            }
        });

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "play", button -> {
            if (universeWrapper.getTargetWorld() == null) {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("No world generator selected!", "Please select a world generator first!");
                return;
            }

            final GameManifest gameManifest = GameManifestProvider.createGameManifest(universeWrapper, moduleManager, config);
            if (gameManifest != null) {
                gameEngine.changeState(new StateLoading(gameManifest, (universeWrapper.getLoadingAsServer())
                        ? NetworkMode.DEDICATED_SERVER
                        : NetworkMode.NONE));
            } else {
                getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", "Can't create new game!");
            }
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        if (universeWrapper.getTargetWorld() != null) {
            try {
                if (universeWrapper.getTargetWorld().getWorldGenerator() == null) {
                    WorldGenerator worldGenerator = WorldGeneratorManager.createWorldGenerator(universeWrapper.getTargetWorld()
                            .getWorldGeneratorInfo().getUri(), context, environment);
                    universeWrapper.getTargetWorld().setWorldGenerator(worldGenerator);
                }
                if (universeWrapper.getTargetWorld().getWorldGenerator().getWorldSeed().isEmpty()) {
                    universeWrapper.getTargetWorld().getWorldGenerator().setWorldSeed(universeWrapper.getSeed());
                }
                previewGen = new FacetLayerPreview(environment, universeWrapper.getTargetWorld().getWorldGenerator());
                updatePreview();
            } catch (UnresolvedWorldGeneratorException e) {
                e.printStackTrace();
            }
        }
    }

    private Set<Name> getAllEnabledModuleNames() {
        final Set<Name> enabledModules = Sets.newHashSet();
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            enabledModules.add(moduleName);
            recursivelyAddModuleDependencies(enabledModules, moduleName);
        }

        return enabledModules;
    }


    private void recursivelyAddModuleDependencies(Set<Name> modules, Name moduleName) {
        Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
        if (module != null) {
            for (DependencyInfo dependencyInfo : module.getMetadata().getDependencies()) {
                modules.add(dependencyInfo.getId());
                recursivelyAddModuleDependencies(modules, dependencyInfo.getId());
            }
        }
    }

    /**
     * Called whenever the user decides to add a new world.
     * @param worldGeneratorInfo The {@link WorldGeneratorInfo} object for the new world.
     */
    private void addNewWorld(WorldGeneratorInfo worldGeneratorInfo) {
        try {
            WorldGenerator worldGenerator = WorldGeneratorManager.createWorldGenerator(worldGeneratorInfo.getUri(), context, environment);
            worldGenerator.setWorldSeed(universeWrapper.getSeed());
            universeWrapper.setTargetWorld(new WorldSetupWrapper(new Name(worldGeneratorInfo.getDisplayName()), worldGeneratorInfo, worldGenerator));
        } catch (UnresolvedWorldGeneratorException e) {
            //TODO: this will likely fail at game creation time later-on due to lack of world generator - don't just ignore this
            e.printStackTrace();
        }

        genTexture();
        List<Zone> previewZones = Lists.newArrayList(universeWrapper.getTargetWorld().getWorldGenerator().getZones())
                .stream()
                .filter(z -> !z.getPreviewLayers().isEmpty())
                .collect(Collectors.toList());
        if (previewZones.isEmpty()) {
            previewGen = new FacetLayerPreview(environment, universeWrapper.getTargetWorld().getWorldGenerator());
        }

        updatePreview();
    }

    /**
     * This method switches the environment of the game to a temporary one needed for
     * creating a game. It creates a new {@link Context} and only puts the minimum classes
     * needed for successful game creation.
     * @param wrapper takes the {@link AdvancedGameSetupScreen} and pushes it into the new context.
     */
    public void setEnvironment(UniverseWrapper wrapper) {
        universeWrapper = wrapper;
        context = new ContextImpl();
        CoreRegistry.setContext(context);
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        context.put(ReflectFactory.class, reflectFactory);
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategyLibrary);
        context.put(NUIManager.class, getManager());
        context.put(UniverseSetupScreen.class, this);
        assetTypeManager = new AutoReloadAssetTypeManager();
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        context.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        context.put(ModuleManager.class, moduleManager);
        context.put(UniverseWrapper.class, universeWrapper);

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
        if (result.isSuccess()) {
            environment = moduleManager.loadEnvironment(result.getModules(), false);
            context.put(ModuleEnvironment.class, environment);
            context.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment, context));
            initAssets();

            EnvironmentSwitchHandler environmentSwitcher = new EnvironmentSwitchHandler();
            context.put(EnvironmentSwitchHandler.class, environmentSwitcher);

            environmentSwitcher.handleSwitchToPreviewEnvironment(context, environment);
        }
    }

    private void initAssets() {

        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        BlockFamilyLibrary library =  new BlockFamilyLibrary(environment, context);

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.createAssetType(BlockShape.class, BlockShapeImpl::new, "shapes");
        assetTypeManager.createAssetType(BlockSounds.class, BlockSounds::new, "blockSounds");
        assetTypeManager.createAssetType(BlockTile.class, BlockTile::new, "blockTiles");

        AssetType<BlockFamilyDefinition, BlockFamilyDefinitionData> blockFamilyDefinitionDataAssetType = assetTypeManager.createAssetType(
                BlockFamilyDefinition.class, BlockFamilyDefinition::new, "blocks");
        assetTypeManager.getAssetFileDataProducer(blockFamilyDefinitionDataAssetType).addAssetFormat(
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));
        assetTypeManager.createAssetType(UISkinAsset.class, UISkinAsset::new, "skins");
        assetTypeManager.createAssetType(BehaviorTree.class, BehaviorTree::new, "behaviors");
        assetTypeManager.createAssetType(UIElement.class, UIElement::new, "ui");
    }

    /**
     * @return the selected world in the drop-down.
     */
    public WorldSetupWrapper getSelectedWorld() {
        return universeWrapper.getTargetWorld();
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

    private String createRandomSeed() {
        String seed = new FastRandom().nextString(32);
        return seed;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    @Override
    public void onSliderValueChanged(float val) {
        if (universeWrapper.getTargetWorld() != null) {
            updatePreview();
        }
    }
}

