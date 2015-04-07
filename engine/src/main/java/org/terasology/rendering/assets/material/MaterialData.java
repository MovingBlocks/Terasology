/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.Maps;
import org.terasology.asset.AssetData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * @author Immortius
 */
public class MaterialData implements AssetData {

    private Shader shader;
    private Map<String, Texture> textures = Maps.newHashMap();

    private Map<String, Float> floatUniforms = Maps.newHashMap();
    private Map<String, float[]> floatArrayUniforms = Maps.newHashMap();
    private Map<String, Integer> intUniforms = Maps.newHashMap();

    private Map<String, Float> floatAttributes = Maps.newHashMap();
    private Map<String, float[]> floatArrayAttributes = Maps.newHashMap();


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

    public Map<String, Float> getFloatUniforms() {
        return floatUniforms;
    }

    public Map<String, Float> getFloatAttributes() {
        return floatAttributes;
    }

    public void setParam(Material.StorageQualifier qualifier, String parmName, float value) {
        switch (qualifier) {
            case UNIFORM:
                this.floatUniforms.put(parmName, value);
                break;
            case ATTRIBUTE:
                this.floatAttributes.put(parmName, value);
                break;
            default:
                throw noSwitchCaseImplemented(qualifier);
        }
    }

    public Map<String, float[]> getFloatArrayUniforms() {
        return floatArrayUniforms;
    }

    public Map<String, float[]> getFloatArrayAttributes() {
        return floatArrayAttributes;
    }

    public void setParam(Material.StorageQualifier qualifier, String parmName, float[] value) {
        switch (qualifier) {
            case UNIFORM:
                this.floatArrayUniforms.put(parmName, value);
                break;
            case ATTRIBUTE:
                this.floatArrayAttributes.put(parmName, value);
                break;
            default:
                throw noSwitchCaseImplemented(qualifier);
        }
    }

    public Map<String, Integer> getIntegerUniforms() {
        return intUniforms;
    }

    public void setParam(Material.StorageQualifier qualifier, String parmName, int value) {
        switch (qualifier) {
            case UNIFORM:
                this.intUniforms.put(parmName, value);
                break;
            case ATTRIBUTE:
                throw new UnsupportedOperationException("Int attributes are not supported in GLSL 1.2");
            default:
                throw noSwitchCaseImplemented(qualifier);
        }
    }

    public void setParam(Material.StorageQualifier qualifier, String parmName, boolean value) {
        final int intValue = (value) ? 1 : 0;

        switch (qualifier) {
            case UNIFORM:
                this.intUniforms.put(parmName, intValue);
                break;
            case ATTRIBUTE:
                throw new UnsupportedOperationException("Int attributes are not supported in GLSL 1.2");
            default:
                throw noSwitchCaseImplemented(qualifier);
        }
    }

    public void setTextureParams(Map<String, Texture> newTextureParmas) {
        this.textures.clear();
        this.textures.putAll(newTextureParmas);
    }

    public void setFloatUniforms(Map<String, Float> floatParams) {
        this.floatUniforms.clear();
        this.floatUniforms.putAll(floatParams);
    }

    public void setFloatArrayUniforms(Map<String, float[]> floatArrayParams) {
        this.floatArrayUniforms.clear();
        this.floatArrayUniforms.putAll(floatArrayParams);
    }

    public void setIntUniforms(Map<String, Integer> intParams) {
        this.intUniforms.clear();
        this.intUniforms.putAll(intParams);
    }

    public void setFloatAttributes(Map<String, Float> floatParams) {
        this.floatAttributes.clear();
        this.floatAttributes.putAll(floatParams);
    }

    public void setFloatArrayAttributes(Map<String, float[]> floatArrayParams) {
        this.floatArrayAttributes.clear();
        this.floatArrayAttributes.putAll(floatArrayParams);
    }

    private static UnsupportedOperationException noSwitchCaseImplemented(Material.StorageQualifier qualifier) {
        return new UnsupportedOperationException(
                "Switch statement does not have a case in for " + qualifier
        );
    }
}
