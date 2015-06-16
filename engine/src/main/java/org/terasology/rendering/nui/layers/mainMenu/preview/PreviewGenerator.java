/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.mainMenu.preview;

import java.nio.ByteBuffer;

import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.layers.mainMenu.ProgressListener;

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
