// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class IntegerRange implements Iterable<Integer> {
    private Map<Integer, Integer> ranges = new TreeMap<>();

    public void addNumbers(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("From can't be larger than to");
        }

        Integer oldTo = ranges.get(from);
        if (oldTo == null || oldTo < to) {
            ranges.put(from, to);
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RangesIterator(ranges);
    }

    private static final class RangesIterator implements Iterator<Integer> {
        private Iterator<Map.Entry<Integer, Integer>> rangesIterator;
        private Integer next;
        private Integer rangeMax;

        private RangesIterator(Map<Integer, Integer> iterator) {
            this.rangesIterator = iterator.entrySet().iterator();
            goToNextRange();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Integer next() {
            if (next == null) {
                throw new NoSuchElementException("You have reached the end of the iterator");
            }
            int result = next;
            if (next < rangeMax) {
                next++;
            } else {
                goToNextRange();
            }
            return result;
        }

        private void goToNextRange() {
            Integer newNext = null;
            Integer newRangeMax = null;

            // Go through the ranges in ascending order (we use TreeMap for that) and find one that we want to
            // iterate through
            while (rangesIterator.hasNext()) {
                Map.Entry<Integer, Integer> nextRange = rangesIterator.next();
                int tempNext = nextRange.getKey();
                int tempRangeMax = nextRange.getValue();

                // If we reached MAX_VALUE, we should just stop the iteration
                if (rangeMax != null && rangeMax == Integer.MAX_VALUE) {
                    break;
                }

                // If the new range starts before the last one has finished, try to iterate from after the range
                // (last range max plus 1)
                if (rangeMax != null && tempNext <= rangeMax) {
                    tempNext = rangeMax + 1;
                }
                // If the new range actually has some numbers, set the values as new range to iterate over
                if (tempNext <= tempRangeMax) {
                    newNext = tempNext;
                    newRangeMax = tempRangeMax;
                    break;
                }
            }

            next = newNext;
            rangeMax = newRangeMax;
        }
    }
}
