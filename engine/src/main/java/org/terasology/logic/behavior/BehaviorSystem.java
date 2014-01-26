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
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

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
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;

    private Map<BehaviorTree, List<Interpreter>> interpreters = Maps.newHashMap();
    private Map<EntityRef, Interpreter> entityInterpreters = Maps.newHashMap();
    private List<BehaviorTree> trees = Lists.newArrayList();

    public BehaviorSystem() {
        CoreRegistry.put(BehaviorSystem.class, this);
    }

    @Override
    public void initialise() {
        for (AssetUri uri : assetManager.listAssets(AssetType.BEHAVIOR)) {

            BehaviorTree asset = assetManager.loadAsset(uri, BehaviorTree.class);
            trees.add(asset);
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
            BehaviorTree tree = behaviorComponent.tree;
            Interpreter interpreter = entityInterpreters.remove(entityRef);
            interpreters.get(tree).remove(interpreter);
        }
    }

    @Override
    public void update(float delta) {
        for (Map.Entry<BehaviorTree, List<Interpreter>> entry : interpreters.entrySet()) {
            for (Interpreter interpreter : entry.getValue()) {
                interpreter.tick(delta);
            }
        }
    }

    public List<BehaviorTree> getTrees() {
        return trees;
    }

    public List<Interpreter> getInterpreter(BehaviorTree tree) {
        List<Interpreter> result = interpreters.get(tree);
        if (result == null) {
            result = Lists.newArrayList();
        }
        return result;
    }

    public void treeModified(BehaviorTree tree) {
        List<Interpreter> list = interpreters.get(tree);
        if (list == null || list.size() == 0) {
            return;
        }
        for (Interpreter interpreter : list) {
            interpreter.reset();
        }
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
            List<Interpreter> list = interpreters.get(tree);
            if (list == null) {
                list = Lists.newArrayList();
                interpreters.put(tree, list);
            }
            list.add(interpreter);
        }
    }
}
