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
package org.terasology.rendering.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.model.blocks.Block;

import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderProgram {

    private int _shaderPogram, _fragmentProgram, _vertexProgram;
    private String _title;

    private static String _preProcessorPreamble = "#version 120 \n float TEXTURE_OFFSET = " + Block.TEXTURE_OFFSET + "; \n";
    private static String _includedFunctionsVertex = "", _includedFunctionsFragment = "";

    private ShaderParameters _parameters;

    static {
        _preProcessorPreamble += ((Boolean) ConfigurationManager.getInstance().getConfig().get("Graphics.animatedWaterAndGrass")) ? "#define ANIMATED_WATER_AND_GRASS \n" : "";
        _preProcessorPreamble += "#define GAMMA " + ConfigurationManager.getInstance().getConfig().get("Graphics.gamma").toString() + "\n";
    }

    public ShaderProgram(String title) {
        this(title, null);
    }

    public ShaderProgram(String title, ShaderParameters params) {
        if (_includedFunctionsVertex.length() == 0 || _includedFunctionsFragment.length() == 0) {
            _includedFunctionsFragment += readShader("globalFunctionsFragIncl.glsl");
            _includedFunctionsVertex += readShader("globalFunctionsVertIncl.glsl");
        }

        _title = title;
        _parameters = params;

        createShader();
    }

    private void createShader() {
        createShader(GL20.GL_FRAGMENT_SHADER);
        createShader(GL20.GL_VERTEX_SHADER);

        _shaderPogram = GL20.glCreateProgram();

        GL20.glAttachShader(_shaderPogram, _fragmentProgram);
        GL20.glAttachShader(_shaderPogram, _vertexProgram);
        GL20.glLinkProgram(_shaderPogram);
        GL20.glValidateProgram(_shaderPogram);
    }

    private void createShader(int type) {

        int shaderId = GL20.glCreateShader(type);

        String code = _preProcessorPreamble + "\n";
        if (type == GL20.GL_FRAGMENT_SHADER)
            code += _includedFunctionsFragment + "\n";
        else
            code += _includedFunctionsVertex + "\n";

        String filename = _title;

        if (type == GL20.GL_FRAGMENT_SHADER) {
            filename += "_frag.glsl";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            filename += "_vert.glsl";
        }

        Terasology.getInstance().getLogger().log(Level.INFO, "Loading shader {0} ({1}, type = {2})", new String[]{_title, filename, String.valueOf(type)});

        // Read in the shader code
        code += readShader(filename);

        if (type == GL20.GL_FRAGMENT_SHADER) {
            _fragmentProgram = shaderId;
        } else if (type == GL20.GL_VERTEX_SHADER) {
            _vertexProgram = shaderId;
        }

        GL20.glShaderSource(shaderId, code);
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
            Terasology.getInstance().getLogger().log(Level.SEVERE, "Failed to read shader.");
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

        Terasology.getInstance().getLogger().log(Level.INFO, "{0}", new String(infoBytes));
    }

    public void enable() {
        GL20.glUseProgram(_shaderPogram);

        // Set the shader parameters if available
        if (_parameters != null) {
            _parameters.applyParameters(this);
        }
    }

    public void setFloat(String desc, float f) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform1f(id, f);
    }

    public void setFloat3(String desc, Vector3f v) {
        setFloat3(desc, v.x, v.y, v.z);
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform3f(id, f1, f2, f3);
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform4f(id, f1, f2, f3, f4);
    }

    public void setInt(String desc, int i) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform1i(id, i);
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform2(id, buffer);
    }

    public void setFloat1(String desc, FloatBuffer buffer) {
        int id = GL20.glGetUniformLocation(_shaderPogram, desc);
        GL20.glUniform1(id, buffer);
    }

    public ShaderParameters getShaderParameters() {
        return _parameters;
    }

    public int getShaderId() {
        return _shaderPogram;
    }
}
