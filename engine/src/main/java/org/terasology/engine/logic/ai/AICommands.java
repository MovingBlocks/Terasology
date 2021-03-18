// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.ai;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.registry.In;

import com.google.common.collect.Iterables;

/**
 * Artificial Intelligence commands for counting and destroying AIs used by entities with AI related components attached
 */
@RegisterSystem
public class AICommands extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    /**
     * Counts all AIs in the world
     * @return String string containing number of simple AIs and hierarchical AIs
     */
    @Command(runOnServer = true, shortDescription = "Count all AIs in the world")
    public String countAI() {
        int simpleAIs = Iterables.size(entityManager.getEntitiesWith(SimpleAIComponent.class));
        int hierarchical = Iterables.size(entityManager.getEntitiesWith(HierarchicalAIComponent.class));
        return "Simple AIs: " + simpleAIs + ", Hierarchical AIs: " + hierarchical;
    }

    /**
     * Destroys all entities with attached SimpleAIComponent or HierarchicalAIComponent in the world
     * @return String string containing number of simple AIs and hierarchical AIs destroyed
     */
    @Command(runOnServer = true, shortDescription = "Destroys all AIs in the world")
    public String destroyAI() {
        int simpleAI = 0;
        for (EntityRef ref : entityManager.getEntitiesWith(SimpleAIComponent.class)) {
            ref.destroy();
            simpleAI++;
        }

        int hierarchicalAI = 0;
        for (EntityRef ref : entityManager.getEntitiesWith(HierarchicalAIComponent.class)) {
            ref.destroy();
            hierarchicalAI++;
        }
        return "Simple AIs (" + simpleAI + ") Destroyed, Hierarchical AIs (" + hierarchicalAI + ") Destroyed ";
    }
}
