/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.StaticSound;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.asset.BehaviorTreeFormat;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Behavior tree system
 * <p/>
 * Each entity with BehaviorComponent is kept under control by this system. For each such entity a behavior tree
 * is loaded and an interpreter is started.
 * <p/>
 * Modifications made to a behavior tree will reflect to all entities using this tree.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(CollectiveBehaviorSystem.class)
public class CollectiveBehaviorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final Name BEHAVIORS = new Name("Behaviors");
    private static final Logger logger = LoggerFactory.getLogger(BehaviorSystem.class);
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;

    private List<BehaviorTree> trees = Lists.newArrayList();

    @Override
    public void initialise() {
        List<ResourceUrn> uris = Lists.newArrayList();
        uris.addAll(new ArrayList<>(assetManager.getAvailableAssets(StaticSound.class)));
        for (ResourceUrn uri : assetManager.getAvailableAssets(BehaviorTree.class)) {
            try {
                Optional<BehaviorTree> asset = assetManager.getAsset(uri, BehaviorTree.class);
                asset.ifPresent(behaviorTree -> trees.add(behaviorTree));
            } catch (RuntimeException e) {
                logger.info("Failed to load behavior tree asset {}.", uri, e);
            }
        }
    }

    @ReceiveEvent
    public void onBehaviorAdded(OnAddedComponent event, EntityRef entityRef, CollectiveBehaviorComponent collectiveBehaviorComponent) {
        addEntity(entityRef, collectiveBehaviorComponent);
    }

    @ReceiveEvent
    public void onBehaviorActivated(OnActivatedComponent event, EntityRef entityRef, CollectiveBehaviorComponent collectiveBehaviorComponent) {
        addEntity(entityRef, collectiveBehaviorComponent);
    }

    @Override
    public void update(float delta) {
        Iterable<EntityRef> entities = entityManager.getEntitiesWith(CollectiveBehaviorComponent.class);
        for (EntityRef entity : entities) {
            CollectiveBehaviorComponent collectiveBehaviorComponent = entity.getComponent(CollectiveBehaviorComponent.class);
            collectiveBehaviorComponent.collectiveInterpreter.tick(delta);
        }
    }

    public BehaviorTree createTree(String name, BehaviorNode root) {
        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(root);
        BehaviorTree behaviorTree = assetManager.loadAsset(new ResourceUrn(BEHAVIORS, new Name(name.replaceAll("\\W+", ""))), data, BehaviorTree.class);
        trees.add(behaviorTree);
        save(behaviorTree);
        return behaviorTree;
    }

    public void save(BehaviorTree tree) {
        Path savePath;
        ResourceUrn uri = tree.getUrn();
        if (BEHAVIORS.equals(uri.getModuleName())) {
            savePath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve("assets").resolve("behaviors");
        } else {
            Path overridesPath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve("overrides");
            savePath = overridesPath.resolve(uri.getModuleName().toString()).resolve("behaviors");
        }
        BehaviorTreeFormat loader = new BehaviorTreeFormat();
        try {
            Files.createDirectories(savePath);
            Path file = savePath.resolve(uri.getResourceName() + ".behavior");
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                loader.save(fos, tree.getData());
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot save asset " + uri + " to " + savePath, e);
        }
    }

    public List<BehaviorTree> getTrees() {
        return trees;
    }

    public List<CollectiveInterpreter> getInterpreters() {
        List<CollectiveInterpreter> runners = Lists.newArrayList();
        for (EntityRef entity : entityManager.getEntitiesWith(CollectiveBehaviorComponent.class)) {
            CollectiveBehaviorComponent collectiveBehaviorComponent = entity.getComponent(CollectiveBehaviorComponent.class);
            if (collectiveBehaviorComponent.collectiveInterpreter != null) {
                runners.add(collectiveBehaviorComponent.collectiveInterpreter);
            }
        }

        runners.sort(Comparator.comparing(CollectiveInterpreter::toString));
        return runners;
    }

    public void treeModified(BehaviorTree tree) {
        for (EntityRef entity : entityManager.getEntitiesWith(CollectiveBehaviorComponent.class)) {
            CollectiveBehaviorComponent collectiveBehaviorComponent = entity.getComponent(CollectiveBehaviorComponent.class);
            if (collectiveBehaviorComponent.tree == tree) {
                collectiveBehaviorComponent.collectiveInterpreter.reset();
            }
        }
        save(tree);
    }

    private void addEntity(EntityRef entityRef, CollectiveBehaviorComponent collectiveInterpreter) {
        if (collectiveInterpreter.collectiveInterpreter == null) {
            Set<Actor> newActors = new HashSet<>();
            newActors.add(new Actor(entityRef));
            collectiveInterpreter.collectiveInterpreter = new CollectiveInterpreter(newActors);
            BehaviorTree tree = collectiveInterpreter.tree;
            if (tree != null) {
                collectiveInterpreter.collectiveInterpreter.setTree(tree);
            }
        }
    }
}
