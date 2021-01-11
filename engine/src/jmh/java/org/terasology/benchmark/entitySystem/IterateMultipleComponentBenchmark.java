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

package org.terasology.benchmark.entitySystem;

import com.google.common.collect.Lists;
import org.terasology.benchmark.AbstractBenchmark;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.BlockComponent;

import java.util.List;

/**
 *
 */
public class IterateMultipleComponentBenchmark extends AbstractBenchmark {

    private List<List<Component>> rawEntityData;
    private PojoEntityManager entityManager;

    public IterateMultipleComponentBenchmark() {
        super("Iterate entities by multiple components", 10000, new int[]{10000});
    }

    @Override
    public void setup() {
        FastRandom rand = new FastRandom(0L);
        rawEntityData = Lists.newArrayList();
        for (int i = 0; i < 1000; ++i) {
            List<Component> entityData = Lists.newArrayList();
            if (rand.nextFloat() < 0.75f) {
                entityData.add(new LocationComponent());
            }
            if (rand.nextFloat() < 0.5f) {
                entityData.add(new MeshComponent());
            }
            if (rand.nextFloat() < 0.25f) {
                entityData.add(new BlockComponent());
            }
            rawEntityData.add(entityData);
        }

        entityManager = new PojoEntityManager();
        for (List<Component> rawEntity : rawEntityData) {
            entityManager.create(rawEntity);
        }
    }

    @Override
    public void run() {
        for (EntityRef entity : entityManager.getEntitiesWith(MeshComponent.class, LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            loc.getLocalPosition();
        }
    }
}
