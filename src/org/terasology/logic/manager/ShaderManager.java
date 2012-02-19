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

    private final HashMap<String, ShaderProgram> _shaderPrograms = new HashMap<String, ShaderProgram>(32);

    private static ShaderManager _instance = null;

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
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
        createShader("highp", null);
        createShader("blur", null);
        createShader("down", null);
        createShader("post", new ShaderParametersPostProcessing());
        createShader("sky", null);
        createShader("chunk", new ShaderParametersChunk());
        createShader("particle", new ShaderParametersParticle());
        createShader("block", new ShaderParametersBlock());
        createShader("gelatinousCube", new ShaderParametersGelCube());
        createShader("clouds", null);
    }

    private void createShader(String title, ShaderParameters params) {
        _shaderPrograms.put(title, new ShaderProgram(title, params));
    }

    /**
     * @param s Name of the shader to activate
     */
    public void enableShader(String s) {
        if (s == null) {
            GL20.glUseProgram(0);
            return;
        }

        ShaderProgram program = getShaderProgram(s);
        program.enable();
    }

    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public ShaderProgram getShaderProgram(String s) {
        return _shaderPrograms.get(s);
    }
}
