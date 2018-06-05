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
import com.google.common.collect.Sets;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
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
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
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
import org.terasology.rendering.world.World;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.internal.WorldGeneratorInfo;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UniverseSetupScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:universeSetupScreen");
    private String selectedWorld = "";

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    private ModuleEnvironment environment;
    private ModuleAwareAssetTypeManager assetTypeManager;
    private Context context;
    List<World> worlds = Lists.newArrayList();
    int worldNumber = 0;

    @Override
    public void initialise() {

        ModuleConfig moduleConfig = config.getDefaultModSelection();


        final UIDropdownScrollable<WorldGeneratorInfo> worldGenerator = find("worldGenerators", UIDropdownScrollable.class);
        if (worldGenerator != null) {
            worldGenerator.bindOptions(new ReadOnlyBinding<List<WorldGeneratorInfo>>() {
                @Override
                public List<WorldGeneratorInfo> get() {
                    // grab all the module names and their dependencies
                    // This grabs modules from `config.getDefaultModSelection()` which is updated in AdvancedGameSetupScreen
                    Set<Name> enabledModuleNames = getAllEnabledModuleNames().stream().collect(Collectors.toSet());
                    List<WorldGeneratorInfo> result = Lists.newArrayList();
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
            }
        });

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );

        WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);
        WidgetUtil.trySubscribe(this, "worldConfig", button -> {
            try {
                if (!worlds.isEmpty() || !selectedWorld.isEmpty()) {
                    worldSetupScreen.setWorld(context, findWorldByName());
                    triggerForwardAnimation(worldSetupScreen);
                } else {
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Worlds List Empty!", "No world found to configure.");
                }
            } catch (UnresolvedWorldGeneratorException e) {
                e.getMessage();
            }
        });

        WidgetUtil.trySubscribe(this, "addGenerator", button -> {
            addNewWorld(worldGenerator.getSelection());
            List<World> worldOptions = worlds;
            worldsDropdown.setOptions(worldNames());
            //triggerForwardAnimation(worldSetupScreen);
        });

        WorldPreGenerationScreen worldPreGenerationScreen = getManager().createScreen(WorldPreGenerationScreen.ASSET_URI, WorldPreGenerationScreen.class);
        WidgetUtil.trySubscribe(this, "continue", button -> {
            if (!worlds.isEmpty()) {
                try {
                    worldPreGenerationScreen.setEnvironment(context);
                    triggerForwardAnimation(worldPreGenerationScreen);
                } catch (UnresolvedWorldGeneratorException e) {
                    e.getMessage();
                }
            } else {
                getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Worlds List Empty!", "Please select a world generator and add words to the dropdown!");
            }
        });
    }

    @Override
    public void onOpened() {
        worlds.clear();
        worldNumber = 0;
        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        List<World> worldOptions = worlds;
        worldsDropdown.setOptions(worldNames());
        selectedWorld = "";
    }

    private Set<Name> getAllEnabledModuleNames() {
        Set<Name> enabledModules = Sets.newHashSet();
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

    private void addNewWorld(WorldGeneratorInfo worldGeneratorInfo) {
        selectedWorld = worldGeneratorInfo.getDisplayName() + '-' + worldNumber;
        worlds.add(new World(new Name(worldGeneratorInfo.getDisplayName() + '-' + worldNumber), worldGeneratorInfo ));
        worldNumber++;
    }

    public void setEnvironment() {
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
        DefaultBlockFamilyFactoryRegistry familyFactoryRegistry = new DefaultBlockFamilyFactoryRegistry();
        context.put(BlockFamilyFactoryRegistry.class, familyFactoryRegistry);

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
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager(), familyFactoryRegistry));
        assetTypeManager.registerCoreAssetType(UISkin.class,
                (AssetFactory<UISkin, UISkinData>) UISkin::new, "skins");
        assetTypeManager.registerCoreAssetType(BehaviorTree.class,
                (AssetFactory<BehaviorTree, BehaviorTreeData>) BehaviorTree::new, false, "behaviors");
        assetTypeManager.registerCoreAssetType(UIElement.class,
                (AssetFactory<UIElement, UIData>) UIElement::new, "ui");

    }

    public List<String> worldNames() {
        List<String> worldNamesList = Lists.newArrayList();
        for (World world: worlds) {
            worldNamesList.add(world.getWorldName().toString());
        }
        return worldNamesList;
    }

    public World findWorldByName() {
        for (World world: worlds) {
            if (world.getWorldName().toString().equals(selectedWorld)) {
                return world;
            }
        }
        return null;
    }

    public List<World> getWorldsList() {
        return worlds;
    }

    public String getSelectedWorld() {
        return selectedWorld;
    }

}

