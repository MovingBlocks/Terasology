// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.time;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for converting timestamps to human readable format
 */
public class DateTimeHelper {

    private DateTimeHelper() {
    }

    /**
     * Provides delta between timestamps in format "X Days XXhr XXm XXs". Days are optional.
     *
     * @param start timestamp of the beginning
     * @param end   timestamp of the end
     * @return delta between timestamps as a string
     */
    public static String getDeltaBetweenTimestamps(final long start, final long end) {
        if (start < 0 || end < 0 || start > end) {
            throw new IllegalArgumentException("Wrong timestamp values: " + start + " or " + end);
        }
        TimeUnit timeUnit = TimeUnit.SECONDS;

        long diffInMilliseconds = end - start;
        long s = timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);

        long days = s / (24 * 60 * 60);
        long rest = s - (days * 24 * 60 * 60);
        long hrs = rest / (60 * 60);
        long rest1 = rest - (hrs * 60 * 60);
        long min = rest1 / 60;
        long sec = s % 60;

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days);
            builder.append(" Days ");
        }

        builder.append(fill2(hrs));
        builder.append("h ");

        builder.append(fill2(min));
        builder.append("m ");

        builder.append(fill2(sec));
        builder.append("s");
        return builder.toString();
    }

    private static String fill2(final long value) {
        String ret = String.valueOf(value);
        return ret.length() < 2 ? "0" + ret : ret;
    }

}
