// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.dependencyConnections;

import org.terasology.engine.rendering.opengl.FBO;

/**
 * This class represents BufferPair, a pair of FBO buffers, which represent a main render target.
 * A pair so you can read from one while you write into the other.
 * BufferPair is used as Data type for DependencyConnection extending class - BufferPairConnection.
 */
public class BufferPair {

    private FBO primaryBuffer;
    private FBO secondaryBuffer;

    public BufferPair(FBO primaryBuffer, FBO secondaryBuffer) {
        this.primaryBuffer = primaryBuffer;
        this.secondaryBuffer = secondaryBuffer;
    }

    public FBO getPrimaryFbo() {
        return primaryBuffer;
    }

    public FBO getSecondaryFbo() {
        return secondaryBuffer;
    }
}
