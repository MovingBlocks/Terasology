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
