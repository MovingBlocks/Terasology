/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.ai;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.registry.In;

/**
 * @author Immortius
 */
@RegisterSystem
public class AICommands extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @Command(shortDescription = "Destroys all AIs in the world", runOnServer = true)
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

    @Command(shortDescription = "Count all AIs in the world", runOnServer = true)
    public String countAI() {
        int simpleAIs = 0;
        for (EntityRef ref : entityManager.getEntitiesWith(SimpleAIComponent.class)) {
            simpleAIs++;
        }
        int hierarchical = 0;
        for (EntityRef ref : entityManager.getEntitiesWith(HierarchicalAIComponent.class)) {
            hierarchical++;
        }
        return "Simple AIs: " + simpleAIs + ", Hierarchical AIs: " + hierarchical;
    }
}
