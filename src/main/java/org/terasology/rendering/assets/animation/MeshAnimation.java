/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;

import java.util.List;

/**
 * @author Immortius
 */
public class MeshAnimation implements Asset {
    private AssetUri uri;
    private List<String> boneNames;
    private TIntList boneParent;
    private List<MeshAnimationFrame> frames = Lists.newArrayList();
    private float timePerFrame;

    public MeshAnimation(AssetUri uri) {
        this.uri = uri;
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
    }

    /**
     * Sets up the bone hierarchy for this animation
     *
     * @param names   The names of each bone
     * @param parents The index of the parent of each bone, -1 for no parent
     * @throws IllegalArgumentException If the names.length != parents.length
     */
    public void setBones(String[] names, int[] parents) {
        if (names.length != parents.length) {
            throw new IllegalArgumentException();
        }
        boneNames = Lists.newArrayList(names);
        boneParent = new TIntArrayList(parents);
    }

    public boolean isValidAnimationFor(SkeletalMesh mesh) {
        for (int i = 0; i < boneNames.size(); ++i) {
            Bone bone = mesh.getBone(boneNames.get(i));
            boolean hasParent = boneParent.get(i) != -1;
            if (hasParent && (bone.getParent() == null || !bone.getParent().getName().equals(boneNames.get(boneParent.get(i))))) {
                return false;
            } else if (!hasParent && bone.getParent() != null) {
                return false;
            }
        }
        return true;
    }

    public void addFrame(MeshAnimationFrame frame) {
        frames.add(frame);
    }

    public int getBoneCount() {
        return boneNames.size();
    }

    public int getFrameCount() {
        return frames.size();
    }

    public MeshAnimationFrame getFrame(int frame) {
        return frames.get(frame);
    }

    public String getBoneName(int index) {
        return boneNames.get(index);
    }

    public void setTimePerFrame(float time) {
        this.timePerFrame = time;
    }

    public float getTimePerFrame() {
        return timePerFrame;
    }
}
