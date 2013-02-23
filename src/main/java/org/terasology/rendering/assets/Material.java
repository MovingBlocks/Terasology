/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.assets;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.metadata.ParamMetadata;

/**
 * @author Immortius
 */
public class Material implements Asset {

    private static final Logger logger = LoggerFactory.getLogger(Material.class);

    private final AssetUri uri;

    private MaterialShader materialShader;
    private int shaderProgram;
    private int textureIndex = 0;
    private TObjectIntMap<String> bindMap = new TObjectIntHashMap<String>();
    private TIntObjectMap<Texture> textureMap = new TIntObjectHashMap<Texture>();

    public Material(AssetUri uri, MaterialShader materialShader) {
        this.uri = uri;
        this.materialShader = materialShader;
        shaderProgram = materialShader.generateShaderInstance();
    }

    public void dispose() {
        logger.debug("Disposing material {}.", uri);

        GL20.glDeleteShader(shaderProgram);
        shaderProgram = 0;
    }

    public boolean isDisposed() {
        return shaderProgram == 0;
    }

    public int getShaderId() {
        return shaderProgram;
    }

    public void enable() {
        if (isDisposed()) return;

        Material activeMaterial = ShaderManager.getInstance().getActiveMaterial();
        if (activeMaterial != this) {
            ShaderManager.getInstance().enableMaterial(this);
        }
    }

    public void bindTextures() {
        if (isDisposed()) return;

        enable();
        for (int slot : textureMap.keys()) {
            ShaderManager.getInstance().bindTexture(slot, textureMap.get(slot));
        }
    }

    public void setFloat(String desc, float f) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform1f(id, f);
        }
    }

    public void setFloat2(String desc, float f1, float f2) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform2f(id, f1, f2);
        }
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform3f(id, f1, f2, f3);
        }
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform4f(id, f1, f2, f3, f4);
        }
    }

    public void setInt(String desc, int i) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform1i(id, i);
        }
    }

    public void setFloat1(String desc, FloatBuffer buffer) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform1(id, buffer);
        }
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform2(id, buffer);
        }
    }

    public void setFloat3(String desc, FloatBuffer buffer) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform3(id, buffer);
        }
    }

    public void setFloat4(String desc, FloatBuffer buffer) {
        if (isDisposed()) return;

        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        if (id != -1) {
            GL20.glUniform4(id, buffer);
        }
    }

    public void setTexture(String desc, Texture texture) {
        if (isDisposed()) return;

        int texId = -1;
        if (bindMap.containsKey(desc)) {
            texId = bindMap.get(desc);
        } else {
            // TODO: do this initially, and try and have similar textures in similar slots for all materials.
            ParamMetadata metadata = materialShader.getParameter(desc);
            if (metadata == null) {
                return;
            }
            enable();
            texId = textureIndex++;
            int id = GL20.glGetUniformLocation(shaderProgram, desc);
            GL20.glUniform1i(id, texId);
            bindMap.put(desc, texId);
        }

        textureMap.put(texId, texture);
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

}
