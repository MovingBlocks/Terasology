// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring;

/**
 * Activity allows for the use the use of an activity in a try-with-resources block - when the block ends so too does the activity.
 */
@FunctionalInterface
public interface Activity extends AutoCloseable {

    @Override
    void close();
}
