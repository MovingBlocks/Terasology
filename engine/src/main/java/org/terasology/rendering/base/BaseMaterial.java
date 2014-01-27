/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.base;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.base.BaseMaterial;
import org.terasology.rendering.cameras.Camera;

public abstract class BaseMaterial extends AbstractAsset<MaterialData> implements Material {

    public BaseMaterial(AssetUri uri) {
        super(uri);
    }

    public abstract void reload(MaterialData data);

    public abstract void dispose();

    public abstract boolean isDisposed();

    public abstract void recompile();

    public abstract void enable();

    public abstract void setFloat(String name, float f, boolean currentOnly);

    public abstract void setFloat1(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat2(String name, float f1, float f2, boolean currentOnly);

    public abstract void setFloat2(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly);

    public abstract void setFloat3(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    public abstract void setFloat4(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setInt(String name, int i, boolean currentOnly);

    public abstract void setBoolean(String name, boolean value, boolean currentOnly);

    public abstract void setMatrix3(String name, Matrix3f matrix, boolean currentOnly);

    public abstract void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setMatrix4(String name, Matrix4f matrix, boolean currentOnly);

    public abstract void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setTexture(String name, Texture texture);

    public abstract boolean supportsFeature(ShaderProgramFeature feature);

    public abstract void activateFeature(ShaderProgramFeature feature);

    public abstract void deactivateFeature(ShaderProgramFeature feature);

    public abstract void deactivateFeatures(ShaderProgramFeature ... features);

    public abstract void bindTextures();

    @Override
    public void setFloat(String name, float f) {
        setFloat(name, f, false);
    }

    @Override
    public void setFloat1(String name, FloatBuffer buffer) {
        setFloat1(name, buffer, false);
    }

    @Override
    public void setFloat2(String name, float f1, float f2) {
        setFloat2(name, f1, f2, false);
    }

    @Override
    public void setFloat2(String name, Tuple2f value) {
        setFloat2(name, value.x, value.y);
    }

    @Override
    public void setFloat2(String name, Tuple2f value, boolean currentOnly) {
        setFloat2(name, value.x, value.y, currentOnly);
    }

    @Override
    public void setFloat2(String name, FloatBuffer buffer) {
        setFloat2(name, buffer, false);
    }

    @Override
    public void setFloat3(String name, float f1, float f2, float f3) {
        setFloat3(name, f1, f2, f3, false);
    }

    @Override
    public void setFloat3(String name, Tuple3f value) {
        setFloat3(name, value.x, value.y, value.z);
    }

    public void setFloat3(String name, Tuple3f value, boolean currentOnly) {
        setFloat3(name, value.x, value.y, value.z, currentOnly);
    }

    @Override
    public void setFloat3(String name, FloatBuffer buffer) {
        setFloat3(name, buffer, false);
    }

    @Override
    public void setFloat4(String name, float f1, float f2, float f3, float f4) {
        setFloat4(name, f1, f2, f3, f4, false);
    }

    @Override
    public void setFloat4(String name, Tuple4f value) {
        setFloat4(name, value.x, value.y, value.z, value.w);
    }

    @Override
    public void setFloat4(String name, Tuple4f value, boolean currentOnly) {
        setFloat4(name, value.x, value.y, value.z, value.w, currentOnly);
    }

    @Override
    public void setFloat4(String name, FloatBuffer buffer) {
        setFloat4(name, buffer, false);
    }

    @Override
    public void setInt(String name, int i) {
        setInt(name, i, false);
    }

    @Override
    public void setBoolean(String name, boolean value) {
        setBoolean(name, value, false);
    }

    @Override
    public void setMatrix3(String name, Matrix3f matrix) {
        setMatrix3(name, matrix, false);
    }

    @Override
    public void setMatrix3(String name, FloatBuffer buffer) {
        setMatrix3(name, buffer, false);
    }

    @Override
    public void setMatrix4(String name, Matrix4f matrix) {
        setMatrix4(name, matrix, false);
    }

    @Override
    public void setMatrix4(String name, FloatBuffer buffer) {
        setMatrix3(name, buffer, false);
    }

    @Override
    public void setCamera(Camera camera) {
        setMatrix4("viewMatrix", camera.getViewMatrix());
        setMatrix4("projMatrix", camera.getProjectionMatrix());
        setMatrix4("viewProjMatrix", camera.getViewProjectionMatrix());
        setMatrix4("invProjMatrix", camera.getInverseProjectionMatrix());
    }
}
