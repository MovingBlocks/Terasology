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
package org.terasology.engine.subsystem.headless.assets;

import java.nio.FloatBuffer;

import org.terasology.asset.AssetUri;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.material.BaseMaterial;

public class HeadlessMaterial extends BaseMaterial {

    private MaterialData data;

    public HeadlessMaterial(AssetUri uri, MaterialData data) {
        super(uri);
        onReload(data);
    }

    @Override
    protected void onReload(MaterialData newData) {
        this.data = newData;
    }

    @Override
    protected void onDispose() {
        data = null;
    }

    @Override
    public void recompile() {
        // Do nothing
    }

    @Override
    public void enable() {
        // Do nothing
    }

    @Override
    public void setFloat(StorageQualifier qualifier, String name, float f, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat1(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, float f1, float f2, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(StorageQualifier qualifier, String name, float f1, float f2, float f3, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, float f1, float f2, float f3, float f4, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setInt(StorageQualifier qualifier, String name, int i, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setBoolean(StorageQualifier qualifier, String name, boolean value, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String name, Matrix3f matrix, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String name, Matrix4f matrix, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setTexture(String name, Texture texture) {
        // Do nothing
    }

    @Override
    public boolean supportsFeature(ShaderProgramFeature feature) {
        return false;
    }

    @Override
    public void activateFeature(ShaderProgramFeature feature) {
        // Do nothing
    }

    @Override
    public void deactivateFeature(ShaderProgramFeature feature) {
        // Do nothing
    }

    @Override
    public void deactivateFeatures(ShaderProgramFeature ... features) {
        // Do nothing
    }

    @Override
    public void bindTextures() {
        // Do nothing
    }
}
