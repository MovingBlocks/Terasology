// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation;


/**
 * Enqueues prefabs or components in the form to be converted into entities
 * later in the process of chunk finalization.
 */
public interface EntityProvider {

    default void initialize() { }

    void process(Region region, EntityBuffer buffer);
}
