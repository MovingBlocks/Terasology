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
package org.terasology.logic.console.internal.commands;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.registry.In;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class DestroyEntitiesUsingPrefabCommand extends Command {
    @In
    EntityManager entityManager;

    public DestroyEntitiesUsingPrefabCommand() {
        super("destroyEntitiesUsingPrefab", true, "Removes all entities of the given prefab", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
            CommandParameter.single("prefabName", String.class, true)
        };
    }

    public String execute(EntityRef sender, String prefabName)
    {
        Prefab prefab = entityManager.getPrefabManager().getPrefab(prefabName);
        int destroyed = 0;

        if (prefab != null) {
            for (EntityRef entity : entityManager.getAllEntities()) {
                if (prefab.getURI().equals(entity.getPrefabURI())) {
                    entity.destroy();
                    destroyed++;
                }
            }
        }

        return "Destroyed " + destroyed + " entities.";
    }

    //TODO Add the suggestion method
}
