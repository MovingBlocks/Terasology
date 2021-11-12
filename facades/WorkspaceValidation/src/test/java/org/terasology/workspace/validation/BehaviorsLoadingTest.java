// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import java.util.stream.Stream;

@Tag("Mte")
@ExtendWith(MTEExtension.class)
@Dependencies("JoshariasSurvival")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BehaviorsLoadingTest {

    @In
    AssetManager assetManager;
    @In
    EntityManager entityManager;

    public Stream<Arguments> behaviors() {
        return assetManager.getAvailableAssets(BehaviorTree.class)
                .stream()
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("behaviors")
    void behaviorsTest(ResourceUrn urn) {
        Assertions.assertDoesNotThrow(() ->
                assetManager.getAsset(urn, BehaviorTree.class));
    }


    public Stream<Arguments> prefabs() {
        return assetManager.getAvailableAssets(Prefab.class)
                .stream()
                .map((p) -> assetManager.getAsset(p, Prefab.class).get())
                .filter((p) -> p.hasComponent(BehaviorComponent.class))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("prefabs")
    void prefabsTest(Prefab prefab, ModuleTestingHelper helper) {
        EntityRef ref = entityManager.create(prefab);
        Assertions.assertNotNull(ref.getComponent(BehaviorComponent.class).tree);
    }

}
