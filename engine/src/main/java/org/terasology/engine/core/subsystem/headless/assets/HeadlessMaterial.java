// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.terasology.engine.rendering.assets.material.BaseMaterial;
import org.terasology.engine.rendering.assets.material.MaterialData;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.nio.FloatBuffer;

public class HeadlessMaterial extends BaseMaterial {

    private MaterialData data;

    public HeadlessMaterial(ResourceUrn urn, AssetType<?, MaterialData> assetType, MaterialData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(MaterialData newData) {
        this.data = newData;
    }

    @Override
    public boolean isRenderable() {
        return true;
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
    public void setFloat(String name, float f, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat1(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(String name, float f1, float f2, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat2(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat3(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setFloat4(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setInt(String name, int i, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setBoolean(String name, boolean value, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix3(String name, Matrix3fc matrix, boolean currentOnly) {

    }

    @Override
    public void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly) {
        // Do nothing
    }

    @Override
    public void setMatrix4(String name, Matrix4fc matrix, boolean currentOnly) {

    }

    @Override
    public void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly) {
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
    public void deactivateFeatures(ShaderProgramFeature... features) {
        // Do nothing
    }

    @Override
    public void bindTextures() {
        // Do nothing
    }
}
