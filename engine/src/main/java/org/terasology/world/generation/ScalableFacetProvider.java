// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.generation;

public interface ScalableFacetProvider extends FacetProvider {
    void process(GeneratingRegion region, float scale);

    default void process(GeneratingRegion region) {
        process(region, 1);
    }
}
