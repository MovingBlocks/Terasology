/*
 * Copyright 2014 MovingBlocks
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.StaticSound;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.asset.BehaviorTreeFormat;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Behavior tree system
 * <br><br>
 * Each entity with BehaviorComponent is kept under control by this system. For each such entity a behavior tree
 * is loaded and an interpreter is started.
 * <br><br>
 * Modifications made to a behavior tree will reflect to all entities using this tree.
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(BehaviorSystem.class)
public class BehaviorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final Name BEHAVIORS = new Name("Behaviors");

    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;

    private Map<EntityRef, Interpreter> entityInterpreters = Maps.newHashMap();
    private List<BehaviorTree> trees = Lists.newArrayList();

    @Override
    public void initialise() {
        List<ResourceUrn> uris = Lists.newArrayList();
        uris.addAll(assetManager.getAvailableAssets(StaticSound.class).stream().collect(Collectors.toList()));
        for (ResourceUrn uri : assetManager.getAvailableAssets(BehaviorTree.class)) {

            Optional<BehaviorTree> asset = assetManager.getAsset(uri, BehaviorTree.class);
            if (asset.isPresent()) {
                trees.add(asset.get());
            }
        }
    }

    @ReceiveEvent
    public void onBehaviorActivated(OnActivatedComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
        addEntity(entityRef, behaviorComponent);
    }

    @ReceiveEvent
    public void onBehaviorDeactivated(BeforeDeactivateComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
        if (behaviorComponent.tree != null) {
            entityInterpreters.remove(entityRef);
        }
    }

    @Override
    public void update(float delta) {
        for (Interpreter interpreter : entityInterpreters.values()) {
            interpreter.tick(delta);
        }
    }

    public BehaviorTree createTree(String name, Node root) {
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

    public List<Interpreter> getInterpreter() {
        List<Interpreter> interpreters = Lists.newArrayList();
        interpreters.addAll(entityInterpreters.values());
        Collections.sort(interpreters, (o1, o2) -> o1.toString().compareTo(o2.toString()));
        return interpreters;
    }

    public void treeModified(BehaviorTree tree) {
        entityInterpreters.values().forEach(Interpreter::reset);
        save(tree);
    }

    private void addEntity(EntityRef entityRef, BehaviorComponent behaviorComponent) {
        Interpreter interpreter = entityInterpreters.get(entityRef);
        if (interpreter == null) {
            interpreter = new Interpreter(new Actor(entityRef));
            BehaviorTree tree = behaviorComponent.tree;
            entityInterpreters.put(entityRef, interpreter);
            if (tree != null) {
                interpreter.start(tree.getRoot());
            }
        }
    }
}
