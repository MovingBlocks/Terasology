// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.entitySystem;

import com.google.common.collect.Lists;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.BlockComponent;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class IterateComponentsBenchmark {

    @State(Scope.Benchmark)
    public static class StateObject {
        private final PojoEntityManager entityManager = new PojoEntityManager();

        public void setup() {
            FastRandom rand = new FastRandom(0L);
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
                entityManager.create(entityData);
            }
        }
    }
    
    @Benchmark
    public void iterateMultipleComponent(StateObject state) {
        for (EntityRef entity : state.entityManager.getEntitiesWith(MeshComponent.class, LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            loc.getLocalPosition();
        }
    }

    @Benchmark
    public void iterateSingleComponent(StateObject state) {
        for (EntityRef entity : state.entityManager.getEntitiesWith(LocationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            loc.getLocalPosition();
        }
    }

}
