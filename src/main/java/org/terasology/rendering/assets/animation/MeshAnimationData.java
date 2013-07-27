/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.ImmutableList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.asset.AssetData;

import java.util.List;

/**
 * @author Immortius
 */
public class MeshAnimationData implements AssetData {

    public static final int NO_PARENT = -1;

    private List<String> boneNames;
    private TIntList boneParent;
    private List<MeshAnimationFrame> frames;
    private float timePerFrame;

    /**
     * @param boneNames    The names of the bones this animation expects
     * @param boneParents  The indices of the parent of each bone in the boneNames list, NO_PARENT for no parent.
     * @param frames
     * @param timePerFrame
     */
    public MeshAnimationData(List<String> boneNames, TIntList boneParents, List<MeshAnimationFrame> frames, float timePerFrame) {
        if (boneNames.size() != boneParents.size()) {
            throw new IllegalArgumentException("Bone names and boneParent indices must align");
        }
        this.boneNames = ImmutableList.copyOf(boneNames);
        this.boneParent = new TIntArrayList(boneParents);
        this.frames = ImmutableList.copyOf(frames);
        this.timePerFrame = timePerFrame;
    }

    public List<String> getBoneNames() {
        return boneNames;
    }

    public TIntList getBoneParent() {
        return boneParent;
    }

    public List<MeshAnimationFrame> getFrames() {
        return frames;
    }

    public float getTimePerFrame() {
        return timePerFrame;
    }
}
