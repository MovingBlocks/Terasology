package org.terasology.entitySystem.internal;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.block.entity.BlockComponent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class EntitySystemPerformanceTest {


    @Test
    public void runBenchmarkSuite() {
        test(new CreateTest());
        test(new IterateSingleTest());
        test(new IterateMultipleTest());
    }

    public void test(BenchmarkTest test) {
        for (int i = 0; i < 10000; ++i) {
            test.run();
        }

        long start = System.nanoTime();
        for (int i = 0; i < 10000; ++i) {
            test.run();
        }

        long durationNS = System.nanoTime() - start;
        long duration = TimeUnit.MILLISECONDS.convert(durationNS, TimeUnit.NANOSECONDS);
        System.out.println(test.getTitle() + ": " + ((float) duration / 10000) + "ms");
    }

    interface BenchmarkTest {
        public String getTitle();

        public void run();
    }

    private static class CreateTest implements BenchmarkTest {

        private List<List<Component>> rawEntityData;

        public CreateTest() {
            FastRandom rand = new FastRandom(0L);
            rawEntityData = Lists.newArrayList();
            for (int i = 0; i < 1000; ++i) {
                List<Component> entityData = Lists.newArrayList();
                if (rand.randomFloat() < 0.75f) {
                    entityData.add(new LocationComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new MeshComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new InventoryComponent());
                }
                if (rand.randomFloat() < 0.25f) {
                    entityData.add(new BlockComponent());
                }
                rawEntityData.add(entityData);
            }
        }

        @Override
        public String getTitle() {
            return "Create Entities";
        }

        @Override
        public void run() {
            PojoEntityManager entityManager = new PojoEntityManager();
            for (List<Component> rawEntity : rawEntityData) {
                entityManager.create(rawEntity);
            }
        }
    }

    private static class IterateSingleTest implements BenchmarkTest {

        private List<List<Component>> rawEntityData;
        private PojoEntityManager entityManager;

        public IterateSingleTest() {
            FastRandom rand = new FastRandom(0L);
            rawEntityData = Lists.newArrayList();
            for (int i = 0; i < 1000; ++i) {
                List<Component> entityData = Lists.newArrayList();
                if (rand.randomFloat() < 0.75f) {
                    entityData.add(new LocationComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new MeshComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new InventoryComponent());
                }
                if (rand.randomFloat() < 0.25f) {
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
        public String getTitle() {
            return "Iterate Entities w/ Location";
        }

        @Override
        public void run() {
            for (EntityRef entity : entityManager.listEntitiesWith(LocationComponent.class)) {
                //LocationComponent loc = entity.getComponent(LocationComponent.class);
                //loc.getLocalPosition();
            }
        }
    }

    private static class IterateMultipleTest implements BenchmarkTest {

        private List<List<Component>> rawEntityData;
        private PojoEntityManager entityManager;

        public IterateMultipleTest() {
            FastRandom rand = new FastRandom(0L);
            rawEntityData = Lists.newArrayList();
            for (int i = 0; i < 1000; ++i) {
                List<Component> entityData = Lists.newArrayList();
                if (rand.randomFloat() < 0.75f) {
                    entityData.add(new LocationComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new MeshComponent());
                }
                if (rand.randomFloat() < 0.5f) {
                    entityData.add(new InventoryComponent());
                }
                if (rand.randomFloat() < 0.25f) {
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
        public String getTitle() {
            return "Iterate Entities w/ Mesh and Location";
        }

        @Override
        public void run() {
            for (EntityRef entity : entityManager.listEntitiesWith(MeshComponent.class, LocationComponent.class)) {
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                loc.getLocalPosition();
            }
        }
    }

}
