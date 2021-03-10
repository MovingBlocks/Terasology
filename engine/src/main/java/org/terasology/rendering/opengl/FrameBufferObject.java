// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

/**
 *
 */
public interface FrameBufferObject {
    void unbindFrame();

    void bindFrame();
}
