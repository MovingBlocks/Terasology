/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.rendering.md5;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.AABB;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RegisterAssetFileFormat
public class MD5AnimationLoader extends AbstractAssetFileFormat<MeshAnimationData> {

    private static final int POSITION_X_FLAG = 0x1;
    private static final int POSITION_Y_FLAG = 0x2;
    private static final int POSITION_Z_FLAG = 0x4;
    private static final int ORIENTATION_X_FLAG = 0x8;
    private static final int ORIENTATION_Y_FLAG = 0x10;
    private static final int ORIENTATION_Z_FLAG = 0x20;

    private Pattern jointPattern = Pattern.compile("\"(.*)\"\\s+" + MD5Patterns.INTEGER_PATTERN +
            "\\s*" + MD5Patterns.INTEGER_PATTERN + "\\s*" + MD5Patterns.INTEGER_PATTERN);
    private Pattern doubleVectorPattern = Pattern.compile(MD5Patterns.VECTOR3_PATTERN +
            "\\s*" + MD5Patterns.VECTOR3_PATTERN);
    private Pattern frameStartPattern = Pattern.compile("frame " + MD5Patterns.INTEGER_PATTERN + " \\{");

    public MD5AnimationLoader() {
        super("md5anim");
    }

    @Override
    public MeshAnimationData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream stream = inputs.get(0).openStream()) {
            MD5 md5 = parse(stream);
            return createAnimation(md5);
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing " + inputs.get(0).getFilename(), e);
        }
    }

    private MeshAnimationData createAnimation(MD5 md5) {
        List<String> boneNames = Lists.newArrayListWithCapacity(md5.numJoints);
        TIntList boneParents = new TIntArrayList(md5.numJoints);
        for (int i = 0; i < md5.numJoints; ++i) {
            boneNames.add(md5.joints[i].name);
            boneParents.add(md5.joints[i].parent);
        }
        float timePerFrame = 1.0f / md5.frameRate;

        List<MeshAnimationFrame> frames = Lists.newArrayList();
        for (int frameIndex = 0; frameIndex < md5.numFrames; ++frameIndex) {
            MD5Frame frame = md5.frames[frameIndex];
            List<Vector3f> positions = Lists.newArrayListWithExpectedSize(md5.numJoints);
            List<Vector3f> rawRotations = Lists.newArrayListWithExpectedSize(md5.numJoints);
            for (int i = 0; i < md5.numJoints; ++i) {
                positions.add(new Vector3f(md5.baseFramePosition[i]));
                rawRotations.add(new Vector3f(md5.baseFrameOrientation[i]));
            }

            for (int jointIndex = 0; jointIndex < md5.numJoints; ++jointIndex) {
                int compIndex = 0;
                if ((md5.joints[jointIndex].flags & POSITION_X_FLAG) != 0) {
                    positions.get(jointIndex).x = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                    compIndex++;
                }
                if ((md5.joints[jointIndex].flags & POSITION_Y_FLAG) != 0) {
                    positions.get(jointIndex).y = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                    compIndex++;
                }
                if ((md5.joints[jointIndex].flags & POSITION_Z_FLAG) != 0) {
                    positions.get(jointIndex).z = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                    compIndex++;
                }
                if ((md5.joints[jointIndex].flags & ORIENTATION_X_FLAG) != 0) {
                    rawRotations.get(jointIndex).x = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                    compIndex++;
                }
                if ((md5.joints[jointIndex].flags & ORIENTATION_Y_FLAG) != 0) {
                    rawRotations.get(jointIndex).y = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                    compIndex++;
                }
                if ((md5.joints[jointIndex].flags & ORIENTATION_Z_FLAG) != 0) {
                    rawRotations.get(jointIndex).z = frame.components[md5.joints[jointIndex].startIndex + compIndex];
                }
            }

            List<Quat4f> rotations = rawRotations.stream().map(rot ->
                    MD5ParserCommon.completeQuat4f(rot.x, rot.y, rot.z)).collect(Collectors.toCollection(ArrayList::new));

            // Rotate just the root bone to correct for coordinate system differences
            rotations.set(0, MD5ParserCommon.correctQuat4f(rotations.get(0)));
            positions.set(0, MD5ParserCommon.correctOffset(positions.get(0)));


            frames.add(new MeshAnimationFrame(positions, rotations));

        }
        AABB aabb = AABB.createEncompassing(Arrays.asList(md5.bounds));
        return new MeshAnimationData(boneNames, boneParents, frames, timePerFrame, aabb);
    }


    private MD5 parse(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
        MD5 md5 = new MD5();
        String line = MD5ParserCommon.readToLine(reader, "MD5Version ");
        md5.version = Integer.parseInt(line.split(" ", 3)[1]);

        line = MD5ParserCommon.readToLine(reader, "commandline ");
        Matcher commandlineMatch = Pattern.compile(MD5Patterns.COMMAND_LINE_PATTERN).matcher(line);
        if (commandlineMatch.matches()) {
            md5.commandline = commandlineMatch.group(1);
        }

        line = MD5ParserCommon.readToLine(reader, "numFrames ");
        md5.numFrames = Integer.parseInt(line.split(" ", 3)[1]);
        line = MD5ParserCommon.readToLine(reader, "numJoints ");
        md5.numJoints = Integer.parseInt(line.split(" ", 3)[1]);
        line = MD5ParserCommon.readToLine(reader, "frameRate ");
        md5.frameRate = Integer.parseInt(line.split(" ", 3)[1]);
        line = MD5ParserCommon.readToLine(reader, "numAnimatedComponents ");
        md5.numAnimatedComponents = Integer.parseInt(line.split(" ", 3)[1]);

        MD5ParserCommon.readToLine(reader, "hierarchy {");
        readHierarchy(reader, md5);

        MD5ParserCommon.readToLine(reader, "bounds {");
        readBounds(reader, md5);

        MD5ParserCommon.readToLine(reader, "baseframe {");
        readBaseFrames(reader, md5);

        readFrames(reader, md5);

        return md5;
    }

    private void readFrames(BufferedReader reader, MD5 md5) throws IOException {
        md5.frames = new MD5Frame[md5.numFrames];
        for (int i = 0; i < md5.numFrames; ++i) {
            String frameStart = MD5ParserCommon.readToLine(reader, "frame ");
            Matcher frameStartMatcher = frameStartPattern.matcher(frameStart);
            if (!frameStartMatcher.find()) {
                throw new IOException("Invalid frame line: \"" + frameStart + "\"");
            }
            int frameIndex = Integer.parseInt(frameStartMatcher.group(1));

            MD5Frame frame = new MD5Frame();
            frame.components = new float[md5.numAnimatedComponents];
            int componentsRead = 0;
            while (componentsRead < md5.numAnimatedComponents) {
                String line = MD5ParserCommon.readNextLine(reader);
                String[] components = line.trim().split("\\s+");
                for (String component : components) {
                    frame.components[componentsRead++] = Float.parseFloat(component);
                }
            }
            md5.frames[frameIndex] = frame;
        }
    }

    private void readBaseFrames(BufferedReader reader, MD5 md5) throws IOException {
        md5.baseFramePosition = new Vector3f[md5.numJoints];
        md5.baseFrameOrientation = new Vector3f[md5.numJoints];
        for (int i = 0; i < md5.numJoints; ++i) {
            String line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = doubleVectorPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid base frame line: \"" + line + "\"");
            }
            md5.baseFramePosition[i] = MD5ParserCommon.readVector3f(matcher.group(1), matcher.group(2), matcher.group(3));
            md5.baseFrameOrientation[i] = MD5ParserCommon.readVector3f(matcher.group(4), matcher.group(5), matcher.group(6));
        }
    }

    private void readBounds(BufferedReader reader, MD5 md5) throws IOException {
        md5.bounds = new AABB[md5.numFrames];
        for (int i = 0; i < md5.numFrames; ++i) {
            String line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = doubleVectorPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid bounds line: \"" + line + "\"");
            }
            Vector3f a = MD5ParserCommon.readVector3fAndCorrect(matcher.group(1), matcher.group(2), matcher.group(3));
            Vector3f b = MD5ParserCommon.readVector3fAndCorrect(matcher.group(4), matcher.group(5), matcher.group(6));
            Vector3f min = new Vector3f();
            min.x = Math.min(a.x, b.x);
            min.y = Math.min(a.y, b.y);
            min.z = Math.min(a.z, b.z);
            Vector3f max = new Vector3f();
            max.x = Math.max(a.x, b.x);
            max.y = Math.max(a.y, b.y);
            max.z = Math.max(a.z, b.z);
            md5.bounds[i] = AABB.createMinMax(min, max);
        }
    }

    private void readHierarchy(BufferedReader reader, MD5 md5) throws IOException {
        md5.joints = new MD5Joint[md5.numJoints];
        for (int i = 0; i < md5.numJoints; ++i) {
            String line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = jointPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid joint line: \"" + line + "\"");
            }
            MD5Joint joint = new MD5Joint();
            joint.name = matcher.group(1);
            joint.parent = Integer.parseInt(matcher.group(2));
            joint.flags = Integer.parseInt(matcher.group(3));
            joint.startIndex = Integer.parseInt(matcher.group(4));
            md5.joints[i] = joint;
        }
    }

    private static class MD5 {
        public int version;
        public String commandline;
        public int numFrames;
        public int numJoints;
        public int frameRate;
        public int numAnimatedComponents;

        public MD5Joint[] joints;
        public AABB[] bounds;
        public Vector3f[] baseFramePosition;
        public Vector3f[] baseFrameOrientation;
        public MD5Frame[] frames;
    }

    public static class MD5Joint {
        public String name;
        public int parent;
        public int flags;
        public int startIndex;
    }

    public static class MD5Frame {
        public float[] components;
    }
}
