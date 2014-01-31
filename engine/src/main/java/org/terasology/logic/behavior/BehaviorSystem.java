/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.module.UriUtil;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Behavior tree system
 * <p/>
 * Each entity with BehaviorComponent is kept under control by this system. For each such entity a behavior tree
 * is loaded and an interpreter is started.
 * <p/>
 * Modifications made to a behavior tree will reflect to all entities using this tree.
 *
 * @author synopia
 */
@RegisterSystem
public class BehaviorSystem implements ComponentSystem, UpdateSubscriberSystem {
    public static final String BEHAVIORS = UriUtil.normalise("Behaviors");
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;

    private Map<EntityRef, Interpreter> entityInterpreters = Maps.newHashMap();
    private List<BehaviorTree> trees = Lists.newArrayList();

    public BehaviorSystem() {
        CoreRegistry.put(BehaviorSystem.class, this);
    }

    @Override
    public void initialise() {
        for (AssetUri uri : assetManager.listAssets(AssetType.BEHAVIOR)) {

            BehaviorTree asset = assetManager.loadAsset(uri, BehaviorTree.class);
            if (asset != null) {
                trees.add(asset);
            }
        }
    }

    @ReceiveEvent
    public void onBehaviorAdded(OnAddedComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
        addEntity(entityRef, behaviorComponent);
    }

    @ReceiveEvent
    public void onBehaviorActivated(OnActivatedComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
        addEntity(entityRef, behaviorComponent);
    }

    @ReceiveEvent
    public void onBehaviorRemoved(BeforeRemoveComponent event, EntityRef entityRef, BehaviorComponent behaviorComponent) {
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
        BehaviorTree behaviorTree = new BehaviorTree(new AssetUri(AssetType.BEHAVIOR, BEHAVIORS, name.replaceAll("\\W+", "")), data);
        trees.add(behaviorTree);
        save(behaviorTree);
        return behaviorTree;
    }

    public void save(BehaviorTree tree) {
        Path savePath;
        AssetUri uri = tree.getURI();
        if (BEHAVIORS.equals(uri.getModuleName())) {
            savePath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS).resolve("assets").resolve("behaviors");
        } else {
            Path overridesPath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS).resolve("overrides");
            savePath = overridesPath.resolve(uri.getModuleName()).resolve("behaviors");
        }
        BehaviorTreeLoader loader = new BehaviorTreeLoader();
        try {
            Files.createDirectories(savePath);
            Path file = savePath.resolve(uri.getAssetName() + ".behavior");
            loader.save(new FileOutputStream(file.toFile()), tree.getData());
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
        Collections.sort(interpreters, new Comparator<Interpreter>() {
            @Override
            public int compare(Interpreter o1, Interpreter o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return interpreters;
    }

    public void treeModified(BehaviorTree tree) {
        for (Interpreter interpreter : entityInterpreters.values()) {
            interpreter.reset();
        }
        save(tree);
    }

    @Override
    public void shutdown() {

    }

    private void addEntity(EntityRef entityRef, BehaviorComponent behaviorComponent) {
        Interpreter interpreter = entityInterpreters.get(entityRef);
        if (interpreter == null) {
            interpreter = new Interpreter(new Actor(entityRef));
            BehaviorTree tree = behaviorComponent.tree;
            entityInterpreters.put(entityRef, interpreter);
            behaviorComponent.tree = tree;
            entityRef.saveComponent(behaviorComponent);
            interpreter.start(tree.getRoot());
        }
    }
}
