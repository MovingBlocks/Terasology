// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SphereBuilder {
    private boolean textured = false;
    private float radius = 1;
    private int horizontalCuts = 6;
    private int verticalCuts = 6;
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

    public SphereBuilder setVerticalCuts(int value) {
        this.verticalCuts = value;
        return this;
    }

    public SphereBuilder setHorizontalCuts(int value) {
        this.horizontalCuts = value;
        return this;
    }

    public SphereBuilder setRadius(float value) {
        this.radius = value;
        return this;
    }

    // refrence: https://github.com/caosdoar/spheres/blob/master/src/spheres.cpp
    public StandardMeshData build() {
        StandardMeshData meshData = new StandardMeshData();

        Matrix4f mat = new Matrix4f().setRotationXYZ(0, 0, (float) (Math.PI / 2.0f));
        Vector4f pos = new Vector4f();
        float s = 0.0f;
        float t = 1.0f;

        float ds = 1.0f / verticalCuts;
        float dt = 1.0f / horizontalCuts ;

        for (int j = 0; j <= horizontalCuts; ++j) {
            double polar = (Math.PI * j) / horizontalCuts;
            double sp = Math.sin(polar );
            double cp = Math.cos(polar);
            s = 0.0f;
            for (int i = 0; i <= verticalCuts; ++i) {
                double azimuth = (2.0 * Math.PI * i) / verticalCuts;
                double sa = Math.sin(azimuth);
                double ca = Math.cos(azimuth);
                if (this.normals) {
                    meshData.normals.add(new float[]{(float) (sp * ca), (float) cp, (float) (sp * sa)});
                }
                if (this.textured) {
                    meshData.uv0.add(new float[]{s, t});
                }
                s += ds;

                pos.set((float) ((sp * ca) * radius), (float) (cp * radius), (float) ((sp * sa) * radius), 1.0f);
                mat.transform(pos);
                meshData.vertices.add(new float[]{pos.x, pos.y, pos.z});
            }
            t -= dt;
        }


        for (int j = 0; j <= horizontalCuts; ++j) {
            int aStart = (j * (verticalCuts + 1)) + 1;
            int bStart = (j + 1) * (verticalCuts + 1) + 1;
            for (int i = 0; i <= verticalCuts; ++i) {
                int a = aStart + i;
                int a1 = aStart + ((i + 1) % (verticalCuts + 1));
                int b = bStart + i;
                int b1 = bStart + ((i + 1) % (verticalCuts + 1));
                meshData.getIndices().add(new int[]{a, b1, b});
                meshData.getIndices().add(new int[]{a1, b1, a});
            }
        }

        return meshData;
    }
}
