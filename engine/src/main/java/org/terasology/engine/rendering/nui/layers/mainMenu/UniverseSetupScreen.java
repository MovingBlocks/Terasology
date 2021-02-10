// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.asset.UIData;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinData;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.world.WorldSetupWrapper;
import org.terasology.world.block.family.BlockFamilyLibrary;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.sounds.BlockSoundsData;
import org.terasology.world.block.tiles.BlockTile;
import org.terasology.world.block.tiles.TileData;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sets up the Universe for a user. Displays a list of {@link org.terasology.world.generator.WorldGenerator}
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

    private List<WorldSetupWrapper> worlds = Lists.newArrayList();
    private ModuleEnvironment environment;
    private ModuleAwareAssetTypeManager assetTypeManager;
    private Context context;
    private int worldNumber;
    private String selectedWorld = "";
    private int indexOfSelectedWorld;
    private WorldSetupWrapper copyOfSelectedWorld;

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
        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        worldsDropdown.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return selectedWorld;
            }

            @Override
            public void set(String value) {
                selectedWorld = value;
                indexOfSelectedWorld = findIndex(worlds, selectedWorld);
            }
        });

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WidgetUtil.trySubscribe(this, "worldConfig", button -> {
            final WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);
            try {
                if (!worlds.isEmpty() || !selectedWorld.isEmpty()) {
                    worldSetupScreen.setWorld(context, findWorldByName(), worldsDropdown);
                    triggerForwardAnimation(worldSetupScreen);
                } else {
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Worlds List Empty!", "No world found to configure.");
                }
            } catch (UnresolvedWorldGeneratorException e) {
                logger.error("Can't configure the world! due to {}", e.getMessage());
            }
        });

        WidgetUtil.trySubscribe(this, "addGenerator", button -> {
            //TODO: there should not be a reference from the engine to some module - the engine must be agnostic to what
            //      modules may do
            if (worldGenerator.getSelection().getUri().toString().equals("CoreWorlds:heightMap")) {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(
                        "HeightMap not supported", "HeightMap is not supported for advanced setup right now, a game template will be introduced soon.");
            } else {
                addNewWorld(worldGenerator.getSelection());
                worldsDropdown.setOptions(worldNames());
            }
        });

        WidgetUtil.trySubscribe(this, "continue", button -> {
            final WorldPreGenerationScreen worldPreGenerationScreen = getManager().createScreen(WorldPreGenerationScreen.ASSET_URI, WorldPreGenerationScreen.class);
            if (!worlds.isEmpty()) {
                final WaitPopup<Boolean> loadPopup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
                loadPopup.setMessage("Loading", "please wait ...");
                loadPopup.onSuccess(result -> {
                    if (result != null && result) {
                        triggerForwardAnimation(worldPreGenerationScreen);
                    } else {
                        getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", "Can't load world pre generation screen! Please, try again!");
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
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(
                        "Worlds List Empty!", "Please select a world generator and add words to the dropdown!");
            }
        });

        WidgetUtil.trySubscribe(this, "mainMenu", button -> {
            getManager().pushScreen("engine:mainMenuScreen");
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();

        worlds.clear();
        worldNumber = 0;
        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        if (worldsDropdown != null) {
            worldsDropdown.setOptions(worldNames());
        }
        selectedWorld = "";
        indexOfSelectedWorld = findIndex(worlds, selectedWorld);
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
     * returns true if 'name' matches (case-insensitive) with another world already present
     * @param name The world name to be checked
     */
    public boolean worldNameMatchesAnother(String name) {
        boolean taken = false;

        for (WorldSetupWrapper worldTaken: worlds) {
            if (worldTaken.getWorldName().toString().equalsIgnoreCase(name)) {
                taken = true;
                break;
            }
        }

        return taken;
    }

    /**
     * Called whenever the user decides to add a new world.
     * @param worldGeneratorInfo The {@link WorldGeneratorInfo} object for the new world.
     */
    private void addNewWorld(WorldGeneratorInfo worldGeneratorInfo) {
        String selectedWorldName = worldGeneratorInfo.getDisplayName();

        while (worldNameMatchesAnother(selectedWorldName + "-" + worldNumber)) {
            ++worldNumber;
        }

        selectedWorld = worldGeneratorInfo.getDisplayName() + '-' + worldNumber;
        worlds.add(new WorldSetupWrapper(new Name(worldGeneratorInfo.getDisplayName() + '-' + worldNumber), worldGeneratorInfo));
        indexOfSelectedWorld = findIndex(worlds, selectedWorld);
        ++worldNumber;
    }

    /**
     * This method refreshes the worlds drop-down menu when world name is changed and updates variable selectedWorld.
     * @param worldsDropdown the drop-down to work on
     */
    public void refreshWorldDropdown(UIDropdownScrollable worldsDropdown) {
        worldsDropdown.setOptions(worldNames());
        copyOfSelectedWorld = worlds.get(indexOfSelectedWorld);
        selectedWorld = copyOfSelectedWorld.getWorldName().toString();
    }

    /**
     * This method switches the environment of the game to a temporary one needed for
     * creating a game. It creates a new {@link Context} and only puts the minimum classes
     * needed for successful game creation.
     * @param wrapper takes the {@link org.terasology.rendering.nui.layers.mainMenu.advancedGameSetupScreen.AdvancedGameSetupScreen} and pushes it into the new context.
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
        assetTypeManager = new ModuleAwareAssetTypeManager();
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

    /**
     * Looks for the index of a selected world from the given list.
     * @param worldsList the list to search
     * @param worldName the name of the world to find
     * @return the found index value or -1 if not found
     */
    private int findIndex(List<WorldSetupWrapper> worldsList, String worldName) {
        for (int i = 0; i < worldsList.size(); i++) {
            WorldSetupWrapper currentWorldFromList = worldsList.get(i);
            Name customName = currentWorldFromList.getWorldName();
            if (customName.toString().equals(worldName)) {
                return i;
            }
        }
        return -1;
    }

    private void initAssets() {

        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        BlockFamilyLibrary library =  new BlockFamilyLibrary(environment, context);

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Prefab.class,
                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, false, "prefabs");
        assetTypeManager.registerCoreAssetType(BlockShape.class,
                (AssetFactory<BlockShape, BlockShapeData>) BlockShapeImpl::new, "shapes");
        assetTypeManager.registerCoreAssetType(BlockSounds.class,
                (AssetFactory<BlockSounds, BlockSoundsData>) BlockSounds::new, "blockSounds");
        assetTypeManager.registerCoreAssetType(BlockTile.class,
                (AssetFactory<BlockTile, TileData>) BlockTile::new, "blockTiles");
        assetTypeManager.registerCoreAssetType(BlockFamilyDefinition.class,
                (AssetFactory<BlockFamilyDefinition, BlockFamilyDefinitionData>) BlockFamilyDefinition::new, "blocks");
        assetTypeManager.registerCoreFormat(BlockFamilyDefinition.class,
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));
        assetTypeManager.registerCoreAssetType(UISkin.class,
                (AssetFactory<UISkin, UISkinData>) UISkin::new, "skins");
        assetTypeManager.registerCoreAssetType(BehaviorTree.class,
                (AssetFactory<BehaviorTree, BehaviorTreeData>) BehaviorTree::new, false, "behaviors");
        assetTypeManager.registerCoreAssetType(UIElement.class,
                (AssetFactory<UIElement, UIData>) UIElement::new, "ui");
    }

    /**
     * Create a list of the names of the world, so that they can be displayed as simple String
     * in the drop-down.
     * @return A list of world names encoded as a String
     */
    public List<String> worldNames() {
        final List<String> worldNamesList = Lists.newArrayList();
        for (WorldSetupWrapper world : worlds) {
            worldNamesList.add(world.getWorldName().toString());
        }
        return worldNamesList;
    }

    /**
     * This method takes the name of the selected world as String and return the corresponding
     * WorldSetupWrapper object.
     * @return {@link WorldSetupWrapper} object.
     */
    public WorldSetupWrapper findWorldByName() {
        for (WorldSetupWrapper world : worlds) {
            if (world.getWorldName().toString().equals(selectedWorld)) {
                return world;
            }
        }
        return null;
    }

    public List<WorldSetupWrapper> getWorldsList() {
        return worlds;
    }

    /**
     * @return the selcted world in the drop-down.
     */
    public String getSelectedWorld() {
        return selectedWorld;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}

