// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import com.google.common.collect.Streams;
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
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.resources.ModuleFileSource;
import org.terasology.moduletestingenvironment.Engines;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WorkspaceBehaviorsTests {

    @TestFactory
    Stream<DynamicNode> behaviours() {
        System.setProperty(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY, "true");
        ModuleManager temporary = new ModuleManager("");
        return temporary.getRegistry()
                .getModuleIds()
                .stream()
                .filter(name -> {
                    Module module = temporary.getRegistry().getLatestModuleVersion(name);
                    ModuleFileSource resources = module.getResources();

                    boolean haveBehaviors = !resources.getFilesInPath(true, "assets/behaviors").isEmpty();
                    boolean haveBehaviorOverride =
                            resources.getFilesInPath(false, "overrides")
                                    .stream()
                                    .anyMatch(fr -> fr.getPath().contains("behaviors"));
                    boolean haveBehaviorDelta =
                            resources.getFilesInPath(false, "deltas")
                                    .stream()
                                    .anyMatch(fr -> fr.getPath().contains("behaviors"));

                    boolean havePrefab = resources.getFiles().stream()
                            .filter(fr -> fr.toString().contains("behaviours") && fr.toString().contains(".prefab"))
                            .anyMatch(fr -> {
                                try (InputStream inputStream = fr.open()) {
                                    byte[] bytes = ByteStreams.toByteArray(inputStream);
                                    String content = new String(bytes, TerasologyConstants.CHARSET);
                                    return content.contains(BehaviorComponent.class.getSimpleName());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            });

                    return haveBehaviors || haveBehaviorOverride || haveBehaviorDelta || havePrefab;
                })
                .map(moduleName -> {
                    EnginesAccessor engine = new EnginesAccessor(Set.of(moduleName.toString()), null);
                    AtomicReference<AssetManager> assetManagerRef = new AtomicReference<>();
                    return DynamicContainer.dynamicContainer(String.format("Module - %s", moduleName), Streams.concat(
                            Stream.of(engine).map((e) ->
                                    DynamicTest.dynamicTest("setup", () -> {
                                        e.setup();
                                        assetManagerRef.set(e.getHostContext().get(AssetManager.class));
                                    })
                            ),
                            Stream.of(DynamicContainer.dynamicContainer("Assets tests", Stream.of(
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

                                    )))),
                            Stream.of(DynamicTest.dynamicTest("tearDown", engine::tearDown))));
                });
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
                        Stream.generate(() -> assetManager.get().getAvailableAssets(assetClazz).stream())
                                .limit(1)
                                .flatMap(s -> s)
                                .filter(urn -> filter.test(assetManager.get().getAsset(urn, assetClazz).get())),
                        ResourceUrn::toString,
                        urn -> {
                            A asset = (A) assetManager.get().getAsset(urn, assetClazz).get();
                            validator.accept(asset);
                        }
                ));

    }

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
