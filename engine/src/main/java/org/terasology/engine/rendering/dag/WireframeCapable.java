// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

/**
 * Classes implementing this interface are capable of producing
 * a wireframe rendering that can be triggered on or off.
 */
public interface WireframeCapable {

    /**
     * Enables the wireframe rendering mode.
     */
    void enableWireframe();

    /**
     * Disables the wireframe rendering mode.
     */
    void disableWireframe();
}
