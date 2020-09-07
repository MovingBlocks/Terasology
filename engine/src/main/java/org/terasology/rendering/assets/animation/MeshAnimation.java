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

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;

/**
 */
public abstract class MeshAnimation extends Asset<MeshAnimationData> {

    protected MeshAnimation(ResourceUrn urn, AssetType<?, MeshAnimationData> assetType) {
        super(urn, assetType);
    }

    public abstract boolean isValidAnimationFor(SkeletalMesh mesh);

    public abstract int getBoneCount();

    public abstract int getFrameCount();

    public abstract MeshAnimationFrame getFrame(int frame);

    public abstract String getBoneName(int index);

    public abstract float getTimePerFrame();

    public abstract AABB getAabb();
}
