/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.zones;

import org.terasology.module.sandbox.API;

/**
 * This is a {@link LayerWidth} for a layer that has a constant, predetermined width at all paints.
 */
@API
public class ConstantLayerWidth implements LayerWidth {

    private final int width;

    /**
     * @param width the desired width of this layer
     */
    public ConstantLayerWidth(int width) {
        this.width = width;
    }

    @Override
    public int get(int x, int z) {
        return width;
    }
}
