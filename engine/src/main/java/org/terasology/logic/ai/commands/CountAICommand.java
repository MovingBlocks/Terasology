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
package org.terasology.logic.ai.commands;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.ai.HierarchicalAIComponent;
import org.terasology.logic.ai.SimpleAIComponent;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.registry.In;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class CountAICommand extends Command {
    @In
    private EntityManager entityManager;

    public CountAICommand() {
        super("countAI", true, "Count all AIs in the world", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
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
