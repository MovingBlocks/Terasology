/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.core.logic.tree;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.core.logic.tree.lsystem.AdvanceAxionElementGeneration;
import org.terasology.core.logic.tree.lsystem.AdvancedLSystemTreeDefinition;
import org.terasology.core.logic.tree.lsystem.AxionElementGeneration;
import org.terasology.core.logic.tree.lsystem.AxionElementReplacement;
import org.terasology.core.logic.tree.lsystem.DefaultAxionElementGeneration;
import org.terasology.core.logic.tree.lsystem.SimpleAxionElementReplacement;
import org.terasology.core.logic.tree.lsystem.SurroundAxionElementGeneration;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TreeGrowingSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(TreeGrowingSystem.class);
    private static final int CHECK_INTERVAL = 1000;
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private BlockManager blockManager;
    @In
    private Time time;

    private long lastCheckTime;

    private Map<String, TreeDefinition> treeDefinitions = new HashMap<>();

    @Override
    public void initialise() {
        addTreeType("oak", constructOakDefinition());
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        if (lastCheckTime + CHECK_INTERVAL < gameTimeInMs) {
            Iterable<EntityRef> treeRefs = entityManager.getEntitiesWith(LivingTreeComponent.class, BlockComponent.class);
            for (EntityRef treeRef : treeRefs) {
                LivingTreeComponent tree = treeRef.getComponent(LivingTreeComponent.class);
                if (tree != null) {
                    TreeDefinition treeDefinition = treeDefinitions.get(tree.type);
                    treeDefinition.updateTree(worldProvider, blockEntityRegistry, treeRef);
                } else {
                    logger.error("Got an entity without a component (LivingTreeComponent) even though a list of entities that do have it was requested");
                }
            }

            lastCheckTime = gameTimeInMs;
        }
    }

    public void addTreeType(String type, TreeDefinition treeDefinition) {
        treeDefinitions.put(type, treeDefinition);
    }

    private TreeDefinition constructOakDefinition() {
        Map<Character, AxionElementReplacement> replacementMap = Maps.newHashMap();

        SimpleAxionElementReplacement sapling = new SimpleAxionElementReplacement("s");
        sapling.addReplacement(1f, "Tt");

        final FastRandom rnd = new FastRandom();

        SimpleAxionElementReplacement trunkTop = new SimpleAxionElementReplacement("t");
        trunkTop.addReplacement(0.6f,
                new SimpleAxionElementReplacement.ReplacementGenerator() {
                    @Override
                    public String generateReplacement(String currentAxion) {
                        // 137.5 degrees is a golden ratio
                        int deg = rnd.nextInt(130, 147);
                        return "+(" + deg + ")[&Mb]Wt";
                    }
                });
        trunkTop.addReplacement(0.4f,
                new SimpleAxionElementReplacement.ReplacementGenerator() {
                    @Override
                    public String generateReplacement(String currentAxion) {
                        // Always generate at least 2 branches
                        if (currentAxion.split("b").length < 2) {
                            // 137.5 degrees is a golden ratio
                            int deg = rnd.nextInt(130, 147);
                            return "+(" + deg + ")[&Mb]Wt";
                        }
                        return "Wt";
                    }
                });

        SimpleAxionElementReplacement smallBranch = new SimpleAxionElementReplacement("b");
        smallBranch.addReplacement(0.8f, "Bb");

        SimpleAxionElementReplacement trunk = new SimpleAxionElementReplacement("T");
        trunk.addReplacement(0.7f, "TN");

        replacementMap.put('s', sapling);
        replacementMap.put('g', sapling);
        replacementMap.put('t', trunkTop);
        replacementMap.put('T', trunk);
        replacementMap.put('b', smallBranch);


        Block oakSapling = blockManager.getBlock("core:OakSapling");
        Block oakSaplingGenerated = blockManager.getBlock("core:OakSaplingGenerated");
        Block greenLeaf = blockManager.getBlock("core:GreenLeaf");
        Block oakTrunk = blockManager.getBlock("core:OakTrunk");
        Block oakBranch = blockManager.getBlock(new BlockUri("core", "OakBranch", "0"));

        float trunkAdvance = 0.3f;
        float branchAdvance = 0.2f;

        Map<Character, AxionElementGeneration> blockMap = Maps.newHashMap();
        blockMap.put('s', new DefaultAxionElementGeneration(oakSapling, trunkAdvance));
        blockMap.put('g', new DefaultAxionElementGeneration(oakSaplingGenerated, trunkAdvance));

        // Trunk building blocks
        blockMap.put('t', new SurroundAxionElementGeneration(greenLeaf, greenLeaf, trunkAdvance, 2f));
        blockMap.put('T', new DefaultAxionElementGeneration(oakTrunk, trunkAdvance));
        blockMap.put('N', new DefaultAxionElementGeneration(oakTrunk, trunkAdvance));
        blockMap.put('W', new SurroundAxionElementGeneration(oakBranch, greenLeaf, trunkAdvance, 2f));

        // Branch building blocks
        SurroundAxionElementGeneration smallBranchGeneration = new SurroundAxionElementGeneration(greenLeaf, greenLeaf, branchAdvance, 2.6f);
        smallBranchGeneration.setMaxZ(0);
        SurroundAxionElementGeneration largeBranchGeneration = new SurroundAxionElementGeneration(oakBranch, greenLeaf, branchAdvance, 1.1f, 3.5f);
        largeBranchGeneration.setMaxZ(0);
        blockMap.put('b', smallBranchGeneration);
        blockMap.put('B', largeBranchGeneration);
        blockMap.put('M', new AdvanceAxionElementGeneration(branchAdvance));

        return new AdvancedLSystemTreeDefinition(replacementMap, blockMap, Arrays.asList(oakTrunk, oakBranch, greenLeaf), 1.5f);
    }
}
