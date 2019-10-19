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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.AABB;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.animation.MeshAnimationBundleData;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.rendering.gltf.model.*;
import org.terasology.rendering.assets.skeletalmesh.Bone;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterAssetFileFormat
public class GLTFAnimationFormat extends GLTFCommonFormat<MeshAnimationBundleData> {

    private static float TIME_PER_FRAME = 1f / 60f;

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

                animations.put(new ResourceUrn(urn, name), loadAnimation(gltf, gltfAnimation, loadedBuffers, nodeToJoint, boneNames, boneParents, bones));
            }

            return new MeshAnimationBundleData(animations);
        }
    }

    private MeshAnimationData loadAnimation(GLTF gltf, GLTFAnimation animation, List<byte[]> loadedBuffers, TIntIntMap boneIndexMapping, List<String> boneNames, TIntList boneParents, List<Bone> bones) throws IOException {
        List<ChannelReader> channelReaders = new ArrayList<>();
        for (GLTFChannel channel : animation.getChannels()) {
            switch (channel.getTarget().getPath()) {
                case TRANSLATION:
                    channelReaders.add(new PositionChannelReader(gltf, animation, channel, boneIndexMapping, loadedBuffers));
                    break;
                case ROTATION:
                    channelReaders.add(new RotationChannelReader(gltf, animation, channel, boneIndexMapping, loadedBuffers));
                    break;
                case SCALE:
                    channelReaders.add(new ScaleChannelReader(gltf, animation, channel, boneIndexMapping, loadedBuffers));
                    break;
                default:
                    break;
            }

        }
        int frameCount = (int) (channelReaders.stream().map(ChannelReader::endTime).reduce(Float::max).orElse(0f) / TIME_PER_FRAME) + 1;
        List<MeshAnimationFrame> frames = new ArrayList<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            float time = i * TIME_PER_FRAME;
            List<Vector3f> boneLocations = new ArrayList<>();
            List<Quat4f> boneRotations = new ArrayList<>();
            List<Vector3f> boneScales = new ArrayList<>();
            for (Bone bone : bones) {
                boneLocations.add(new Vector3f(bone.getLocalPosition()));
                boneRotations.add(new Quat4f(bone.getLocalRotation()));
                // TODO: Default scale
                boneScales.add(new Vector3f(Vector3f.one()));
            }
            MeshAnimationFrame frame = new MeshAnimationFrame(boneLocations, boneRotations, boneScales);
            channelReaders.forEach(x -> x.updateFrame(time, frame));
            frames.add(frame);
        }

        MeshAnimationData animationData = new MeshAnimationData(boneNames, boneParents, frames, TIME_PER_FRAME, AABB.createEmpty());
        return animationData;
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

    private List<Quat4f> getQuat4fs(GLTF gltf, List<byte[]> loadedBuffers, int accessorIndex) throws IOException {
        TFloatList floats = getFloats(gltf, loadedBuffers, accessorIndex);
        List<Quat4f> quats = Lists.newArrayListWithCapacity(floats.size() / 4);
        for (int i = 0; i < floats.size(); i += 4) {
            quats.add(new Quat4f(floats.get(i), floats.get(i + 1), floats.get(i + 2), floats.get(i + 3)));
        }
        return quats;
    }

    private interface ChannelReader {
        void updateFrame(float time, MeshAnimationFrame frame);

        float endTime();

    }

    private class PositionChannelReader implements ChannelReader {

        private TFloatList times;
        private List<Vector3f> positions;
        private int bone;
        private GLTFInterpolation interpolation;

        public PositionChannelReader(GLTF gltf, GLTFAnimation animation, GLTFChannel channel, TIntIntMap boneIndexMapping, List<byte[]> loadedBuffers) throws IOException {
            GLTFAnimationSampler sampler = animation.getSamplers().get(channel.getSampler());
            interpolation = sampler.getInterpolation();
            times = getFloats(gltf, loadedBuffers, sampler.getInput());
            bone = boneIndexMapping.get(channel.getTarget().getNode());
            positions = getVector3fs(gltf, loadedBuffers, sampler.getOutput());
        }

        @Override
        public void updateFrame(float time, MeshAnimationFrame frame) {
            int upperFrame = 0;
            while (upperFrame < times.size() && times.get(upperFrame) < time) {
                upperFrame++;
            }
            int lowerFrame = Math.max(0, upperFrame - 1);
            if (upperFrame == lowerFrame) {
                frame.getPosition(bone).set(positions.get(lowerFrame));
            } else {
                float t = time - times.get(lowerFrame) / (times.get(upperFrame) - times.get(lowerFrame));
                interpolation.interpolate(positions.get(lowerFrame), positions.get(upperFrame), t, frame.getPosition(bone));
            }
        }

        @Override
        public float endTime() {
            return times.get(times.size() - 1);
        }


    }

    private class ScaleChannelReader implements ChannelReader {

        private TFloatList times;
        private List<Vector3f> scales;
        private int bone;
        private GLTFInterpolation interpolation;

        public ScaleChannelReader(GLTF gltf, GLTFAnimation animation, GLTFChannel channel, TIntIntMap boneIndexMapping, List<byte[]> loadedBuffers) throws IOException {
            GLTFAnimationSampler sampler = animation.getSamplers().get(channel.getSampler());
            interpolation = sampler.getInterpolation();
            times = getFloats(gltf, loadedBuffers, sampler.getInput());
            bone = boneIndexMapping.get(channel.getTarget().getNode());
            scales = getVector3fs(gltf, loadedBuffers, sampler.getOutput());
        }

        @Override
        public void updateFrame(float time, MeshAnimationFrame frame) {
            int upperFrame = 0;
            while (upperFrame < times.size() && times.get(upperFrame) < time) {
                upperFrame++;
            }
            int lowerFrame = Math.max(0, upperFrame - 1);
            if (upperFrame == lowerFrame) {
                frame.getBoneScale(bone).set(scales.get(lowerFrame));
            } else {
                float t = time - times.get(lowerFrame) / (times.get(upperFrame) - times.get(lowerFrame));
                interpolation.interpolate(scales.get(lowerFrame), scales.get(upperFrame), t, frame.getBoneScale(bone));
            }
        }

        @Override
        public float endTime() {
            return times.get(times.size() - 1);
        }


    }

    private class RotationChannelReader implements ChannelReader {

        private TFloatList times;
        private List<Quat4f> rotations;
        private int bone;
        private GLTFInterpolation interpolation;

        public RotationChannelReader(GLTF gltf, GLTFAnimation animation, GLTFChannel channel, TIntIntMap boneIndexMapping, List<byte[]> loadedBuffers) throws IOException {
            GLTFAnimationSampler sampler = animation.getSamplers().get(channel.getSampler());
            interpolation = sampler.getInterpolation();
            times = getFloats(gltf, loadedBuffers, sampler.getInput());
            bone = boneIndexMapping.get(channel.getTarget().getNode());
            rotations = getQuat4fs(gltf, loadedBuffers, sampler.getOutput());
            for (Quat4f rot : rotations) {
                rot.inverse();
            }
        }

        @Override
        public void updateFrame(float time, MeshAnimationFrame frame) {
            int upperFrame = 0;
            while (upperFrame < times.size() && times.get(upperFrame) < time) {
                upperFrame++;
            }
            int lowerFrame = Math.max(0, upperFrame - 1);
            if (upperFrame == lowerFrame) {
                frame.getRotation(bone).set(rotations.get(lowerFrame));
            } else {
                float t = time - times.get(lowerFrame) / (times.get(upperFrame) - times.get(lowerFrame));
                interpolation.interpolate(rotations.get(lowerFrame), rotations.get(upperFrame), t, frame.getRotation(bone));
            }
        }

        @Override
        public float endTime() {
            return times.get(times.size() - 1);
        }


    }
}
