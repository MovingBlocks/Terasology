// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Math;
import org.joml.Vector3f;

public class SphereBuilder {
    private boolean textured = false;
    private float radius = 1;
    private int slices = 6;
    private int stacks = 6;
    private boolean normals = false;
    public SphereBuilder() {

    }
    public SphereBuilder setNormal(boolean value) {
        this.normals = value;
        return this;
    }

    public SphereBuilder setTextured(boolean hasTexture) {
        textured = hasTexture;
        return this;
    }

    public SphereBuilder setStacks(int value) {
        this.stacks = value;
        return this;
    }

    public SphereBuilder setSlices(int value) {
        this.slices = value;
        return this;
    }

    public SphereBuilder setRadius(float value) {
        this.radius = value;
        return this;
    }

    // refrence: https://github.com/caosdoar/spheres/blob/master/src/spheres.cpp
    public StandardMeshData build() {
//        MeshBuilder builder = new MeshBuilder();
        StandardMeshData meshData = new StandardMeshData();

        Vector3f pos = new Vector3f();
        int count = 0;

        float s = 0;
        float t = 1.0f;


        float ds = 1.0f / slices;
        float dt = 1.0f / stacks;

        meshData.vertices.add(new float[]{0.0f, radius, 0.0f});
        if (this.normals) {
            meshData.normals.add(new float[]{0.0f, radius, 0.0f});
        }
        if (this.textured) {
            meshData.uv0.add(new float[]{s, t});
        }
        count++;
        for (int j = 0; j < slices - 1; ++j) {
            double polar = (Math.PI * (j + 1)) / slices;
            double sp = Math.sin(polar);
            double cp = Math.sin(polar);
            s = 0.0f;
            for (int i = 0; i < stacks; ++i) {
                double azimuth = (2.0 * Math.PI * i) / stacks;
                double sa = Math.sin(azimuth);
                double ca = Math.cos(azimuth);
                if (this.normals) {
                    meshData.normals.add(new float[]{(float) (sp * ca), (float) cp, (float) (sp * sa)});
                }
                if (this.textured) {
                    s += ds;
                    meshData.uv0.add(new float[]{s, t});
                }
                meshData.vertices.add(new float[]{(float) ((sp * ca) * radius), (float) (cp * radius), (float) ((sp * sa) * radius)});
                count++;
            }
            t -= dt;
        }
        if (this.textured) {
            meshData.uv0.add(new float[]{s, t});
        }
        if (this.normals) {
            meshData.normals.add(new float[]{0.0f, -radius, 0.0f});
        }
        meshData.vertices.add(new float[]{0.0f, -radius, 0.0f});
        count++;

        for (int i = 0; i < stacks; i++) {
            int a = i + 1;
            int b = (i + 1) % stacks + 1;
            meshData.getIndices().add(new int[]{0, a, b});
        }

        for (int j = 0; j < slices - 2; ++j) {
            int aStart = j * stacks + 1;
            int bStart = (j + 1) * stacks + 1;
            for (int i = 0; i < stacks; ++i) {
                int a = aStart + i;
                int a1 = aStart + (i + 1) % stacks;
                int b = bStart + i;
                int b1 = bStart + (i + 1) % stacks;
                meshData.getIndices().add(new int[]{a, a1, b1});
                meshData.getIndices().add(new int[]{b1, a1, b});
            }
        }

        for (int i = 0; i < stacks; i++) {
            int a = i + stacks * (slices - 2) + 1;
            int b = (i + 1) % stacks + stacks * (slices - 2) + 1;
            meshData.getIndices().add(new int[]{count - 1, a, b});
        }
        return meshData;
    }
}
