// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.resources.ModuleFileSource;
import org.terasology.gestalt.naming.Name;
import org.terasology.moduletestingenvironment.Engines;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WorkspaceBehaviorsTests {

    @TestFactory
    Stream<DynamicNode> behaviours() {
        return template(
                // Filter modules
                haveAsset("behaviors").or(havePrefab(BehaviorComponent.class)),
                // Create assetTests
                assetManagerRef -> Stream.of(
                        asset(
                                assetManagerRef,
                                BehaviorTree.class,
                                (b) -> Assertions.assertNotNull(b.getData())
                        ),
                        asset(
                                assetManagerRef,
                                Prefab.class,
                                prefab -> prefab.hasComponent(BehaviorComponent.class),
                                prefab -> Assertions.assertNotNull(prefab.getComponent(BehaviorComponent.class).tree)

                        ))
        );
    }


    Stream<DynamicNode> template(
            BiPredicate<ModuleManager, Name> moduleFilter,
            Function<AtomicReference<AssetManager>,
                    Stream<DynamicNode>> testsCreator) {
        ModuleManager moduleManager = createModuleManager();
        return moduleManager.getRegistry()
                .getModuleIds()
                .stream()
                // use filter over modules
                .filter(name -> moduleFilter.test(moduleManager, name))
                // create tests
                .map(moduleName -> {
                    // Create engine
                    EnginesAccessor engine = new EnginesAccessor(Set.of(moduleName.toString()), null);
                    // Provide `Lazy` reference for asset tests
                    AtomicReference<AssetManager> assetManagerRef = new AtomicReference<>();
                    return DynamicContainer.dynamicContainer(String.format("Module - %s", moduleName),
                            Stream.of(
                                    // engine test
                                    DynamicTest.dynamicTest("setup", () -> {
                                        engine.setup();
                                        assetManagerRef.set(engine.getHostContext().get(AssetManager.class));
                                    }),
                                    // Container with asset tests
                                    DynamicContainer.dynamicContainer("Assets tests", testsCreator.apply(assetManagerRef)),
                                    // engine teardown test
                                    DynamicTest.dynamicTest("tearDown", engine::tearDown)));
                });
    }

    private ModuleManager createModuleManager() {
        System.setProperty(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY, "true");
        return new ModuleManager("");
    }

    private BiPredicate<ModuleManager, Name> haveAsset(String assetName) {
        return (moduleManager, name) -> {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(name);
            ModuleFileSource resources = module.getResources();

            boolean haveAsset = !resources.getFilesInPath(true, "assets/" + assetName).isEmpty();

            boolean haveAssetOverride =
                    resources.getFilesInPath(false, "overrides")
                            .stream()
                            .anyMatch(fr -> fr.getPath().contains(assetName));
            boolean haveAssetDelta =
                    resources.getFilesInPath(false, "deltas")
                            .stream()
                            .anyMatch(fr -> fr.getPath().contains(assetName));

            return haveAsset || haveAssetOverride || haveAssetDelta;
        };
    }


    private <T extends Component<T>> BiPredicate<ModuleManager, Name> havePrefab(Class<T> componentClass) {
        return (moduleManager, name) -> {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(name);
            ModuleFileSource resources = module.getResources();

            return resources.getFiles().stream()
                    .filter(fr -> fr.toString().contains("prefabs") && fr.toString().contains(".prefab"))
                    .anyMatch(fr -> {
                        try (InputStream inputStream = fr.open()) {
                            byte[] bytes = ByteStreams.toByteArray(inputStream);
                            String content = new String(bytes, TerasologyConstants.CHARSET);
                            String componentName = componentClass.getSimpleName();
                            return content.contains(componentName);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    });
        };
    }

    private <T extends AssetData, A extends Asset<T>> DynamicContainer asset(AtomicReference<AssetManager> assetManager,
                                                                             Class<A> assetClazz,
                                                                             Consumer<A> validator) {
        return asset(assetManager, assetClazz, (a) -> true, validator);

    }

    private <T extends AssetData, A extends Asset<T>> DynamicContainer asset(AtomicReference<AssetManager> assetManager,
                                                                             Class<A> assetClazz,
                                                                             Predicate<A> filter,
                                                                             Consumer<A> validator) {

        return DynamicContainer.dynamicContainer(String.format("AssetType: %s", assetClazz.getSimpleName()),
                DynamicTest.stream(
                        // Special generator for 1 value for `lazy` getting.
                        Stream.generate(() -> assetManager.get().getAvailableAssets(assetClazz).stream())
                                .limit(1)
                                .flatMap(s -> s)
                                .filter(urn -> filter.test(assetManager.get().getAsset(urn, assetClazz).get())),
                        ResourceUrn::toString,
                        urn -> {
                            A asset = assetManager.get().getAsset(urn, assetClazz).get();
                            validator.accept(asset);
                        }
                ));

    }

    // Just grant access to protected methods.
    public static class EnginesAccessor extends Engines {

        public EnginesAccessor(Set<String> dependencies, String worldGeneratorUri) {
            super(dependencies, worldGeneratorUri);
        }

        @Override
        protected void setup() {
            super.setup();
        }

        @Override
        protected void tearDown() {
            super.tearDown();
        }
    }
}
