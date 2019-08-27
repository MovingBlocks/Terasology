/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.assets.gltf;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.animation.MeshAnimationBundleData;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.rendering.assets.gltf.model.GLTF;
import org.terasology.rendering.assets.gltf.model.GLTFAnimation;
import org.terasology.rendering.assets.skeletalmesh.Bone;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

@RegisterAssetFileFormat
public class GLTFAnimationFormat extends GLTFCommonFormat<MeshAnimationBundleData> {

    public GLTFAnimationFormat(AssetManager assetManager) {
        super(assetManager, "gltf");
    }

    @Override
    public MeshAnimationBundleData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader in = new InputStreamReader(inputs.get(0).openStream())) {
            GLTF gltf = gson.fromJson(in, GLTF.class);

            checkVersionSupported(urn, gltf);
            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

            Map<ResourceUrn, MeshAnimationData> result = Maps.newLinkedHashMap();

            if (gltf.getSkins().isEmpty()) {
                throw new IOException("Skeletal mesh '" + urn + "' missing skin");
            }
            List<String> boneNames = Lists.newArrayList();
            TIntIntMap bonePositions = new TIntIntHashMap();
            TIntList boneParents = new TIntArrayList();

            TIntObjectMap<Bone> bones = loadBones(gltf, loadedBuffers);
            bones.forEachValue(x -> {
                bonePositions.put(x.getIndex(), boneNames.size());
                boneNames.add(x.getName());
                return true;
            });

            bones.forEachValue(x -> {
                if (x.getParentIndex() != -1) {
                    boneParents.add(bonePositions.get(x.getParentIndex()));
                } else {
                    boneParents.add(MeshAnimationData.NO_PARENT);
                }
                return true;
            });


            for (int index = 0; index < gltf.getAnimations().size(); ++index) {
                GLTFAnimation animation = gltf.getAnimations().get(index);
                String name = animation.getName();
                if (Strings.isNullOrEmpty(name)) {
                    name = "anim_" + index;
                }

                List<MeshAnimationFrame> frames = Lists.newArrayList();

                // TODO: Basically we need to process all the gltf animation to produce strictly spaced frames compatible with the existing animation system - probably at 60fps, and then return that

                MeshAnimationData animationData = new MeshAnimationData(boneNames, boneParents, frames, 0.1f, AABB.createEmpty());
                result.put(new ResourceUrn(urn, name), animationData);
            }

            return new MeshAnimationBundleData(result);
        }
    }
}
