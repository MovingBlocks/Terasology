// Copyright The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.junit.jupiter.api.Test;
import org.terasology.engine.math.IntegerRange;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class YuTsuIntegerRangeTest {
    private static List<Integer> toList(IntegerRange range) {
        List<Integer> result = new ArrayList<>();
        for (int v : range) {
            result.add(v);
        }
        return result;
    }

    @Test
    void addNumbers_throwsWhenFromGreaterThanTo() {
        IntegerRange range = new IntegerRange();
        assertThrows(IllegalArgumentException.class, () -> range.addNumbers(5, 3));
    }

    @Test
    void addNumbers_singlePointRange_isInclusive() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(7, 7);

        assertEquals(List.of(7), toList(range));
    }

    @Test
    void addNumbers_normalRange_isInclusiveOnBothEnds() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);

        assertEquals(List.of(1, 2, 3), toList(range));
    }

    @Test
    void iterator_ordersRangesAscendingByStart() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(10, 12);
        range.addNumbers(1, 2);

        assertEquals(List.of(1, 2, 10, 11, 12), toList(range));
    }

    @Test
    void addNumbers_sameStart_extendsRangeWhenNewToIsLarger() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        range.addNumbers(1, 5);

        assertEquals(List.of(1, 2, 3, 4, 5), toList(range));
    }

    @Test
    void addNumbers_overlappingRanges_mergesBySkippingDuplicates() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 5);
        range.addNumbers(3, 7);

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), toList(range));
    }

    @Test
    void addNumbers_adjacentRanges_becomeContinuousSequence() {
        IntegerRange range = new IntegerRange();
        range.addNumbers(1, 3);
        range.addNumbers(4, 6);

        assertEquals(List.of(1, 2, 3, 4, 5, 6), toList(range));
    }

    @Test
    void copy_createsIndependentCopy() {
        IntegerRange original = new IntegerRange();
        original.addNumbers(1, 3);

        IntegerRange copied = original.copy();
        original.addNumbers(10, 10);

        assertEquals(List.of(1, 2, 3), toList(copied));
        assertEquals(List.of(1, 2, 3, 10), toList(original));
    }
}
