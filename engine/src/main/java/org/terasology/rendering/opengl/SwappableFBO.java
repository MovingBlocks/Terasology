// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

/**
 * TODO: Add Javadocs
 */
public class SwappableFBO {
    private FBO lastUpdatedFbo;
    private FBO staleFbo;
    private boolean isSwapped;

    public SwappableFBO(FBO lastUpdatedFbo, FBO staleFbo) {
        this.lastUpdatedFbo = lastUpdatedFbo;
        this.staleFbo = staleFbo;
        isSwapped = false;
    }

    public FBO getStaleFbo() {
        return isSwapped ? lastUpdatedFbo : staleFbo;
    }

    public FBO getLastUpdatedFbo() {
        return isSwapped ? staleFbo : lastUpdatedFbo;
    }

    public void swap() {
        isSwapped = !isSwapped;
    }
}
