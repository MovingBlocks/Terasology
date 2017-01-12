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
package org.terasology.config.flexible.setting.validators;

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
                assertTrue(String.format("%d returned invalid", i), validator.isValid(i));
            }

            assertFalse(validator.isValid(101));
            assertFalse(validator.isValid(-99));
        }


        @Test
        public void testExclusive() {
            validator.setInclusive(false);

            for (int i = validator.getLow() + 1; i < validator.getHigh(); i++) {
                assertTrue(String.format("%d returned invalid", i), validator.isValid(i));
            }

            assertFalse(validator.isValid(100));
            assertFalse(validator.isValid(0));
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
                assertTrue(String.format("%f returned invalid", i), validator.isValid(i));
            }

            assertFalse(validator.isValid(validator.getLow() - EPSILON));
            assertFalse(validator.isValid(validator.getHigh() + EPSILON));
        }


        @Test
        public void testExclusive() {
            validator.setInclusive(false);

            for (double i = validator.getLow() + EPSILON; i < validator.getHigh(); i++) {
                assertTrue(String.format("%f returned invalid", i), validator.isValid(i));
            }

            assertFalse(validator.isValid(100d));
            assertFalse(validator.isValid(0d));
        }
    }
}