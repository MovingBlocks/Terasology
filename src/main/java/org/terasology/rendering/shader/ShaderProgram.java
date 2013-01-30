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
import java.nio.FloatBuffer;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.Shader;

import javax.swing.*;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

/**
 * Wraps a OpenGL shader program. Provides convenience methods for setting
 * uniform variables of various types.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);

    private int shaderProgram, fragmentProgram, vertexProgram;
    private String title;

    private IShaderParameters parameters;

    public ShaderProgram(String title) {
        this(title, null);
    }

    public ShaderProgram(String title, IShaderParameters params) {
        this.title = title;
        this.parameters = params;

        compileShaderProgram();
    }

    private void compileShaderProgram() {
        compileShader(GL20.GL_FRAGMENT_SHADER);
        compileShader(GL20.GL_VERTEX_SHADER);

        shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, fragmentProgram);
        GL20.glAttachShader(shaderProgram, vertexProgram);
        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);
    }

    public void recompile() {
        logger.debug("Recompiling shader {}.", title);

        dispose();
        compileShaderProgram();
    }

    public void dispose() {
        logger.debug("Disposing shader {}.", title);

        GL20.glDeleteShader(shaderProgram);
        shaderProgram = 0;

        GL20.glDeleteProgram(fragmentProgram);
        fragmentProgram = 0;

        GL20.glDeleteProgram(vertexProgram);
        vertexProgram = 0;
    }

    private void compileShader(int type) {

        int shaderId = GL20.glCreateShader(type);

        StringBuilder shader = Shader.createShaderBuilder();

        if (type == GL20.GL_FRAGMENT_SHADER)
            shader.append(Shader.getIncludedFunctionsFragment()).append("\n");
        else
            shader.append(Shader.getIncludedFunctionsVertex()).append("\n");

        String filename = title;

        if (type == GL20.GL_FRAGMENT_SHADER) {
            filename += "_frag.glsl";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            filename += "_vert.glsl";
        }

        logger.debug("Loading shader {} ({}, type = {})", title, filename, String.valueOf(type));

        // Read in the shader code
        shader.append(readShader(filename));

        if (type == GL20.GL_FRAGMENT_SHADER) {
            fragmentProgram = shaderId;
        } else if (type == GL20.GL_VERTEX_SHADER) {
            vertexProgram = shaderId;
        }

        GL20.glShaderSource(shaderId, shader.toString());
        GL20.glCompileShader(shaderId);

        String error;
        if ((error = printLogInfo(shaderId)) != null) {
            JOptionPane.showMessageDialog(null, "Shader '"+title+"' failed to compile. Terasology might not look quite as good as it should now...\n\n"+error, "Shader compilation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String readShader(String filename) {
        String line, code = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("assets/shaders/" + filename).openStream()));
            while ((line = reader.readLine()) != null) {
                code += line + "\n";
            }
        } catch (Exception e) {
            logger.error("Failed to read shader '{}'.", filename, e);
        }

        return code;
    }

    private String printLogInfo(int shaderId) {
        int length = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);

        int compileStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB);
        //int linkStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB);
        //int validateStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB);

        String logEntry = ARBShaderObjects.glGetInfoLogARB(shaderId, length);

        if (length > 0) {
            logger.error("{}", logEntry);
        }

        if (compileStatus == 0 /*|| linkStatus == 0 || validateStatus == 0*/) {
            return logEntry;
        }

        logger.info("Shader '"+title+"' successfully compiled.");
        return null;
    }

    public void enable() {
        ShaderProgram activeProgram = ShaderManager.getInstance().getActiveShaderProgram();

        if (activeProgram != this) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL20.glUseProgram(shaderProgram);

            // Make sure the shader manager knows that this program is currently active
            ShaderManager.getInstance().setActiveShaderProgram(this);

            // Set the shader parameters if available
            if (parameters != null) {
                parameters.applyParameters(this);
            }
        }
    }

    public void setFloat(String desc, float f) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform1f(id, f);
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform3f(id, f1, f2, f3);
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform4f(id, f1, f2, f3, f4);
    }

    public void setInt(String desc, int i) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform1i(id, i);
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform2(id, buffer);
    }

    public void setFloat1(String desc, FloatBuffer buffer) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniform1(id, buffer);
    }

    public void setMatrix4(String desc, Matrix4f m) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniformMatrix4(id, false, TeraMath.matrixToFloatBuffer(m));
    }

    public void setMatrix3(String desc, Matrix3f m) {
        enable();
        int id = GL20.glGetUniformLocation(shaderProgram, desc);
        GL20.glUniformMatrix3(id, false, TeraMath.matrixToFloatBuffer(m));
    }

    public IShaderParameters getShaderParameters() {
        return parameters;
    }

    public int getShaderId() {
        return shaderProgram;
    }
}
