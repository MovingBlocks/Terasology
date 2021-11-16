// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.workspace.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;

import java.util.stream.Stream;

public class WorkspaceBehaviorsTests extends AssetTesting {

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

}
