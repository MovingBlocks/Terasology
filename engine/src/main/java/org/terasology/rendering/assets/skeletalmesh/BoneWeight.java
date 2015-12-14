/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.skeletalmesh;

import org.terasology.math.geom.Vector3f;

/**
 */
public class BoneWeight {
    private Vector3f position = new Vector3f();
    private float bias;
    private int boneIndex;
    private Vector3f normal = new Vector3f();

    public BoneWeight(Vector3f position, float bias, int boneIndex) {
        this.position.set(position);
        this.bias = bias;
        this.boneIndex = boneIndex;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getBias() {
        return bias;
    }

    public int getBoneIndex() {
        return boneIndex;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f normal) {
        this.normal.set(normal);
    }
}

