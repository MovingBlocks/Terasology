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
package org.terasology.rendering.assets.material;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;

import java.nio.FloatBuffer;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

public abstract class BaseMaterial extends AbstractAsset<MaterialData> implements Material {

    public BaseMaterial(AssetUri uri) {
        super(uri);
    }

    protected abstract void onReload(MaterialData data);

    protected abstract void onDispose();

    public abstract void recompile();

    public abstract void enable();

    public abstract void setFloat(StorageQualifier qualifier, String name, float f, boolean currentOnly);

    public abstract void setFloat1(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat2(StorageQualifier qualifier, String name, float f1, float f2, boolean currentOnly);

    public abstract void setFloat2(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat3(StorageQualifier qualifier, String name, float f1, float f2, float f3, boolean currentOnly);

    public abstract void setFloat3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setFloat4(StorageQualifier qualifier, String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    public abstract void setFloat4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setInt(StorageQualifier qualifier, String name, int i, boolean currentOnly);

    public abstract void setBoolean(StorageQualifier qualifier, String name, boolean value, boolean currentOnly);

    public abstract void setMatrix3(StorageQualifier qualifier, String name, Matrix3f matrix, boolean currentOnly);

    public abstract void setMatrix3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setMatrix4(StorageQualifier qualifier, String name, Matrix4f matrix, boolean currentOnly);

    public abstract void setMatrix4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    public abstract void setTexture(String name, Texture texture);

    public abstract boolean supportsFeature(ShaderProgramFeature feature);

    public abstract void activateFeature(ShaderProgramFeature feature);

    public abstract void deactivateFeature(ShaderProgramFeature feature);

    public abstract void deactivateFeatures(ShaderProgramFeature... features);

    public abstract void bindTextures();

    @Override
    public void setFloat(StorageQualifier qualifier, String name, float f) {
        setFloat(qualifier, name, f, false);
    }

    @Override
    public void setFloat1(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setFloat1(qualifier, name, buffer, false);
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, float f1, float f2) {
        setFloat2(qualifier, name, f1, f2, false);
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, Vector2f value) {
        setFloat2(qualifier, name, value.x, value.y);
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, Vector2f value, boolean currentOnly) {
        setFloat2(qualifier, name, value.x, value.y, currentOnly);
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setFloat2(qualifier, name, buffer, false);
    }

    @Override
    public void setFloat3(StorageQualifier qualifier, String name, float f1, float f2, float f3) {
        setFloat3(qualifier, name, f1, f2, f3, false);
    }

    @Override
    public void setFloat3(StorageQualifier qualifier, String name, Vector3f value) {
        setFloat3(qualifier, name, value.x, value.y, value.z);
    }

    public void setFloat3(StorageQualifier qualifier, String name, Vector3f value, boolean currentOnly) {
        setFloat3(qualifier, name, value.x, value.y, value.z, currentOnly);
    }

    @Override
    public void setFloat3(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setFloat3(qualifier, name, buffer, false);
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, float f1, float f2, float f3, float f4) {
        setFloat4(qualifier, name, f1, f2, f3, f4, false);
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, Vector4f value) {
        setFloat4(qualifier, name, value.x, value.y, value.z, value.w);
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, Vector4f value, boolean currentOnly) {
        setFloat4(qualifier, name, value.x, value.y, value.z, value.w, currentOnly);
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setFloat4(qualifier, name, buffer, false);
    }

    @Override
    public void setInt(StorageQualifier qualifier, String name, int i) {
        setInt(qualifier, name, i, false);
    }

    @Override
    public void setBoolean(StorageQualifier qualifier, String name, boolean value) {
        setBoolean(qualifier, name, value, false);
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String name, Matrix3f matrix) {
        setMatrix3(qualifier, name, matrix, false);
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setMatrix3(qualifier, name, buffer, false);
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String name, Matrix4f matrix) {
        setMatrix4(qualifier, name, matrix, false);
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String name, FloatBuffer buffer) {
        setMatrix3(qualifier, name, buffer, false);
    }

    @Override
    public void setCamera(Camera camera) {
        setMatrix4(UNIFORM, "viewMatrix", camera.getViewMatrix());
        setMatrix4(UNIFORM, "projMatrix", camera.getProjectionMatrix());
        setMatrix4(UNIFORM, "viewProjMatrix", camera.getViewProjectionMatrix());
        setMatrix4(UNIFORM, "invProjMatrix", camera.getInverseProjectionMatrix());
    }
}
