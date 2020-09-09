// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.entitySystem;

import com.google.common.collect.Lists;
import org.terasology.benchmark.AbstractBenchmark;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.BlockComponent;

import java.util.List;

/**
 *
 */
public class IterateSingleComponentBenchmark extends AbstractBenchmark {
    private List<List<Component>> rawEntityData;
    private PojoEntityManager entityManager;

    public IterateSingleComponentBenchmark() {
        super("Iterate Entities Single Component", 10000, new int[]{10000});
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
        for (EntityRef entity : entityManager.getEntitiesWith(LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            loc.getLocalPosition();
        }
    }

    @Override
    public void finish(boolean aborted) {
        rawEntityData = null;
        entityManager = null;
    }
}
