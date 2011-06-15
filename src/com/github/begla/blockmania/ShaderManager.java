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
package com.github.begla.blockmania;

import com.github.begla.blockmania.utilities.Helper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderManager {

    private HashMap<String, Integer> _shaderPrograms = new HashMap<String, Integer>();
    private HashMap<String, Integer> _fragmentShader = new HashMap<String, Integer>();
    private HashMap<String, Integer> _vertexShader = new HashMap<String, Integer>();
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

    /**
     * 
     */
    public ShaderManager() {
        initShader();

        Helper.LOGGER.log(Level.INFO, "Loading Blockmania shader manager...");
        Helper.LOGGER.log(Level.INFO, "GL_VERSION: {0}", GL11.glGetString(GL11.GL_VERSION));
        Helper.LOGGER.log(Level.INFO, "SHADING_LANGUAGE VERSION: {0}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        Helper.LOGGER.log(Level.INFO, "EXTENSIONS: {0}", GL11.glGetString(GL11.GL_EXTENSIONS));
    }

    /**
     * 
     */
    private void initShader() {
        createVertexShader("chunk_vert.glsl", "chunk");
        createFragShader("chunk_frag.glsl", "chunk");
        createVertexShader("cloud_vert.glsl", "cloud");
        createFragShader("cloud_frag.glsl", "cloud");

        for (String s : _fragmentShader.keySet()) {
            int shaderProgram = GL20.glCreateProgram();

            GL20.glAttachShader(shaderProgram, _fragmentShader.get(s));
            GL20.glAttachShader(shaderProgram, _vertexShader.get(s));
            GL20.glLinkProgram(shaderProgram);
            GL20.glValidateProgram(shaderProgram);

            _shaderPrograms.put(s, shaderProgram);
        }
    }

    private int createFragShader(String filename, String title) {

        _fragmentShader.put(title, GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));

        if (_fragmentShader.get(title) == 0) {
            return 0;
        }

        String fragCode = "";
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("com/github/begla/blockmania/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                fragCode += line + "\n";
            }
        } catch (Exception e) {
            Helper.LOGGER.log(Level.SEVERE, "Failed reading fragment shading code.");
            return 0;
        }

        GL20.glShaderSource(_fragmentShader.get(title), fragCode);
        GL20.glCompileShader(_fragmentShader.get(title));

        printLogInfo(_fragmentShader.get(title));

        return _fragmentShader.get(title);
    }

    private int createVertexShader(String filename, String title) {

        _vertexShader.put(title, GL20.glCreateShader(GL20.GL_VERTEX_SHADER));

        if (_vertexShader.get(title) == 0) {
            return 0;
        }

        String fragCode = "";
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("com/github/begla/blockmania/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                fragCode += line + "\n";
            }
        } catch (Exception e) {
            Helper.LOGGER.log(Level.SEVERE, "Failed reading vertex shading code.");
            return 0;
        }

        GL20.glShaderSource(_vertexShader.get(title), fragCode);
        GL20.glCompileShader(_vertexShader.get(title));

        printLogInfo(_vertexShader.get(title));

        return _vertexShader.get(title);
    }

    private static void printLogInfo(int obj) {
        String output = GL20.glGetShaderInfoLog(obj, 1024);

        if (output.length() > 0) {
            Helper.LOGGER.log(Level.INFO, "{0}", output);
        }
    }

    /**
     * 
     * @param s
     */
    public void enableShader(String s) {
        if (s == null) {
            GL20.glUseProgram(0);
            return;
        }

        int shader = getShader(s);
        GL20.glUseProgram(shader);
    }

    /**
     * 
     * @param s
     * @return
     */
    public int getShader(String s) {
        return _shaderPrograms.get(s);
    }
}
