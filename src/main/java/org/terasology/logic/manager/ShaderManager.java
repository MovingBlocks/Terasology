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
package org.terasology.logic.manager;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.shader.*;

/**
 * Provides support for loading and applying shaders.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderManager {

    private static final Logger logger = LoggerFactory.getLogger(ShaderManager.class);
    private static ShaderManager instance = null;

    private Material activeMaterial = null;
    private GLSLShaderProgramInstance activeShaderProgram = null;

    private GLSLShaderProgramInstance defaultShaderProgram, defaultTexturedShaderProgram;
    private final HashMap<String, GLSLShaderProgramInstance> shaderPrograms = new HashMap<String, GLSLShaderProgramInstance>(16);

    public static ShaderManager getInstance() {
        if (instance == null) {
            instance = new ShaderManager();
        }

        return instance;
    }

    private ShaderManager() {
        logger.info("Loading Terasology shader manager...");
        logger.info("GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("SHADING_LANGUAGE VERSION: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        logger.info("EXTENSIONS: {}", GL11.glGetString(GL11.GL_EXTENSIONS));

        initShaders();
    }

    private void initShaders() {
        defaultShaderProgram = prepareAndStoreShaderProgramInstance("default", new ShaderParametersDefault());
        defaultTexturedShaderProgram = prepareAndStoreShaderProgramInstance("defaultTextured", new ShaderParametersDefault());

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

    public void enableMaterial(Material material) {
        if (material.isDisposed()) {
            // TODO: Fallback on default material
            return;
        }

        if (!material.equals(activeMaterial)) {
            material.getShaderProgramInstance().enable();
            activeMaterial = material;
            activeShaderProgram = null;
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

    public Material getActiveMaterial() {
        return activeMaterial;
    }

    public void recompileAllShaders() {
        for (GLSLShaderProgramInstance program : shaderPrograms.values()) {
            program.recompile();
        }

        activeMaterial = null;
        activeShaderProgram = null;
    }

    private GLSLShaderProgramInstance prepareAndStoreShaderProgramInstance(String title, IShaderParameters params) {
        // Make sure to remove the old shader program
        if (shaderPrograms.containsKey(title)) {
            shaderPrograms.remove(title).dispose();
        }

        GLSLShaderProgramInstance program = Assets.getShader("engine:" + title).createShaderProgramInstance(params);
        shaderPrograms.put(title, program);

        return program;
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

    /**
     * @param s Name of the shader to activate
     */
    public void enableShader(String s) {
        GLSLShaderProgramInstance program = getShaderProgramInstance(s);
        program.enable();
    }

    public void disableShader() {
        GL20.glUseProgram(0);
    }

    public GLSLShaderProgramInstance getActiveShaderProgram() {
        return activeShaderProgram;
    }

    public void setActiveShaderProgram(GLSLShaderProgramInstance program) {
        activeShaderProgram = program;
        activeMaterial = null;
    }
    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public GLSLShaderProgramInstance getShaderProgramInstance(String s) {
        return shaderPrograms.get(s);
    }

    public HashMap<String, GLSLShaderProgramInstance> getShaderPrograms() {
        return shaderPrograms;
    }
}
