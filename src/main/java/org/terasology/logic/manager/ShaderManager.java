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
import org.terasology.rendering.assets.GLSLShaderProgram;
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

    private final HashMap<String, GLSLShaderProgram> shaderPrograms = new HashMap<String, GLSLShaderProgram>(16);

    private int activeFeatures = 0;
    private Material activateMaterial = null;
    private GLSLShaderProgram activeShaderProgram = null;

    private GLSLShaderProgram defaultShaderProgram, defaultTexturedShaderProgram;

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
        defaultShaderProgram = createAndStoreShaderProgram("default", new ShaderParametersDefault());
        defaultTexturedShaderProgram = createAndStoreShaderProgram("defaultTextured", new ShaderParametersDefault());

        // TODO: Port this to the asset pipeline
        createAndStoreShaderProgram("post", new ShaderParametersPost());
        createAndStoreShaderProgram("ssao", new ShaderParametersSSAO());
        createAndStoreShaderProgram("lightshaft", new ShaderParametersLightShaft());
        createAndStoreShaderProgram("sobel", new ShaderParametersSobel());
        createAndStoreShaderProgram("prePost", new ShaderParametersPrePost());
        createAndStoreShaderProgram("combine", new ShaderParametersCombine());
        createAndStoreShaderProgram("highp", new ShaderParametersDefault());
        createAndStoreShaderProgram("blur", new ShaderParametersDefault());
        createAndStoreShaderProgram("down", new ShaderParametersDefault());
        createAndStoreShaderProgram("hdr", new ShaderParametersHdr());
        createAndStoreShaderProgram("sky", new ShaderParametersSky());
        createAndStoreShaderProgram("chunk", new ShaderParametersChunk());
        createAndStoreShaderProgram("particle", new ShaderParametersParticle());
        createAndStoreShaderProgram("block", new ShaderParametersBlock());
        createAndStoreShaderProgram("gelatinousCube", new ShaderParametersGelCube());
        createAndStoreShaderProgram("animateOpacity", new ShaderParametersDefault());
        createAndStoreShaderProgram("shadowMap", new ShaderParametersShadowMap());
        createAndStoreShaderProgram("debug", new ShaderParametersDebug());
        createAndStoreShaderProgram("ocDistortion", new ShaderParametersOcDistortion());
        createAndStoreShaderProgram("lightBufferPass", new ShaderParametersLightBufferPass());
        createAndStoreShaderProgram("lightGeometryPass", new ShaderParametersLightGeometryPass());
    }

    public void enableMaterial(Material material) {
        if (material.isDisposed()) {
            // TODO: Fallback on default material
            return;
        }

        if (!material.equals(activateMaterial)) {
            material.getShaderProgram().enable();
            activateMaterial = material;
            activeShaderProgram = null;
        }
    }

    public void bindTexture(int slot, Texture texture) {
        if (activateMaterial != null && !activateMaterial.isDisposed()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
            // TODO: Need to be cubemap aware, only need to clear bind when switching from cubemap to 2D and vice versa,
            // TODO: Don't bind if already bound to the same
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }
    }

    public Material getActiveMaterial() {
        return activateMaterial;
    }

    public void recompileAllShaders() {
        for (GLSLShaderProgram program : shaderPrograms.values()) {
            program.recompile();
        }
    }

    private GLSLShaderProgram createAndStoreShaderProgram(String title, IShaderParameters params) {
        // Make sure to remove the old shader program
        if (shaderPrograms.containsKey(title)) {
            shaderPrograms.remove(title).dispose();
        }

        GLSLShaderProgram program = new GLSLShaderProgram(title, params);
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
        GLSLShaderProgram program = getShaderProgram(s);
        program.enable();
    }

    public void disableShader() {
        GL20.glUseProgram(0);
    }

    public GLSLShaderProgram getActiveShaderProgram() {
        return activeShaderProgram;
    }

    public int getActiveFeatures() {
        return activeFeatures;
    }

    public void setActiveShaderProgram(GLSLShaderProgram program) {
        activeShaderProgram = program;
        activateMaterial = null;
        activeFeatures = program.getActiveFeatures();
    }
    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public GLSLShaderProgram getShaderProgram(String s) {
        return shaderPrograms.get(s);
    }

    public HashMap<String, GLSLShaderProgram> getShaderPrograms() {
        return shaderPrograms;
    }
}
