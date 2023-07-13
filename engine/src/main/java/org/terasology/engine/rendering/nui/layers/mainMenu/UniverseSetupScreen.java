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
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.advancedGameSetupScreen.AdvancedGameSetupScreen;
import org.terasology.engine.rendering.world.WorldSetupWrapper;
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
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sets up the Universe for a user. Displays a list of {@link WorldGenerator}
 * for a particular game template.
 */
public class UniverseSetupScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:universeSetupScreen");

    private static final Logger logger = LoggerFactory.getLogger(UniverseSetupScreen.class);

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    private ModuleEnvironment environment;
    private ModuleAwareAssetTypeManager assetTypeManager;
    private Context context;
    private WorldSetupWrapper selectedWorld;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        final UIDropdownScrollable<WorldGeneratorInfo> worldGenerator = find("worldGenerators", UIDropdownScrollable.class);
        if (worldGenerator != null) {
            worldGenerator.bindOptions(new ReadOnlyBinding<List<WorldGeneratorInfo>>() {
                @Override
                public List<WorldGeneratorInfo> get() {
                    // grab all the module names and their dependencies
                    // This grabs modules from `config.getDefaultModSelection()` which is updated in AdvancedGameSetupScreen
                    final Set<Name> enabledModuleNames = new HashSet<>(getAllEnabledModuleNames());
                    final List<WorldGeneratorInfo> result = Lists.newArrayList();
                    for (WorldGeneratorInfo option : worldGeneratorManager.getWorldGenerators()) {
                        if (enabledModuleNames.contains(option.getUri().getModuleName())) {
                            result.add(option);
                        }
                    }

                    return result;
                }
            });
            worldGenerator.setVisibleOptions(3);
            worldGenerator.bindSelection(new Binding<WorldGeneratorInfo>() {
                @Override
                public WorldGeneratorInfo get() {
                    // get the default generator from the config. This is likely to have a user triggered selection.
                    WorldGeneratorInfo info = worldGeneratorManager.getWorldGeneratorInfo(config.getWorldGeneration().getDefaultGenerator());
                    if (info != null && getAllEnabledModuleNames().contains(info.getUri().getModuleName())) {
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
                        config.getWorldGeneration().setDefaultGenerator(value.getUri());
                    }
                }
            });
            worldGenerator.setOptionRenderer(new StringTextRenderer<WorldGeneratorInfo>() {
                @Override
                public String getString(WorldGeneratorInfo value) {
                    if (value != null) {
                        return value.getDisplayName();
                    }
                    return "";
                }
            });
        }

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "continue", button -> {
            final WorldPreGenerationScreen worldPreGenerationScreen =
                    getManager().createScreen(WorldPreGenerationScreen.ASSET_URI, WorldPreGenerationScreen.class);
            addNewWorld(worldGenerator.getSelection());
            if (selectedWorld != null) {
                final WaitPopup<Boolean> loadPopup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
                loadPopup.setMessage("Loading", "please wait ...");
                loadPopup.onSuccess(result -> {
                    if (result != null && result) {
                        triggerForwardAnimation(worldPreGenerationScreen);
                    } else {
                        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                                .setMessage("Error", "Can't load world pre generation screen! Please, try again!");
                    }
                });
                loadPopup.startOperation(() -> {
                    try {
                        worldPreGenerationScreen.setEnvironment(context);
                    } catch (UnresolvedWorldGeneratorException e) {
                        return false;
                    }
                    return true;
                }, true);
            } else {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("Worlds List Empty!", "Please select a world generator and add words to the dropdown!");
            }
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        selectedWorld = null;        
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
        String selectedWorldName = worldGeneratorInfo.getDisplayName();

        selectedWorld = new WorldSetupWrapper(new Name(selectedWorldName), worldGeneratorInfo);
    }

    /**
     * This method switches the environment of the game to a temporary one needed for
     * creating a game. It creates a new {@link Context} and only puts the minimum classes
     * needed for successful game creation.
     * @param wrapper takes the {@link AdvancedGameSetupScreen} and pushes it into the new context.
     */
    public void setEnvironment(UniverseWrapper wrapper) {
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
        context.put(UniverseWrapper.class, wrapper);

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
     * Create a list of the names of the world, so that they can be displayed as simple String
     * in the drop-down.
     * @return A list of world names encoded as a String
     */
    @Deprecated
    public List<String> worldNames() {
        final List<String> worldNamesList = Lists.newArrayList();
        if (selectedWorld != null) {
            worldNamesList.add(selectedWorld.getWorldName().toString());
        }
        return worldNamesList;
    }

    /**
     * @return the selected world in the drop-down.
     */
    public WorldSetupWrapper getSelectedWorld() {
        return selectedWorld;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

