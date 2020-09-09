// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.mainMenu.preview;

import java.nio.ByteBuffer;

import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.layers.mainMenu.ProgressListener;

/**
 * Creates 2D images based on game worlds.
 */
public interface PreviewGenerator {

    ByteBuffer render(TextureData texData, int scale, ProgressListener progressListener) throws InterruptedException;

    /**
     * Dispose all resources
     */
    void close();
}
