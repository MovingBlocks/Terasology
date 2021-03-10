// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.mathTypes;

import org.terasology.engine.math.IntegerRange;
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
