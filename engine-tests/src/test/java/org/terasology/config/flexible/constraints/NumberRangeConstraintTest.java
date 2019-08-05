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
package org.terasology.config.flexible.constraints;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class NumberRangeConstraintTest {
    public static class IntegerConstraint {
        private NumberRangeConstraint<Integer> constraint;

        private void initConstraint(Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
            constraint = new NumberRangeConstraint<>(min, max, minInclusive, maxInclusive);
        }

        @Test
        public void testAllInclusive() {
            initConstraint(0, 100, true, true);

            assertTrue(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83));
            assertTrue(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100));


            assertFalse(constraint.isSatisfiedBy(0 - 1));
            assertFalse(constraint.isSatisfiedBy(100 + 1));
        }

        @Test
        public void testMinExclusive() {
            initConstraint(0, 100, false, true);

            assertFalse(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83));
            assertTrue(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100));
        }

        @Test
        public void testMaxExclusive() {
            initConstraint(0, 100, true, false);

            assertTrue(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83));
            assertFalse(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100));
        }

        @Test
        public void testAllExclusive() {
            initConstraint(0, 100, false, false);

            assertFalse(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83));
            assertFalse(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100));
        }

        @Test
        public void testLowUnbounded() {
            initConstraint(null, 100, false, false);

            assertTrue(constraint.isSatisfiedBy(-1000));
            assertTrue(constraint.isSatisfiedBy(-50000));

            assertTrue(constraint.isSatisfiedBy(50));

            assertFalse(constraint.isSatisfiedBy(100 + 1));
        }

        @Test
        public void testHighUnbounded() {
            initConstraint(0, null, false, false);

            assertTrue(constraint.isSatisfiedBy(1000));
            assertTrue(constraint.isSatisfiedBy(50000));

            assertTrue(constraint.isSatisfiedBy(50));

            assertFalse(constraint.isSatisfiedBy(0 - 1));
        }

        @Test
        public void testAllUnbounded() {
            initConstraint(null, null, false, false);

            assertTrue(constraint.isSatisfiedBy(1000));
            assertTrue(constraint.isSatisfiedBy(50000));
            assertTrue(constraint.isSatisfiedBy(50));

            assertTrue(constraint.isSatisfiedBy(-1000));
            assertTrue(constraint.isSatisfiedBy(-50000));
        }
    }

    public static class DoubleConstraint {
        private static final double MAX_ALLOWED_ERROR = 0.000001d;

        private NumberRangeConstraint<Double> constraint;

        private void initConstraint(Double min, Double max, boolean minInclusive, boolean maxInclusive) {
            constraint = new NumberRangeConstraint<>(min, max, minInclusive, maxInclusive);
        }

        @Test
        public void testAllInclusive() {
            initConstraint(0d, 100d, true, true);

            assertTrue(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0d));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12d));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83d));
            assertTrue(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100d));

            assertFalse(constraint.isSatisfiedBy(0 - MAX_ALLOWED_ERROR));
            assertFalse(constraint.isSatisfiedBy(100 + MAX_ALLOWED_ERROR));
        }

        @Test
        public void testMinExclusive() {
            initConstraint(0d, 100d, false, true);

            assertFalse(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0d));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12d));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83d));
            assertTrue(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100d));
        }

        @Test
        public void testMaxExclusive() {
            initConstraint(0d, 100d, true, false);

            assertTrue(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0d));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12d));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83d));
            assertFalse(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100d));
        }

        @Test
        public void testAllExclusive() {
            initConstraint(0d, 100d, false, false);

            assertFalse(String.format("%d returned invalid", 0), constraint.isSatisfiedBy(0d));
            assertTrue(String.format("%d returned invalid", 12), constraint.isSatisfiedBy(12d));
            assertTrue(String.format("%d returned invalid", 83), constraint.isSatisfiedBy(83d));
            assertFalse(String.format("%d returned invalid", 100), constraint.isSatisfiedBy(100d));
        }

        @Test
        public void testLowUnbounded() {
            initConstraint(null, 100d, false, false);

            assertTrue(constraint.isSatisfiedBy(-1000d));
            assertTrue(constraint.isSatisfiedBy(-50000d));
            assertTrue(constraint.isSatisfiedBy(50d));

            assertFalse(constraint.isSatisfiedBy(100 + MAX_ALLOWED_ERROR));
        }

        @Test
        public void testHighUnbounded() {
            initConstraint(0d, null, false, false);

            assertTrue(constraint.isSatisfiedBy(1000d));
            assertTrue(constraint.isSatisfiedBy(50000d));
            assertTrue(constraint.isSatisfiedBy(50d));

            assertFalse(constraint.isSatisfiedBy(0 - MAX_ALLOWED_ERROR));
        }

        @Test
        public void testAllUnbounded() {
            initConstraint(null, null, false, false);

            assertTrue(constraint.isSatisfiedBy(1000d));
            assertTrue(constraint.isSatisfiedBy(50000d));
            assertTrue(constraint.isSatisfiedBy(50d));

            assertTrue(constraint.isSatisfiedBy(-1000d));
            assertTrue(constraint.isSatisfiedBy(-50000d));
        }
    }
}
