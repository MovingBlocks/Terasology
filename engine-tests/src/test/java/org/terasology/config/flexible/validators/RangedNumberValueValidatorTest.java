/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.config.flexible.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class RangedNumberValueValidatorTest {
    public static class IntegerValidator {
        private RangedNumberValueValidator<Integer> validator;

        @Before
        public void setUp() {
            validator = new RangedNumberValueValidator<>(0, 100, true);
        }

        @Test
        public void testInclusive() {
            for (int i = validator.getLow(); i <= validator.getHigh(); i++) {
                assertTrue(String.format("%d returned invalid", i), validator.validate(i));
            }

            assertFalse(validator.validate(validator.getHigh() + 1));
            assertFalse(validator.validate(validator.getLow() - 1));
        }


        @Test
        public void testExclusive() {
            validator.setInclusive(false);

            for (int i = validator.getLow() + 1; i < validator.getHigh(); i++) {
                assertTrue(String.format("%d returned invalid", i), validator.validate(i));
            }

            assertFalse(validator.validate(validator.getHigh()));
            assertFalse(validator.validate(validator.getLow()));
        }

        @Test
        public void testLowUnbounded() {
            validator.removeLowBound();

            assertTrue(validator.validate(-1000));
            assertTrue(validator.validate(-50000));
            assertTrue(validator.validate(50));

            assertFalse(validator.validate(validator.getHigh() + 1));
        }

        @Test
        public void testHighUnbounded() {
            validator.removeHighBound();

            assertTrue(validator.validate(1000));
            assertTrue(validator.validate(50000));
            assertTrue(validator.validate(50));

            assertFalse(validator.validate(validator.getLow() - 1));
        }

        @Test
        public void testAllUnbounded() {
            validator.removeAllBounds();

            assertTrue(validator.validate(1000));
            assertTrue(validator.validate(50000));
            assertTrue(validator.validate(50));

            assertTrue(validator.validate(-1000));
            assertTrue(validator.validate(-50000));
        }
    }

    public static class DoubleValidator {
        private static final double EPSILON = 0.000001d;

        private RangedNumberValueValidator<Double> validator;

        @Before
        public void setUp() {
            validator = new RangedNumberValueValidator<>(0d, 100d, true);
        }

        @Test
        public void testInclusive() {
            for (double i = validator.getLow(); i <= validator.getHigh(); i++) {
                assertTrue(String.format("%f returned invalid", i), validator.validate(i));
            }

            assertFalse(validator.validate(validator.getLow() - EPSILON));
            assertFalse(validator.validate(validator.getHigh() + EPSILON));
        }


        @Test
        public void testExclusive() {
            validator.setInclusive(false);

            for (double i = validator.getLow() + EPSILON; i < validator.getHigh(); i++) {
                assertTrue(String.format("%f returned invalid", i), validator.validate(i));
            }

            assertFalse(validator.validate(validator.getLow()));
            assertFalse(validator.validate(validator.getHigh()));
        }

        @Test
        public void testLowUnbounded() {
            validator.removeLowBound();

            assertTrue(validator.validate(-1000d));
            assertTrue(validator.validate(-50000d));
            assertTrue(validator.validate(50d));

            assertFalse(validator.validate(validator.getHigh() + EPSILON));
        }

        @Test
        public void testHighUnbounded() {
            validator.removeHighBound();

            assertTrue(validator.validate(1000d));
            assertTrue(validator.validate(50000d));
            assertTrue(validator.validate(50d));

            assertFalse(validator.validate(validator.getLow() - EPSILON));
        }

        @Test
        public void testAllUnbounded() {
            validator.removeAllBounds();

            assertTrue(validator.validate(1000d));
            assertTrue(validator.validate(50000d));
            assertTrue(validator.validate(50d));

            assertTrue(validator.validate(-1000d));
            assertTrue(validator.validate(-50000d));
        }
    }
}