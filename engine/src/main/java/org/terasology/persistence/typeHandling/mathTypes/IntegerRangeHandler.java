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
package org.terasology.persistence.typeHandling.mathTypes;

import org.terasology.math.IntegerRange;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

public class IntegerRangeHandler extends StringRepresentationTypeHandler<IntegerRange> {
    @Override
    public String getAsString(IntegerRange item) {
        if (item == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        Integer currentRangeStart = null;
        Integer currentRangeEnd = null;

        for (int nextNumber : item) {
            if (currentRangeStart != null && currentRangeEnd != null && nextNumber > currentRangeEnd + 1) {
                appendRange(sb, currentRangeStart, currentRangeEnd);
                currentRangeStart = nextNumber;
            } else if (currentRangeStart == null) {
                currentRangeStart = nextNumber;
            }
            currentRangeEnd = nextNumber;
        }

        if (currentRangeStart != null && currentRangeEnd != null) {
            appendRange(sb, currentRangeStart, currentRangeEnd);
        }

        return sb.toString();
    }

    private void appendRange(StringBuilder sb, int rangeStart, int rangeEnd) {
        if (sb.length() > 0) {
            sb.append(',');
        }
        if (rangeStart != rangeEnd) {
            sb.append(rangeStart).append("..").append(rangeEnd);
        } else {
            sb.append(rangeStart);
        }
    }

    @Override
    public IntegerRange getFromString(String representation) {
        String[] rangeParts = representation.split(",");
        IntegerRange range = new IntegerRange();
        for (String rangePart : rangeParts) {
            if (!rangePart.isEmpty()) {
                String[] parts = rangePart.split("\\.\\.");
                if (parts.length == 2) {
                    range.addNumbers(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else if (parts.length == 1) {
                    int number = Integer.parseInt(parts[0]);
                    range.addNumbers(number, number);
                } else {
                    throw new IllegalArgumentException("Unable to parse the range correctly: " + representation);
                }
            }
        }

        return range;
    }
}
