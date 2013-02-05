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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static ShaderManager _instance = null;

    private final HashMap<String, ShaderProgram> _shaderPrograms = new HashMap<String, ShaderProgram>(16);

    private ShaderProgram _activeShaderProgram = null;

    private ShaderProgram _defaultShaderProgram, _defaultTexturedShaderProgram;

    private Material activateMaterial = null;

    private TIntObjectMap<Texture> boundTextures = new TIntObjectHashMap<Texture>();

    public static ShaderManager getInstance() {
        if (_instance == null) {
            _instance = new ShaderManager();
        }

        return _instance;
    }

    private ShaderManager() {
        logger.info("Loading Terasology shader manager...");
        logger.info("GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("SHADING_LANGUAGE VERSION: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        logger.info("EXTENSIONS: {}", GL11.glGetString(GL11.GL_EXTENSIONS));

        initShaders();
    }

    private void initShaders() {
        _defaultShaderProgram = createAndStoreShaderProgram("default", new ShaderParametersDefault());
        _defaultTexturedShaderProgram = createAndStoreShaderProgram("defaultTextured", new ShaderParametersDefault());

        createAndStoreShaderProgram("post", new ShaderParametersPost());
        createAndStoreShaderProgram("ssao", new ShaderParametersSSAO());
        createAndStoreShaderProgram("sobel", new ShaderParametersSobel());
        createAndStoreShaderProgram("prePost", new ShaderParametersPrePost());
        createAndStoreShaderProgram("highp", new ShaderParametersDefault());
        createAndStoreShaderProgram("blur", new ShaderParametersDefault());
        createAndStoreShaderProgram("down", new ShaderParametersDefault());
        createAndStoreShaderProgram("hdr", new ShaderParametersHdr());
        createAndStoreShaderProgram("sky", new ShaderParametersSky());
        createAndStoreShaderProgram("chunk", new ShaderParametersChunk());
        createAndStoreShaderProgram("particle", new ShaderParametersParticle());
        createAndStoreShaderProgram("block", new ShaderParametersBlock());
        createAndStoreShaderProgram("gelatinousCube", new ShaderParametersGelCube());
        createAndStoreShaderProgram("clouds", new ShaderParametersDefault());
        createAndStoreShaderProgram("animateOpacity", new ShaderParametersDefault());
        //createAndStoreShaderProgram("genericMesh", new ShaderParametersGenericMesh());
    }

    public void enableMaterial(Material material) {
        if (material.isDisposed()) {
            // TODO: Fallback on default material
            return;
        }

        if (!material.equals(activateMaterial)) {
            GL20.glUseProgram(material.getShaderId());
            activateMaterial = material;
            _activeShaderProgram = null;
        }
    }

    public void bindTexture(int slot, Texture texture) {
        if (activateMaterial != null && !activateMaterial.isDisposed()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
            // TODO: Need to be cubemap aware, only need to clear bind when switching from cubemap to 2D and vice versa,
            // TODO: Don't bind if already bound to the same
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }
    }

    public Material getActiveMaterial() {
        return activateMaterial;
    }

    public void recompileAllShaders() {
        for (ShaderProgram program : _shaderPrograms.values()) {
            program.recompile();
        }
    }

    private ShaderProgram createAndStoreShaderProgram(String title, IShaderParameters params) {
        // Make sure to remove the old shader program
        if (_shaderPrograms.containsKey(title)) {
            _shaderPrograms.remove(title).dispose();
        }

        ShaderProgram program = new ShaderProgram(title, params);
        _shaderPrograms.put(title, program);
        return program;
    }

    /**
     * Enables the default shader program.
     */
    public void enableDefault() {
        _defaultShaderProgram.enable();
    }

    /**
     * Enables the default shader program.
     */
    public void enableDefaultTextured() {
        _defaultTexturedShaderProgram.enable();
    }

    /**
     * @param s Name of the shader to activate
     */
    public void enableShader(String s) {
        ShaderProgram program = getShaderProgram(s);
        program.enable();
    }

    public ShaderProgram getActiveShaderProgram() {
        return _activeShaderProgram;
    }

    public void setActiveShaderProgram(ShaderProgram program) {
        _activeShaderProgram = program;
        activateMaterial = null;
    }

    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public ShaderProgram getShaderProgram(String s) {
        return _shaderPrograms.get(s);
    }

    /**
     *
     * @return
     */
    public HashMap<String, ShaderProgram> getShaderPrograms() {
        return _shaderPrograms;
    }
}
