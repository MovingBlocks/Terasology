/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.terasology.game.Terasology;
import org.terasology.rendering.shader.*;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Provides support for loading and applying shaders.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderManager {

    private static ShaderManager _instance = null;

    private final HashMap<String, ShaderProgram> _shaderPrograms = new HashMap<String, ShaderProgram>(16);

    private ShaderProgram _activeShaderProgram = null;

    private ShaderProgram _defaultShaderProgram, _defaultTexturedShaderProgram;

    public static ShaderManager getInstance() {
        if (_instance == null) {
            _instance = new ShaderManager();
        }

        return _instance;
    }

    private ShaderManager() {
        Terasology.getInstance().getLogger().log(Level.INFO, "Loading Terasology shader manager...");
        Terasology.getInstance().getLogger().log(Level.INFO, "GL_VERSION: {0}", GL11.glGetString(GL11.GL_VERSION));
        Terasology.getInstance().getLogger().log(Level.INFO, "SHADING_LANGUAGE VERSION: {0}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        Terasology.getInstance().getLogger().log(Level.INFO, "EXTENSIONS: {0}", GL11.glGetString(GL11.GL_EXTENSIONS));

        initShaders();
    }

    private void initShaders() {
        _defaultShaderProgram = createAndStoreShaderProgram("default", new ShaderParametersDefault());
        _defaultTexturedShaderProgram = createAndStoreShaderProgram("defaultTextured", new ShaderParametersDefault());

        createAndStoreShaderProgram("post", new ShaderParametersPost());
        createAndStoreShaderProgram("highp", new ShaderParametersDefault());
        createAndStoreShaderProgram("blur", new ShaderParametersDefault());
        createAndStoreShaderProgram("down", new ShaderParametersDefault());
        createAndStoreShaderProgram("hdr", new ShaderParametersHdr());
        createAndStoreShaderProgram("sky", new ShaderParametersDefault());
        createAndStoreShaderProgram("chunk", new ShaderParametersChunk());
        createAndStoreShaderProgram("particle", new ShaderParametersParticle());
        createAndStoreShaderProgram("block", new ShaderParametersBlock());
        createAndStoreShaderProgram("gelatinousCube", new ShaderParametersGelCube());
        createAndStoreShaderProgram("clouds", new ShaderParametersDefault());
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
    }

    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public ShaderProgram getShaderProgram(String s) {
        return _shaderPrograms.get(s);
    }
}
