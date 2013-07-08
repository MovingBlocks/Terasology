package org.terasology.logic.tree;

import com.google.common.collect.Maps;
import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.tree.lsystem.*;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TreeGrowingSystem implements UpdateSubscriberSystem {
    private final static int CHECK_INTERVAL = 10000;
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
        if (lastCheckTime+CHECK_INTERVAL<gameTimeInMs) {
            Iterable<EntityRef> treeRefs = entityManager.getEntitiesWith(LivingTreeComponent.class, BlockComponent.class);
            for (EntityRef treeRef : treeRefs) {
                LivingTreeComponent tree = treeRef.getComponent(LivingTreeComponent.class);
                TreeDefinition treeDefinition = treeDefinitions.get(tree.type);
                treeDefinition.updateTree(worldProvider, treeRef);
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
        trunkTop.addReplacement(0.5f, "Wt");
        trunkTop.addReplacement(0.5f,
                new SimpleAxionElementReplacement.ReplacementGenerator() {
                    @Override
                    public String generateReplacement() {
                        int angleDeg = rnd.randomInt(180);
                        return "W[+("+angleDeg+")&Mb][+("+angleDeg+")^Mb]t";
                    }
                });

        SimpleAxionElementReplacement smallBranch = new SimpleAxionElementReplacement("b");
        smallBranch.addReplacement(0.8f, "Bb");

        SimpleAxionElementReplacement trunk = new SimpleAxionElementReplacement("T");
        trunk.addReplacement(0.7f, "TN");

        replacementMap.put('s', sapling);
        replacementMap.put('t', trunkTop);
        replacementMap.put('T', trunk);
        replacementMap.put('b', smallBranch);


        Block oakSapling = blockManager.getBlock("engine:OakSapling");
        Block greenLeaf = blockManager.getBlock("engine:GreenLeaf");
        Block oakTrunk = blockManager.getBlock("engine:OakTrunk");

        float trunkAdvance = 0.5f;
        float branchAdvance = 0.6f;

        Map<Character, AxionElementGeneration> blockMap = Maps.newHashMap();
        blockMap.put('s', new DefaultAxionElementGeneration(oakSapling, trunkAdvance));

        // Trunk building blocks
        blockMap.put('t', new DefaultAxionElementGeneration(greenLeaf, trunkAdvance));
        blockMap.put('T', new DefaultAxionElementGeneration(oakTrunk, trunkAdvance));
        blockMap.put('N', new DefaultAxionElementGeneration(oakTrunk, trunkAdvance));
        blockMap.put('W', new SurroundAxionElementGeneration(oakTrunk, greenLeaf, trunkAdvance, 1.7f));

        // Branch building blocks
        blockMap.put('b', new SurroundAxionElementGeneration(greenLeaf, greenLeaf, branchAdvance, 1.7f));
        blockMap.put('B', new SurroundAxionElementGeneration(oakTrunk, greenLeaf, branchAdvance, 2.8f));
        blockMap.put('M', new AdvanceAxionElementGeneration(branchAdvance));

        return new AdvancedLSystemTreeDefinition(replacementMap, blockMap, Arrays.asList(oakTrunk, greenLeaf), (float) Math.PI/3);
    }
}
