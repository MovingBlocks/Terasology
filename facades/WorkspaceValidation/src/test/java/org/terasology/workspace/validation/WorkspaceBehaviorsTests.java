// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.moduletestingenvironment.Engines;

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
