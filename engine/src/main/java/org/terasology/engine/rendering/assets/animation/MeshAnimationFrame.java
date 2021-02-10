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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 */
public class MeshAnimationFrame {
    private List<Vector3f> bonePositions;
    private List<Quaternionf> boneRotations;
    private List<Vector3f> boneScales;

    public MeshAnimationFrame(List<Vector3f> bonePositions, List<Quaternionf> boneRotations, List<Vector3f> boneScales) {
        this.bonePositions = Lists.newArrayList(bonePositions);
        this.boneRotations = Lists.newArrayList(boneRotations);
        this.boneScales = Lists.newArrayList(boneScales);
    }

    public Vector3f getPosition(int boneId) {
        return bonePositions.get(boneId);
    }

    public Quaternionf getRotation(int boneId) {
        return boneRotations.get(boneId);
    }

    public Vector3f getBoneScale(int boneId) {
        return boneScales.get(boneId);
    }
}
