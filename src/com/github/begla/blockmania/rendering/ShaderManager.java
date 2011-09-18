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
package com.github.begla.blockmania.rendering;

import com.github.begla.blockmania.main.Blockmania;
import javolution.util.FastMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.util.ResourceLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

/**
 * Provides support for loading and applying shaders.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderManager {

    private final FastMap<String, Integer> _shaderPrograms = new FastMap<String, Integer>(32);
    private final FastMap<String, Integer> _fragmentShader = new FastMap<String, Integer>(32);
    private final FastMap<String, Integer> _vertexShader = new FastMap<String, Integer>(32);
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
        initShader();

        Blockmania.getInstance().getLogger().log(Level.INFO, "Loading Blockmania shader manager...");
        Blockmania.getInstance().getLogger().log(Level.INFO, "GL_VERSION: {0}", GL11.glGetString(GL11.GL_VERSION));
        Blockmania.getInstance().getLogger().log(Level.INFO, "SHADING_LANGUAGE VERSION: {0}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        Blockmania.getInstance().getLogger().log(Level.INFO, "EXTENSIONS: {0}", GL11.glGetString(GL11.GL_EXTENSIONS));
    }

    private void initShader() {
        createVertexShader("sky_vert.glsl", "sky");
        createFragShader("sky_frag.glsl", "sky");
        createVertexShader("chunk_vert.glsl", "chunk");
        createFragShader("chunk_frag.glsl", "chunk");
        createVertexShader("cloud_vert.glsl", "cloud");
        createFragShader("cloud_frag.glsl", "cloud");

        for (FastMap.Entry<String, Integer> e = _fragmentShader.head(), end = _fragmentShader.tail(); (e = e.getNext()) != end; ) {
            int shaderProgram = GL20.glCreateProgram();

            GL20.glAttachShader(shaderProgram, _fragmentShader.get(e.getKey()));
            GL20.glAttachShader(shaderProgram, _vertexShader.get(e.getKey()));
            GL20.glLinkProgram(shaderProgram);
            GL20.glValidateProgram(shaderProgram);

            _shaderPrograms.put(e.getKey(), shaderProgram);
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("com/github/begla/blockmania/data/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                fragCode += line + "\n";
            }
        } catch (Exception e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to read fragment shader.");
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("com/github/begla/blockmania/data/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                fragCode += line + "\n";
            }
        } catch (Exception e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to read vertex shader.");
            return 0;
        }

        GL20.glShaderSource(_vertexShader.get(title), fragCode);
        GL20.glCompileShader(_vertexShader.get(title));

        printLogInfo(_vertexShader.get(title));

        return _vertexShader.get(title);
    }

    private static void printLogInfo(int obj) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        GL20.glGetShader(obj, GL20.GL_INFO_LOG_LENGTH, intBuffer);

        int length = intBuffer.get();

        if (length <= 1) {
            return;
        }

        ByteBuffer infoBuffer = BufferUtils.createByteBuffer(length);
        intBuffer.flip();

        GL20.glGetShaderInfoLog(obj, intBuffer, infoBuffer);

        int actualLength = intBuffer.get();
        byte[] infoBytes = new byte[actualLength];
        infoBuffer.get(infoBytes);

        Blockmania.getInstance().getLogger().log(Level.INFO, "{0}", new String(infoBytes));
    }

    /**
     * @param s Name of the shader to activate
     */
    public void enableShader(@org.jetbrains.annotations.Nullable String s) {
        if (s == null) {
            GL20.glUseProgram(0);
            return;
        }

        int shader = getShader(s);
        GL20.glUseProgram(shader);
    }

    /**
     * @param s Nave of the shader to return
     * @return The id of the requested shader
     */
    public int getShader(String s) {
        return _shaderPrograms.get(s);
    }
}
