// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.entitySystem;

import com.google.common.collect.Lists;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
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
public class EntityCreateBenchmark {

    @Benchmark
    public EntityRef createEntity(StateObject state) {
        return state.entityManager.create(state.entityData);
    }

    @State(Scope.Thread)
    public static class StateObject {

        private List<Component> entityData;
        private PojoEntityManager entityManager;

        @Setup(Level.Invocation)
        public void setup() {
            entityData = Lists.newArrayList();
            entityManager = new PojoEntityManager();
            FastRandom rand = new FastRandom(0L);
            if (rand.nextFloat() < 0.75f) {
                entityData.add(new LocationComponent());
            }
            if (rand.nextFloat() < 0.5f) {
                entityData.add(new MeshComponent());
            }
            if (rand.nextFloat() < 0.25f) {
                entityData.add(new BlockComponent());
            }
        }
    }
}
