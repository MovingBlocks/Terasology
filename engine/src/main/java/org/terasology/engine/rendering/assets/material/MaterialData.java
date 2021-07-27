// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.material;

import com.google.common.collect.Maps;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.assets.AssetData;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class MaterialData implements AssetData {
    private Shader shader;
    private Map<String, Texture> textures = Maps.newHashMap();
    private Map<String, Float> floatParams = Maps.newHashMap();
    private Map<String, float[]> floatArrayParams = Maps.newHashMap();
    private Map<String, Integer> intParams = Maps.newHashMap();

    public MaterialData(Shader shader) {
        checkNotNull(shader);
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }

    public Map<String, Texture> getTextures() {
        return textures;
    }

    public void setParam(String parmName, Texture value) {
        this.textures.put(parmName, value);
    }

    public Map<String, Float> getFloatParams() {
        return floatParams;
    }

    public void setParam(String parmName, float value) {
        this.floatParams.put(parmName, value);
    }

    public Map<String, float[]> getFloatArrayParams() {
        return floatArrayParams;
    }

    public void setParam(String parmName, float[] value) {
        this.floatArrayParams.put(parmName, value);
    }

    public Map<String, Integer> getIntegerParams() {
        return intParams;
    }

    public void setParam(String parmName, int value) {
        this.intParams.put(parmName, value);
    }

    public void setParam(String parmName, boolean value) {
        this.intParams.put(parmName, (value) ? 1 : 0);
    }


    public void setTextureParams(Map<String, Texture> newTextureParmas) {
        this.textures.clear();
        this.textures.putAll(newTextureParmas);
    }

    public void setFloatParams(Map<String, Float> floatParams) {
        this.floatParams.clear();
        this.floatParams.putAll(floatParams);
    }

    public void setFloatArrayParams(Map<String, float[]> floatArrayParams) {
        this.floatArrayParams.clear();
        this.floatArrayParams.putAll(floatArrayParams);
    }

    public void setIntParams(Map<String, Integer> intParams) {
        this.intParams.clear();
        this.intParams.putAll(intParams);
    }
}
