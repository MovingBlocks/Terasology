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

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class RangedNumberValidatorTest {
    public static class IntegerValidator {
        private RangedNumberValidator<Integer> validator;

        private void initValidator(Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
            validator = new RangedNumberValidator<>(min, max, minInclusive, maxInclusive);
        }

        @Test
        public void testAllInclusive() {
            initValidator(0, 100, true, true);

            assertTrue(String.format("%d returned invalid", 0), validator.fastValidate(0));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83));
            assertTrue(String.format("%d returned invalid", 100), validator.fastValidate(100));


            assertFalse(validator.fastValidate(0 - 1));
            assertFalse(validator.fastValidate(100 + 1));
        }

        @Test
        public void testMinExclusive() {
            initValidator(0, 100, false, true);

            assertFalse(String.format("%d returned invalid", 0), validator.fastValidate(0));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83));
            assertTrue(String.format("%d returned invalid", 100), validator.fastValidate(100));
        }

        @Test
        public void testMaxExclusive() {
            initValidator(0, 100, true, false);

            assertTrue(String.format("%d returned invalid", 0), validator.fastValidate(0));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83));
            assertFalse(String.format("%d returned invalid", 100), validator.fastValidate(100));
        }

        @Test
        public void testAllExclusive() {
            initValidator(0, 100, false, false);

            assertFalse(String.format("%d returned invalid", 0), validator.fastValidate(0));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83));
            assertFalse(String.format("%d returned invalid", 100), validator.fastValidate(100));
        }

        @Test
        public void testLowUnbounded() {
            initValidator(null, 100, false, false);

            assertTrue(validator.fastValidate(-1000));
            assertTrue(validator.fastValidate(-50000));

            assertTrue(validator.fastValidate(50));

            assertFalse(validator.fastValidate(100 + 1));
        }

        @Test
        public void testHighUnbounded() {
            initValidator(0, null, false, false);

            assertTrue(validator.fastValidate(1000));
            assertTrue(validator.fastValidate(50000));

            assertTrue(validator.fastValidate(50));

            assertFalse(validator.fastValidate(0 - 1));
        }

        @Test
        public void testAllUnbounded() {
            initValidator(null, null, false, false);

            assertTrue(validator.fastValidate(1000));
            assertTrue(validator.fastValidate(50000));
            assertTrue(validator.fastValidate(50));

            assertTrue(validator.fastValidate(-1000));
            assertTrue(validator.fastValidate(-50000));
        }
    }

    public static class DoubleValidator {
        private static final double MAX_ALLOWED_ERROR = 0.000001d;

        private RangedNumberValidator<Double> validator;

        private void initValidator(Double min, Double max, boolean minInclusive, boolean maxInclusive) {
            validator = new RangedNumberValidator<>(min, max, minInclusive, maxInclusive);
        }

        @Test
        public void testAllInclusive() {
            initValidator(0d, 100d, true, true);

            assertTrue(String.format("%d returned invalid", 0), validator.fastValidate(0d));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12d));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83d));
            assertTrue(String.format("%d returned invalid", 100), validator.fastValidate(100d));

            assertFalse(validator.fastValidate(0 - MAX_ALLOWED_ERROR));
            assertFalse(validator.fastValidate(100 + MAX_ALLOWED_ERROR));
        }

        @Test
        public void testMinExclusive() {
            initValidator(0d, 100d, false, true);

            assertFalse(String.format("%d returned invalid", 0), validator.fastValidate(0d));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12d));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83d));
            assertTrue(String.format("%d returned invalid", 100), validator.fastValidate(100d));
        }

        @Test
        public void testMaxExclusive() {
            initValidator(0d, 100d, true, false);

            assertTrue(String.format("%d returned invalid", 0), validator.fastValidate(0d));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12d));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83d));
            assertFalse(String.format("%d returned invalid", 100), validator.fastValidate(100d));
        }

        @Test
        public void testAllExclusive() {
            initValidator(0d, 100d, false, false);

            assertFalse(String.format("%d returned invalid", 0), validator.fastValidate(0d));
            assertTrue(String.format("%d returned invalid", 12), validator.fastValidate(12d));
            assertTrue(String.format("%d returned invalid", 83), validator.fastValidate(83d));
            assertFalse(String.format("%d returned invalid", 100), validator.fastValidate(100d));
        }

        @Test
        public void testLowUnbounded() {
            initValidator(null, 100d, false, false);

            assertTrue(validator.fastValidate(-1000d));
            assertTrue(validator.fastValidate(-50000d));
            assertTrue(validator.fastValidate(50d));

            assertFalse(validator.fastValidate(100 + MAX_ALLOWED_ERROR));
        }

        @Test
        public void testHighUnbounded() {
            initValidator(0d, null, false, false);

            assertTrue(validator.fastValidate(1000d));
            assertTrue(validator.fastValidate(50000d));
            assertTrue(validator.fastValidate(50d));

            assertFalse(validator.fastValidate(0 - MAX_ALLOWED_ERROR));
        }

        @Test
        public void testAllUnbounded() {
            initValidator(null, null, false, false);

            assertTrue(validator.fastValidate(1000d));
            assertTrue(validator.fastValidate(50000d));
            assertTrue(validator.fastValidate(50d));

            assertTrue(validator.fastValidate(-1000d));
            assertTrue(validator.fastValidate(-50000d));
        }
    }
}
