// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event;

/**
 */
public final class EventPriority {
    public static final int PRIORITY_CRITICAL = 200;
    public static final int PRIORITY_HIGH = 150;
    public static final int PRIORITY_NORMAL = 100;
    public static final int PRIORITY_LOW = 50;
    public static final int PRIORITY_TRIVIAL = 0;

    private EventPriority() {
    }
}
