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
package org.terasology.rendering.gltf;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.animation.MeshAnimationBundleData;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.gltf.model.GLTF;
import org.terasology.rendering.gltf.model.GLTFAccessor;
import org.terasology.rendering.gltf.model.GLTFAnimation;
import org.terasology.rendering.gltf.model.GLTFAnimationSampler;
import org.terasology.rendering.gltf.model.GLTFBufferView;
import org.terasology.rendering.gltf.model.GLTFChannel;
import org.terasology.rendering.gltf.model.GLTFSkin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterAssetFileFormat
public class GLTFAnimationFormat extends GLTFCommonFormat<MeshAnimationBundleData> {

    private static final float TIME_PER_FRAME = 1f / 60f;

    public GLTFAnimationFormat(AssetManager assetManager) {
        super(assetManager, "gltf");
    }

    @Override
    public MeshAnimationBundleData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader in = new InputStreamReader(inputs.get(0).openStream())) {
            GLTF gltf = gson.fromJson(in, GLTF.class);

            checkVersionSupported(urn, gltf);
            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

            if (gltf.getSkins().isEmpty()) {
                throw new IOException("Skeletal mesh '" + urn + "' missing skin");
            }
            GLTFSkin skin = gltf.getSkins().get(0);
            List<String> boneNames = Lists.newArrayList();
            TIntList boneParents = new TIntArrayList();
            TIntIntMap nodeToJoint = new TIntIntHashMap();
            for (int i = 0; i < skin.getJoints().size(); i++) {
                nodeToJoint.put(skin.getJoints().get(i), i);
            }

            List<Bone> bones = loadBones(gltf, skin, loadedBuffers);
            bones.forEach(x -> boneNames.add(x.getName()));
            bones.forEach(x -> {
                if (x.getParentIndex() != -1) {
                    boneParents.add(x.getParentIndex());
                } else {
                    boneParents.add(MeshAnimationData.NO_PARENT);
                }
            });


            Map<ResourceUrn, MeshAnimationData> animations = new HashMap<>();
            for (int index = 0; index < gltf.getAnimations().size(); ++index) {
                GLTFAnimation gltfAnimation = gltf.getAnimations().get(index);
                String name = gltfAnimation.getName();
                if (Strings.isNullOrEmpty(name)) {
                    name = "anim_" + index;
                }

                animations.put(new ResourceUrn(urn, name), loadAnimation(gltf, gltfAnimation, loadedBuffers,
                    nodeToJoint, boneNames, boneParents, bones));
            }

            return new MeshAnimationBundleData(animations);
        }
    }

    private MeshAnimationData loadAnimation(GLTF gltf, GLTFAnimation animation, List<byte[]> loadedBuffers,
                                            TIntIntMap boneIndexMapping, List<String> boneNames, TIntList boneParents
        , List<Bone> bones) throws IOException {
        List<ChannelReader> channelReaders = new ArrayList<>();

        for (GLTFChannel channel : animation.getChannels()) {
            GLTFAnimationSampler sampler = animation.getSamplers().get(channel.getSampler());
            TFloatList times = getFloats(gltf, loadedBuffers, sampler.getInput());
            int bone = boneIndexMapping.get(channel.getTarget().getNode());

            switch (channel.getTarget().getPath()) {
                case TRANSLATION: {
                    List<Vector3f> data = getVector3fs(gltf, loadedBuffers, sampler.getOutput());

                    channelReaders.add(new BufferChannelReader<>(times, data, sampler.getInterpolation()::interpolate
                        , x -> x.getPosition(bone)));
                    break;
                }
                case ROTATION: {
                    List<Quaternionf> data = getQuat4fs(gltf, loadedBuffers, sampler.getOutput());
                    channelReaders.add(new BufferChannelReader<>(times, data, sampler.getInterpolation()::interpolate
                        , x -> x.getRotation(bone)));
                    break;
                }
                case SCALE: {
                    List<Vector3f> data = getVector3fs(gltf, loadedBuffers, sampler.getOutput());
                    channelReaders.add(new BufferChannelReader<>(times, data, sampler.getInterpolation()::interpolate
                        , x -> x.getBoneScale(bone)));
                    break;
                }
                default:
                    break;
            }

        }
        int frameCount =
            (int) (channelReaders.stream().map(ChannelReader::endTime).reduce(Float::max).orElse(0f) / TIME_PER_FRAME) + 1;
        List<MeshAnimationFrame> frames = new ArrayList<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            float time = i * TIME_PER_FRAME;
            List<Vector3f> boneLocations = new ArrayList<>();
            List<Quaternionf> boneRotations = new ArrayList<>();
            List<Vector3f> boneScales = new ArrayList<>();
            for (Bone bone : bones) {
                boneLocations.add(new Vector3f(bone.getLocalPosition()));
                boneRotations.add(new Quaternionf(bone.getLocalRotation()));
                boneScales.add(new Vector3f(bone.getLocalScale()));
            }
            MeshAnimationFrame frame = new MeshAnimationFrame(boneLocations, boneRotations, boneScales);
            channelReaders.forEach(x -> x.updateFrame(time, frame));
            frames.add(frame);
        }

        return new MeshAnimationData(boneNames, boneParents, frames, TIME_PER_FRAME, AABB.createEmpty());
    }

    private TFloatList getFloats(GLTF gltf, List<byte[]> loadedBuffers, int accessorIndex) throws IOException {
        GLTFAccessor accessor = gltf.getAccessors().get(accessorIndex);
        GLTFBufferView bufferView = gltf.getBufferViews().get(accessor.getBufferView());
        TFloatList floats = new TFloatArrayList();
        readBuffer(loadedBuffers.get(bufferView.getBuffer()), accessor, bufferView, floats);
        return floats;
    }

    private List<Vector3f> getVector3fs(GLTF gltf, List<byte[]> loadedBuffers, int accessorIndex) throws IOException {
        TFloatList floats = getFloats(gltf, loadedBuffers, accessorIndex);
        List<Vector3f> vectors = Lists.newArrayListWithCapacity(floats.size() / 3);
        for (int i = 0; i < floats.size(); i += 3) {
            vectors.add(new Vector3f(floats.get(i), floats.get(i + 1), floats.get(i + 2)));
        }
        return vectors;
    }

    private List<Quaternionf> getQuat4fs(GLTF gltf, List<byte[]> loadedBuffers, int accessorIndex) throws IOException {
        TFloatList floats = getFloats(gltf, loadedBuffers, accessorIndex);
        List<Quaternionf> quats = Lists.newArrayListWithCapacity(floats.size() / 4);
        for (int i = 0; i < floats.size(); i += 4) {
            quats.add(new Quaternionf(floats.get(i), floats.get(i + 1), floats.get(i + 2), floats.get(i + 3)));
        }
        return quats;
    }

    private interface ChannelReader {
        void updateFrame(float time, MeshAnimationFrame frame);

        float endTime();
    }

    private interface Interpolator<T> {
        void interpolate(T a, T b, float delta, T out);
    }

    private interface TargetRetriever<T> {
        T getTarget(MeshAnimationFrame frame);
    }

    private class BufferChannelReader<T> implements ChannelReader {

        private TFloatList times;
        private List<T> data;
        private Interpolator<T> interpolator;
        private TargetRetriever<T> targetRetriever;

        BufferChannelReader(TFloatList times, List<T> data, Interpolator<T> interpolator,
                            TargetRetriever<T> targetRetriever) {
            this.times = times;
            this.data = data;
            this.interpolator = interpolator;
            this.targetRetriever = targetRetriever;
        }

        public void updateFrame(float time, MeshAnimationFrame frame) {
            int upperFrame = 0;
            while (upperFrame < times.size() - 1 && times.get(upperFrame) < time) {
                upperFrame++;
            }
            int lowerFrame = Math.max(0, upperFrame - 1);
            T target = targetRetriever.getTarget(frame);
            if (upperFrame == lowerFrame) {
                interpolator.interpolate(data.get(lowerFrame), data.get(lowerFrame), 0, target);
            } else {
                float t = (time - times.get(lowerFrame)) / (times.get(upperFrame) - times.get(lowerFrame));
                interpolator.interpolate(data.get(lowerFrame), data.get(upperFrame), t, target);
            }
        }

        public float endTime() {
            return times.get(times.size() - 1);
        }
    }
}
