/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.shader.ShaderParameters;
import org.terasology.rendering.shader.ShaderParametersBlock;
import org.terasology.rendering.shader.ShaderParametersChunk;
import org.terasology.rendering.shader.ShaderParametersCombine;
import org.terasology.rendering.shader.ShaderParametersDebug;
import org.terasology.rendering.shader.ShaderParametersDefault;
import org.terasology.rendering.shader.ShaderParametersGelCube;
import org.terasology.rendering.shader.ShaderParametersHdr;
import org.terasology.rendering.shader.ShaderParametersLightBufferPass;
import org.terasology.rendering.shader.ShaderParametersLightGeometryPass;
import org.terasology.rendering.shader.ShaderParametersLightShaft;
import org.terasology.rendering.shader.ShaderParametersOcDistortion;
import org.terasology.rendering.shader.ShaderParametersParticle;
import org.terasology.rendering.shader.ShaderParametersPost;
import org.terasology.rendering.shader.ShaderParametersPrePost;
import org.terasology.rendering.shader.ShaderParametersSSAO;
import org.terasology.rendering.shader.ShaderParametersShadowMap;
import org.terasology.rendering.shader.ShaderParametersSky;
import org.terasology.rendering.shader.ShaderParametersSobel;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides support for loading and applying shaders.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderManager {

    private static final Logger logger = LoggerFactory.getLogger(ShaderManager.class);

    private GLSLMaterial activeMaterial = null;
    private GLSLMaterial defaultShaderProgram;
    private GLSLMaterial defaultTexturedShaderProgram;

    public ShaderManager() {
        logger.info("Loading Terasology shader manager...");
        logger.info("GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("SHADING_LANGUAGE VERSION: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        logger.info("EXTENSIONS: {}", GL11.glGetString(GL11.GL_EXTENSIONS));
    }

    public void initShaders() {
        defaultShaderProgram = prepareAndStoreShaderProgramInstance("default", new ShaderParametersDefault());
        defaultTexturedShaderProgram = prepareAndStoreShaderProgramInstance("defaultTextured", new ShaderParametersDefault());

        // TODO: Find a better way to do this
        prepareAndStoreShaderProgramInstance("post", new ShaderParametersPost());
        prepareAndStoreShaderProgramInstance("ssao", new ShaderParametersSSAO());
        prepareAndStoreShaderProgramInstance("lightshaft", new ShaderParametersLightShaft());
        prepareAndStoreShaderProgramInstance("sobel", new ShaderParametersSobel());
        prepareAndStoreShaderProgramInstance("prePost", new ShaderParametersPrePost());
        prepareAndStoreShaderProgramInstance("combine", new ShaderParametersCombine());
        prepareAndStoreShaderProgramInstance("highp", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("blur", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("down", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("hdr", new ShaderParametersHdr());
        prepareAndStoreShaderProgramInstance("sky", new ShaderParametersSky());
        prepareAndStoreShaderProgramInstance("chunk", new ShaderParametersChunk());
        prepareAndStoreShaderProgramInstance("particle", new ShaderParametersParticle());
        prepareAndStoreShaderProgramInstance("block", new ShaderParametersBlock());
        prepareAndStoreShaderProgramInstance("gelatinousCube", new ShaderParametersGelCube());
        prepareAndStoreShaderProgramInstance("animateOpacity", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("shadowMap", new ShaderParametersShadowMap());
        prepareAndStoreShaderProgramInstance("debug", new ShaderParametersDebug());
        prepareAndStoreShaderProgramInstance("ocDistortion", new ShaderParametersOcDistortion());
        prepareAndStoreShaderProgramInstance("lightBufferPass", new ShaderParametersLightBufferPass());
        prepareAndStoreShaderProgramInstance("lightGeometryPass", new ShaderParametersLightGeometryPass());
        prepareAndStoreShaderProgramInstance("simple", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("ssaoBlur", new ShaderParametersDefault());
    }

    public void setActiveMaterial(GLSLMaterial material) {
        if (!material.equals(activeMaterial)) {
            activeMaterial = material;
        }
    }

    public void bindTexture(int slot, Texture texture) {
        if (activeMaterial != null && !activeMaterial.isDisposed()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
            // TODO: Need to be cubemap aware, only need to clear bind when switching from cubemap to 2D and vice versa,
            // TODO: Don't bind if already bound to the same
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }
    }

    public GLSLMaterial getActiveMaterial() {
        return activeMaterial;
    }

    public void recompileAllShaders() {
        for (Shader shader : AssetManager.getInstance().listLoadedAssets(AssetType.SHADER, Shader.class)) {
            shader.recompile();
        }

        activeMaterial = null;
    }

    private GLSLMaterial prepareAndStoreShaderProgramInstance(String title, ShaderParameters params) {
        String uri = "engine:" + title;
        Shader shader = Assets.getShader(uri);
        checkNotNull(shader, "Failed to resolve %s", uri);
        shader.recompile();
        GLSLMaterial material = Assets.generateAsset(new AssetUri(AssetType.MATERIAL, uri), new MaterialData(shader), GLSLMaterial.class);
        material.setShaderParameters(params);

        return material;
    }

    /**
     * Enables the default shader program.
     */
    public void enableDefault() {
        defaultShaderProgram.enable();
    }

    /**
     * Enables the default shader program.
     */
    public void enableDefaultTextured() {
        defaultTexturedShaderProgram.enable();
    }

    public void disableShader() {
        GL20.glUseProgram(0);
        activeMaterial = null;
    }

}
