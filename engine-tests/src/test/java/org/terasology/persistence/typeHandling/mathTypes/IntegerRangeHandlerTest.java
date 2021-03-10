/*
# * Copyright 2015 MovingBlocks
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
package org.terasology.engine.persistence.typeHandling.mathTypes;

import org.junit.jupiter.api.Test;
import org.terasology.engine.math.IntegerRange;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegerRangeHandlerTest {
    private IntegerRangeHandler handler = new IntegerRangeHandler();

    @Test
    public void testEmptyRange() {
        IntegerRange range = new IntegerRange();
        String rangeStr = handler.getAsString(range);
        assertEquals("", rangeStr);
        validateRange(handler.getFromString(rangeStr));
    }

    @Test
    public void testSimpleRange() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        String rangeStr = handler.getAsString(range);
        assertEquals("1..3", rangeStr);
        validateRange(handler.getFromString(rangeStr), 1, 2, 3);
    }

    @Test
    public void testSingleRange() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 1);
        String rangeStr = handler.getAsString(range);
        assertEquals("1", rangeStr);
        validateRange(handler.getFromString(rangeStr), 1);
    }

    @Test
    public void testTwoRanges() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        range.addNumbers(5, 6);
        String rangeStr = handler.getAsString(range);
        assertEquals("1..3,5..6", rangeStr);
        validateRange(handler.getFromString(rangeStr), 1, 2, 3, 5, 6);
    }

    @Test
    public void testTwoRangesMerged() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 5);
        range.addNumbers(3, 6);
        String rangeStr = handler.getAsString(range);
        assertEquals("1..6", rangeStr);
        validateRange(handler.getFromString(rangeStr), 1, 2, 3, 4, 5, 6);
    }

    private void validateRange(IntegerRange range, Integer... numbers) {
        Iterator<Integer> iterator = range.iterator();
        for (Integer number : numbers) {
            assertTrue(iterator.hasNext());
            assertEquals(number, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }
}
