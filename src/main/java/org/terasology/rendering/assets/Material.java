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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.metadata.ParamMetadata;

/**
 *
 * @author Immortius
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Material implements Asset {

    private static final Logger logger = LoggerFactory.getLogger(Material.class);

    private final AssetUri uri;

    private GLSLShaderProgram shader;
    private int textureIndex = 0;
    private TObjectIntMap<String> bindMap = new TObjectIntHashMap<String>();
    private TIntObjectMap<Texture> textureMap = new TIntObjectHashMap<Texture>();

    public Material(AssetUri uri, GLSLShaderProgram shader) {
        this.uri = uri;
        this.shader = shader;
    }

    public void dispose() {
        logger.debug("Disposing material {}.", uri);
        shader.dispose();
    }

    public boolean isDisposed() {
        return shader == null || shader.isDisposed();
    }

    public GLSLShaderProgram getShaderProgram() {
        return shader;
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

    public void setTexture(String desc, Texture texture) {
        if (isDisposed()) return;

        int texId = -1;
        if (bindMap.containsKey(desc)) {
            texId = bindMap.get(desc);
        } else {
            // TODO: do this initially, and try and have similar textures in similar slots for all materials.
            ParamMetadata metadata = shader.getParameter(desc);
            if (metadata == null) {
                return;
            }
            enable();
            texId = textureIndex++;

            // Make sure to bind the texture for all permutations
            shader.setIntForAllPermutations(desc, texId);

            bindMap.put(desc, texId);
        }

        textureMap.put(texId, texture);
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

}
