// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation;

/**
 * Some preset priority values for Updates.priority.
 */
public final class UpdatePriority {
    public static final int PRIORITY_PRODUCES = 250;
    public static final int PRIORITY_CRITICAL = 200;
    public static final int PRIORITY_HIGH = 150;
    public static final int PRIORITY_NORMAL = 100;
    public static final int PRIORITY_LOW = 50;
    public static final int PRIORITY_TRIVIAL = 0;
    public static final int PRIORITY_REQUIRES = -50;

    private UpdatePriority() {
    }

    /**
     * Attempts to translate a priority value into a human-readable string like "PRIORITY_LOW".
     * If the value doesn't match any presets, it just returns the string representation.
     */
    public static String priorityString(int priority) {
        switch (priority) {
            case PRIORITY_PRODUCES:
                return "PRIORITY_PRODUCES (" + priority + ")";
            case PRIORITY_CRITICAL:
                return "PRIORITY_CRITICAL (" + priority + ")";
            case PRIORITY_HIGH:
                return "PRIORITY_HIGH (" + priority + ")";
            case PRIORITY_NORMAL:
                return "PRIORITY_NORMAL (" + priority + ")";
            case PRIORITY_LOW:
                return "PRIORITY_LOW (" + priority + ")";
            case PRIORITY_REQUIRES:
                return "PRIORITY_REQUIRES (" + priority + ")";
            default:
                return Integer.toString(priority);
        }
    }
}
