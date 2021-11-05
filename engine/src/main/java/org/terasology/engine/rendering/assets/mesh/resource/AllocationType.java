// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.opengl.GL33;

public enum AllocationType {
    STATIC(GL33.GL_STATIC_DRAW),
    DYNAMIC(GL33.GL_DYNAMIC_DRAW),
    STREAM(GL33.GL_STREAM_DRAW);


    public final int glCall;
    AllocationType(int gl) {
        this.glCall = gl;
    }
}
