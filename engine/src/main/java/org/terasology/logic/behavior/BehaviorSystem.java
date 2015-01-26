/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
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
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    @In
    private BehaviorTreeBuilder treeBuilder;

    private List<BehaviorTree> trees = Lists.newArrayList();

    private EntityRef dummy;


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

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            behaviorComponent.interpreter.tick(delta);
        }
    }

    public BehaviorTree createTree(String name, BehaviorNode root) {
        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(root.deepCopy());
        BehaviorTree behaviorTree = new BehaviorTree(new AssetUri(AssetType.BEHAVIOR, BEHAVIORS, name.replaceAll("\\W+", "")), data);
        trees.add(behaviorTree);
        save(behaviorTree);
        return behaviorTree;
    }

    public void save(BehaviorTree tree) {
        Path savePath;
        AssetUri uri = tree.getURI();
        /*if (BEHAVIORS.equals(uri.getModuleName())) {
            savePath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve("assets").resolve("behaviors");
        } else */
        {
            Path overridesPath = PathManager.getInstance().getHomeModPath().resolve(BEHAVIORS.toString()).resolve("overrides");
            savePath = overridesPath.resolve(uri.getModuleName().toString()).resolve("behaviors");
        }
        BehaviorTreeLoader loader = new BehaviorTreeLoader();
        try {
            Files.createDirectories(savePath);
            Path file = savePath.resolve(uri.getAssetName() + ".behavior");
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
        List<Interpreter> runners = Lists.newArrayList();
        for (EntityRef entity : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            if (behaviorComponent.interpreter != null) {
                runners.add(behaviorComponent.interpreter);
            }
        }
        if (runners.size() == 0) {
            BehaviorComponent behaviorComponent = new BehaviorComponent();
            behaviorComponent.tree = assetManager.resolveAndLoad(AssetType.BEHAVIOR, "engine:default", BehaviorTree.class);
            dummy = entityManager.create(behaviorComponent);
            DisplayNameComponent nameComponent = new DisplayNameComponent();
            nameComponent.name = "Main";
            dummy.addComponent(nameComponent);
            dummy.addComponent(behaviorComponent);
        }
        Collections.sort(runners, new Comparator<Interpreter>() {
            @Override
            public int compare(Interpreter o1, Interpreter o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
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
