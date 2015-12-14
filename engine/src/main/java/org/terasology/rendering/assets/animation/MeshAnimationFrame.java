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

package org.terasology.rendering.assets.animation;

import com.google.common.collect.Lists;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 */
public class MeshAnimationFrame {
    private List<Vector3f> bonePositions;
    private List<Quat4f> boneRotations;

    public MeshAnimationFrame(Collection<Vector3f> bonePositions, Collection<Quat4f> boneRotations) {
        this.bonePositions = Lists.newArrayList(bonePositions);
        this.boneRotations = Lists.newArrayList(boneRotations);
    }

    public Vector3f getPosition(int boneId) {
        return bonePositions.get(boneId);
    }

    public Quat4f getRotation(int boneId) {
        return boneRotations.get(boneId);
    }
}
