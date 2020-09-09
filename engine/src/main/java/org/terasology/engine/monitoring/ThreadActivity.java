// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring;

/**
 * A token that is used to express a thread working on a task.
 */
@FunctionalInterface
public interface ThreadActivity extends AutoCloseable {
    @Override
    void close();
}
