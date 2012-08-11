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
package org.terasology.rendering.shader;

import static org.lwjgl.opengl.GL11.glBindTexture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.assets.Shader;

/**
 * Wraps a OpenGL shader program. Provides convenience methods for setting
 * uniform variables of various types.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderProgram {

    private int _shaderProgram, _fragmentProgram, _vertexProgram;
    private String _title;

    private IShaderParameters _parameters;

    private Logger logger = Logger.getLogger(getClass().getName());

    public ShaderProgram(String title) {
        this(title, null);
    }

    public ShaderProgram(String title, IShaderParameters params) {
        _title = title;
        _parameters = params;

        compileShaderProgram();
    }

    private void compileShaderProgram() {
        compileShader(GL20.GL_FRAGMENT_SHADER);
        compileShader(GL20.GL_VERTEX_SHADER);

        _shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(_shaderProgram, _fragmentProgram);
        GL20.glAttachShader(_shaderProgram, _vertexProgram);
        GL20.glLinkProgram(_shaderProgram);
        GL20.glValidateProgram(_shaderProgram);
    }

    public void recompile() {
        logger.log(Level.INFO, "Recompiling shader {0}.", new String[]{_title});

        dispose();
        compileShaderProgram();
    }

    public void dispose() {
        logger.log(Level.INFO, "Disposing shader {0}.", new String[]{_title});

        GL20.glDeleteShader(_shaderProgram);
        _shaderProgram = 0;

        GL20.glDeleteProgram(_fragmentProgram);
        _fragmentProgram = 0;

        GL20.glDeleteProgram(_vertexProgram);
        _vertexProgram = 0;
    }

    private void compileShader(int type) {

        int shaderId = GL20.glCreateShader(type);

        StringBuilder shader = Shader.createShaderBuilder();

        if (type == GL20.GL_FRAGMENT_SHADER)
            shader.append(Shader.getIncludedFunctionsFragment()).append("\n");
        else
            shader.append(Shader.getIncludedFunctionsVertex()).append("\n");

        String filename = _title;

        if (type == GL20.GL_FRAGMENT_SHADER) {
            filename += "_frag.glsl";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            filename += "_vert.glsl";
        }

        logger.log(Level.INFO, "Loading shader {0} ({1}, type = {2})", new String[]{_title, filename, String.valueOf(type)});

        // Read in the shader code
        shader.append(readShader(filename));

        if (type == GL20.GL_FRAGMENT_SHADER) {
            _fragmentProgram = shaderId;
        } else if (type == GL20.GL_VERTEX_SHADER) {
            _vertexProgram = shaderId;
        }

        GL20.glShaderSource(shaderId, shader.toString());
        GL20.glCompileShader(shaderId);

        printLogInfo(shaderId);
    }

    private String readShader(String filename) {
        String line, code = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("org/terasology/data/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                code += line + "\n";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read shader.");
        }

        return code;
    }

    private void printLogInfo(int shaderId) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        GL20.glGetShader(shaderId, GL20.GL_INFO_LOG_LENGTH, intBuffer);

        int length = intBuffer.get();

        if (length <= 1) {
            return;
        }

        ByteBuffer infoBuffer = BufferUtils.createByteBuffer(length);
        intBuffer.flip();

        GL20.glGetShaderInfoLog(shaderId, intBuffer, infoBuffer);

        int actualLength = intBuffer.get();
        byte[] infoBytes = new byte[actualLength];
        infoBuffer.get(infoBytes);

        logger.log(Level.INFO, "{0}", new String(infoBytes));
    }

    public void enable() {
        ShaderProgram activeProgram = ShaderManager.getInstance().getActiveShaderProgram();

        if (activeProgram != this) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL11.GL_TEXTURE_2D, 0);

            GL20.glUseProgram(_shaderProgram);

            // Make sure the shader manager knows that this program is currently active
            ShaderManager.getInstance().setActiveShaderProgram(this);

            // Set the shader parameters if available
            if (_parameters != null) {
                _parameters.applyParameters(this);
            }
        }
    }

    public void setFloat(String desc, float f) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform1f(id, f);
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform3f(id, f1, f2, f3);
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform4f(id, f1, f2, f3, f4);
    }

    public void setInt(String desc, int i) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform1i(id, i);
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform2(id, buffer);
    }

    public void setFloat1(String desc, FloatBuffer buffer) {
        enable();
        int id = GL20.glGetUniformLocation(_shaderProgram, desc);
        GL20.glUniform1(id, buffer);
    }

    public IShaderParameters getShaderParameters() {
        return _parameters;
    }

    public int getShaderId() {
        return _shaderProgram;
    }
}
