// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.math.TeraMath;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link ParticlePool}.
 */
public class ParticlePoolTest {
    private static Random random = new FastRandom(9083);

    private static void randomizeParticle(final ParticlePool pool, final int index) {
        final int index3 = index * 3;
        final int index4 = index * 4;

        pool.energy[index] = random.nextFloat();

        for (int i = 0; i < 3; i++) {
            pool.position[index3 + i] = random.nextFloat();
            pool.previousPosition[index3 + i] = random.nextFloat();
            pool.velocity[index3 + i] = random.nextFloat();
            pool.scale[index3 + i] = random.nextFloat();
        }

        for (int i = 0; i < 4; i++) {
            pool.color[index4 + i] = random.nextFloat();
        }
    }

    private static void copyParticle(final ParticlePool src, final ParticlePool dest, final int index) {
        final int index3 = index * 3;
        final int index4 = index * 4;

        dest.energy[index] = src.energy[index];

        System.arraycopy(src.position, index3, dest.position, index3, 3);
        System.arraycopy(src.previousPosition, index3, dest.previousPosition, index3, 3);
        System.arraycopy(src.velocity, index3, dest.velocity, index3, 3);
        System.arraycopy(src.scale, index3, dest.scale, index3, 3);

        System.arraycopy(src.color, index4, dest.color, index4, 4);
    }

    private static ParticlePool createCopy(final ParticlePool src) {
        ParticlePool copy = new ParticlePool(src.size());

        for (int i = 0; i < src.livingParticles(); i++) {
            copy.reviveParticle();
            copyParticle(src, copy, i);
        }

        return copy;
    }

    private static void assertEqualParticles(
            final ParticlePool expected, final int expIndex,
            final ParticlePool actual, final int actIndex,
            final float epsilon
    ) {
        final int expIndex3 = expIndex * 3;
        final int expIndex4 = expIndex * 4;

        final int actIndex3 = actIndex * 3;
        final int actIndex4 = actIndex * 4;

        assertTrue(TeraMath.fastAbs(expected.energy[expIndex] - actual.energy[actIndex]) < epsilon);

        for (int i = 0; i < 3; i++) {
            assertTrue(TeraMath.fastAbs(expected.position[expIndex3 + i] - actual.position[actIndex3 + i]) < epsilon);
            assertTrue(TeraMath.fastAbs(expected.previousPosition[expIndex3 + i] - actual.previousPosition[actIndex3 + i]) < epsilon);
            assertTrue(TeraMath.fastAbs(expected.velocity[expIndex3 + i] - actual.velocity[actIndex3 + i]) < epsilon);
            assertTrue(TeraMath.fastAbs(expected.scale[expIndex3 + i] - actual.scale[actIndex3 + i]) < epsilon);
        }

        for (int i = 0; i < 4; i++) {
            assertTrue(TeraMath.fastAbs(expected.color[expIndex4 + i] - actual.color[actIndex4 + i]) < epsilon);
        }
    }

    private static void fillWithRandom(ParticlePool pool, int nr) {
        for (int i = 0; i < nr; i++) {
            pool.reviveParticle();
            randomizeParticle(pool, i);
        }
    }

    @Test
    public void constructorTest() {
        final int[] poolSizes = {1, 27, 133};

        for (int size : poolSizes) {
            ParticlePool pool = new ParticlePool(size);
            assertEquals(size, pool.size());
            assertEquals(0, pool.livingParticles());
            assertEquals(pool.size(), pool.deadParticles());

            assertEquals(size, pool.energy.length);

            assertEquals(size * 3, pool.position.length);
            assertEquals(size * 3, pool.previousPosition.length);
            assertEquals(size * 3, pool.velocity.length);
            assertEquals(size * 3, pool.scale.length);

            assertEquals(size * 4, pool.color.length);
        }
        // Should throw exception after creating the pool
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new ParticlePool(0));
        
    }

    @Test
    public void reviveParticleTest() {
        final int poolSize = 8;

        ParticlePool pool = new ParticlePool(poolSize);
        for (int i = 1; i <= poolSize; i++) {
            pool.reviveParticle();
            assertEquals(i, pool.livingParticles());
            assertEquals(poolSize - i, pool.deadParticles());
            assertEquals(poolSize, pool.size());
        }
    }

    @Test
    public void moveDeceasedTest() {
        // initialize
        int poolSize = 14;
        int livingParticles = 7;
        int deadParticles = poolSize - livingParticles;

        ParticlePool testPool = new ParticlePool(poolSize);
        fillWithRandom(testPool, livingParticles);

        ParticlePool comparisonPool = createCopy(testPool);


        // kill particle 3
        testPool.moveDeceasedParticle(3);
        livingParticles--;
        deadParticles++;

        assertEquals(poolSize, testPool.size());
        assertEquals(livingParticles, testPool.livingParticles());
        assertEquals(deadParticles, testPool.deadParticles());
        assertEqualParticles(comparisonPool, 6, testPool, 3, 1.0e-6f);


        // kill particle 0
        testPool.moveDeceasedParticle(0);
        livingParticles--;
        deadParticles++;

        assertEquals(poolSize, testPool.size());
        assertEquals(livingParticles, testPool.livingParticles());
        assertEquals(deadParticles, testPool.deadParticles());
        assertEqualParticles(comparisonPool, 5, testPool, 0, 1.0e-6f);


        // test it with a pool of length one (degenerate case)
        poolSize = 1;
        testPool = new ParticlePool(poolSize);
        fillWithRandom(testPool, 1);
        livingParticles = 1;
        deadParticles = 0;

        testPool.moveDeceasedParticle(0);
        livingParticles--;
        deadParticles++;

        assertEquals(poolSize, testPool.size());
        assertEquals(livingParticles, testPool.livingParticles());
        assertEquals(deadParticles, testPool.deadParticles());
        // there are no living particles to compare here
    }
}
