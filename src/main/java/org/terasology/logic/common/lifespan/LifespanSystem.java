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

package org.terasology.logic.common.lifespan;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

/**
 * @author Immortius
 */
@RegisterSystem
public class LifespanSystem implements UpdateSubscriberSystem {

    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(LifespanComponent.class)) {
            LifespanComponent lifespan = entity.getComponent(LifespanComponent.class);
            lifespan.lifespan -= delta;
            if (lifespan.lifespan < 0) {
                entity.destroy();
            } else {
                entity.saveComponent(lifespan);
            }
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }
}
