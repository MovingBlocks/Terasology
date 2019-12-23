/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegerRangeTest {
    @Test
    public void testNoRange() {
        IntegerRange range = new IntegerRange();
        validateRange(range);
    }

    @Test
    public void testIncorrectRange() {
        IntegerRange range = new IntegerRange();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> range.addNumbers(3, 2));
    }

    @Test
    public void testSimpleRange() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        validateRange(range, 1, 2, 3);
    }

    @Test
    public void testOneNumberRange() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 1);
        validateRange(range, 1);
    }

    @Test
    public void testTwoRanges() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        range.addNumbers(5, 6);
        validateRange(range, 1, 2, 3, 5, 6);
    }

    @Test
    public void testTwoRangesAddedInReverseOrder() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(5, 6);
        range.addNumbers(1, 3);
        validateRange(range, 1, 2, 3, 5, 6);
    }

    @Test
    public void testTwoRangesOneWithinAnother() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 6);
        range.addNumbers(3, 5);
        validateRange(range, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testTwoRangesOverlapping() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 4);
        range.addNumbers(3, 6);
        validateRange(range, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testTwoRangesAtTheMaxInt() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(Integer.MAX_VALUE - 3, Integer.MAX_VALUE);
        range.addNumbers(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        validateRange(range, Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    }

    private void validateRange(IntegerRange range, Integer...numbers) {
        Iterator<Integer> iterator = range.iterator();
        for (Integer number : numbers) {
            assertTrue(iterator.hasNext());
            assertEquals(number, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }
}
