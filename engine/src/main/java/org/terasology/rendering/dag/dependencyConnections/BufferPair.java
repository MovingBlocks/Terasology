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
package org.terasology.rendering.dag.dependencyConnections;

import org.terasology.rendering.opengl.FBO;

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
