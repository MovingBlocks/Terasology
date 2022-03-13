// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.mainMenu;

/**
 * Gets notified whenever a long-running background task has performed some work.
 */
@FunctionalInterface
public interface ProgressListener {

    /**
     * @param percent in the range [0..1]
     */
    void onProgress(float percent);
}
