// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.behavior.asset.BehaviorTreeData;
import org.terasology.engine.logic.behavior.asset.BehaviorTreeFormat;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.naming.Name;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Behavior tree system
 * <p/>
 * Each entity with BehaviorComponent is kept under control by this system. For each such entity a behavior tree is
 * loaded and an interpreter is started.
 * <p/>
 * Modifications made to a behavior tree will reflect to all entities using this tree.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(BehaviorSystem.class)
public class BehaviorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    public static final Name BEHAVIORS = new Name("Behaviors");
    private static final Logger logger = LoggerFactory.getLogger(BehaviorSystem.class);
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;

    private final List<BehaviorTree> trees = Lists.newArrayList();

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
    public void onBehaviorAdded(OnAddedComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
        addEntity(entityRef, behaviorComponent);
    }

    @ReceiveEvent
    public void onBehaviorActivated(OnActivatedComponent event, EntityRef entityRef,
                                    BehaviorComponent behaviorComponent) {
        addEntity(entityRef, behaviorComponent);
    }

    @Override
    public void update(float delta) {
        Iterable<EntityRef> entities = entityManager.getEntitiesWith(BehaviorComponent.class);
        for (EntityRef entity : entities) {
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            behaviorComponent.interpreter.tick(delta);
        }
    }

    public BehaviorTree createTree(String name, BehaviorNode root) {
        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(root);
        BehaviorTree behaviorTree = assetManager.loadAsset(new ResourceUrn(BEHAVIORS, new Name(name.replaceAll("\\W+"
                , ""))), data, BehaviorTree.class);
        trees.add(behaviorTree);
        save(behaviorTree);
        return behaviorTree;
    }

    public void save(BehaviorTree tree) {
        Path savePath;
        ResourceUrn uri = tree.getUrn();
        if (BEHAVIORS.equals(uri.getModuleName())) {
            savePath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve("org" +
                    "/terasology/assets").resolve("behaviors");
        } else {
            Path overridesPath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve(
                    "overrides");
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

    public List<Interpreter> getInterpreters() {
        List<Interpreter> runners = Lists.newArrayList();
        for (EntityRef entity : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            if (behaviorComponent.interpreter != null) {
                runners.add(behaviorComponent.interpreter);
            }
        }

        runners.sort(Comparator.comparing(Interpreter::toString));
        return runners;
    }

    public void treeModified(BehaviorTree tree) {
        for (EntityRef entity : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            if (behaviorComponent.tree == tree) {
                behaviorComponent.interpreter.reset();
            }
        }
        save(tree);
    }

    private void addEntity(EntityRef entityRef, BehaviorComponent behaviorComponent) {
        if (behaviorComponent.interpreter == null) {
            behaviorComponent.interpreter = new Interpreter(new Actor(entityRef));
            BehaviorTree tree = behaviorComponent.tree;
            if (tree != null) {
                behaviorComponent.interpreter.setTree(tree);
            }
        }
    }

}
