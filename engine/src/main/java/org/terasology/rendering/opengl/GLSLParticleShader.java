/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.rendering.opengl;

import com.google.common.base.Charsets;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameThread;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;

public class GLSLParticleShader extends Shader {

    private static final Logger logger = LoggerFactory.getLogger(GLSLParticleShader.class);

    private int programHandle;
    private ShaderData shaderProgramBase;

    private int colorLocation;
    private int viewProjectionLocation;
    private int cameraPositionLocation;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public GLSLParticleShader(ResourceUrn urn, AssetType<?, ShaderData> assetType, ShaderData data) {
        super(urn, assetType);

        reload(data);
    }

    public void bind() {
        GL20.glUseProgram(programHandle);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void recompile() {
        programHandle = createShaderProgram();

        colorLocation = GL20.glGetUniformLocation(programHandle, "color");
        viewProjectionLocation = GL20.glGetUniformLocation(programHandle, "view_projection");
        cameraPositionLocation = GL20.glGetUniformLocation(programHandle, "camera_position");
    }

    public void setColor(float r, float g, float b) {
        GL20.glUniform4f(colorLocation, r, g, b, 1f);
    }

    public void setCameraPosition(float x, float y, float z) {
        GL20.glUniform3f(cameraPositionLocation, x, y, z);
    }

    public void setViewProjection(Matrix4f matrix) {
        GL20.glUniformMatrix4(viewProjectionLocation, false, matrix.get(matrixBuffer));
    }

    private int createShaderProgram() {
        int vertexShaderHandle = compileShader(shaderProgramBase.getVertexProgram(), GL_VERTEX_SHADER);
        int geometryShaderHandle = compileShader(shaderProgramBase.getGeometryProgram(), GL32.GL_GEOMETRY_SHADER);
        int fragmentShaderHandle = compileShader(shaderProgramBase.getFragmentProgram(), GL20.GL_FRAGMENT_SHADER);

        int handle = GL20.glCreateProgram();
        glAttachShader(handle, vertexShaderHandle);
        glAttachShader(handle, geometryShaderHandle);
        glAttachShader(handle, fragmentShaderHandle);

        GL20.glBindAttribLocation(handle, 0, "position_ws");

        GL20.glLinkProgram(handle);

        GL20.glDetachShader(handle, vertexShaderHandle);
        GL20.glDetachShader(handle, geometryShaderHandle);
        GL20.glDetachShader(handle, fragmentShaderHandle);

        validateLinking(handle);
        validateProgram(handle);

        return handle;
    }

    private int compileShader(String shaderSource, int type) {
        int handle = GL20.glCreateShader(type);
        GL20.glShaderSource(handle, shaderSource);
        GL20.glCompileShader(handle);

        if (glGetShaderi(handle, GL_COMPILE_STATUS) == GL_FALSE) {
            String info = glGetShaderInfoLog(handle, GL_INFO_LOG_LENGTH);
            throw new RuntimeException("Shader failed to compile. InfoLog: \n" + info);
        }

        return handle;
    }

    private void validateLinking(int handle) {
        if (glGetProgrami(handle, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(
                    "OpenGL shader linking failed: " + GL20.glGetProgramInfoLog(handle, GL_INFO_LOG_LENGTH)
            );
        }
    }

    private void validateProgram(int handle) {
        GL20.glValidateProgram(handle);

        int error = glGetError();
        if (error != 0) {
            throw new RuntimeException("OpenGL shader creation failed. Error code: " + error);
        }
    }

    @Override
    public ShaderParameterMetadata getParameter(String desc) {
        return null;
    }

    @Override
    public Iterable<ShaderParameterMetadata> listParameters() {
        return Collections.emptyList();
    }

    protected void doReload(ShaderData data) {
        try {
            GameThread.synch(() -> {
                logger.debug("Recompiling shader {}.", getUrn());
                shaderProgramBase = data;

                try {
                    recompile();
                } catch (RuntimeException e) {
                    logger.warn(e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    private static InputStreamReader getInputStreamReaderFromResource(String resource) {
        InputStream resourceStream = GLSLShader.class.getClassLoader().getResourceAsStream(resource);
        return new InputStreamReader(resourceStream, Charsets.UTF_8);
    }
}
