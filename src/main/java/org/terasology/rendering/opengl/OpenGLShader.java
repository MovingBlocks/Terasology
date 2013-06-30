/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.shader.ParamType;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.world.block.Block;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

/**
 * @author Immortius
 */
public class OpenGLShader extends AbstractAsset<ShaderData> implements Shader {
    private static final String PRE_PROCESSOR_PREAMBLE = "#version 120 \n float TEXTURE_OFFSET = " + Block.TEXTURE_OFFSET + "; \n";
    public static final String INCLUDED_FUNCTIONS_VERTEX;
    public static final String INCLUDED_FUNCTIONS_FRAGMENT;

    private static final Logger logger = LoggerFactory.getLogger(Shader.class);

    private ShaderData data;
    private Map<String, ParamType> params = Maps.newHashMap();

    private int fragmentProgram = 0;
    private int vertexProgram = 0;
    private boolean valid = false;

    public OpenGLShader(AssetUri uri, ShaderData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(ShaderData data) {
        dispose();
        this.data = data;

        params.clear();
        for (ShaderParameterMetadata paramData : data.getParameterMetadata()) {
            params.put(paramData.getName(), paramData.getType());
        }
        compileShaderProgram();
    }

    public void recompile() {
        logger.debug("Recompiling shader {}.", getURI());

        dispose();
        compileShaderProgram();
    }

    public void dispose() {
        logger.debug("Disposing shader {}.", getURI());

        if (fragmentProgram != 0) {
            GL20.glDeleteProgram(fragmentProgram);
            fragmentProgram = 0;
        }

        if (vertexProgram != 0) {
            GL20.glDeleteProgram(vertexProgram);
            vertexProgram = 0;
        }
    }

    public boolean isDisposed() {
        return fragmentProgram == 0 && vertexProgram == 0;
    }

    public boolean isValid() {
        return valid;
    }

    public ShaderParameterMetadata getParameter(String desc) {
        if (params.containsKey(desc)) {
            return new ShaderParameterMetadata(desc, params.get(desc));
        }
        return null;
    }

    public Iterable<ShaderParameterMetadata> listParameters() {
        return data.getParameterMetadata();
    }

    public int generateShaderInstance() {
        int shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, fragmentProgram);
        GL20.glAttachShader(shaderProgram, vertexProgram);
        GL20.glLinkProgram(shaderProgram);
        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            logger.error("Failed to link shader {}.", GL20.glGetProgramInfoLog(shaderProgram, GL20.GL_LINK_STATUS));
            GL20.glDeleteProgram(shaderProgram);
            return 0;
        }
        GL20.glValidateProgram(shaderProgram);
        if (GL20.glGetProgrami(shaderProgram, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            logger.error("Failed to validate shader {}.", GL20.glGetProgramInfoLog(shaderProgram, GL20.GL_VALIDATE_STATUS));
            GL20.glDeleteProgram(shaderProgram);
            return 0;
        }
        return shaderProgram;
    }

    private void compileShaderProgram() {
        String finalVert = createShaderBuilder().append(INCLUDED_FUNCTIONS_VERTEX).append('\n').append(data.getVertexProgram()).toString();
        String finalFrag = createShaderBuilder().append(INCLUDED_FUNCTIONS_FRAGMENT).append('\n').append(data.getFragmentProgram()).toString();

        valid = true;
        compileShader(GL20.GL_FRAGMENT_SHADER, finalFrag);
        compileShader(GL20.GL_VERTEX_SHADER, finalVert);
    }

    private void compileShader(int type, String shaderCode) {

        int shaderId = GL20.glCreateShader(type);

        if (type == GL20.GL_FRAGMENT_SHADER) {
            fragmentProgram = shaderId;
        } else if (type == GL20.GL_VERTEX_SHADER) {
            vertexProgram = shaderId;
        }

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        verifyCompile(shaderId);
    }

    private void verifyCompile(int shaderId) {
        IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
        GL20.glGetShader(shaderId, GL20.GL_INFO_LOG_LENGTH, intBuffer);

        int length = intBuffer.get();

        if (length <= 1) {
            return;
        }

        valid = false;

        ByteBuffer infoBuffer = BufferUtils.createByteBuffer(length);
        intBuffer.flip();

        GL20.glGetShaderInfoLog(shaderId, intBuffer, infoBuffer);

        int actualLength = intBuffer.get();
        byte[] infoBytes = new byte[actualLength];
        infoBuffer.get(infoBytes);

        logger.debug("{}", new String(infoBytes));
    }

    public static StringBuilder createShaderBuilder() {
        Config config = CoreRegistry.get(Config.class);
        StringBuilder builder = new StringBuilder().append(PRE_PROCESSOR_PREAMBLE);
        if (config.getRendering().isAnimateGrass())
            builder.append("#define ANIMATED_WATER_AND_GRASS \n");
        if (config.getRendering().getBlurIntensity() == 0)
            builder.append("#define NO_BLUR \n");
        if (!config.getRendering().isBloom())
            builder.append("#define NO_POST_PROCESSING \n");
        if (config.getRendering().isFlickeringLight())
            builder.append("#define FLICKERING_LIGHT \n");
        return builder;
    }

    static {
        String includeVertex = "";
        String includeFrag = "";
        try (InputStream vertStream = Shader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsVertIncl.glsl");
             InputStream fragStream = Shader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsFragIncl.glsl")) {
            includeVertex = CharStreams.toString(new InputStreamReader(vertStream));
            includeFrag = CharStreams.toString(new InputStreamReader(fragStream));
        } catch (IOException e) {
            logger.error("Failed to load Include shader resources");
        }
        INCLUDED_FUNCTIONS_VERTEX = includeVertex;
        INCLUDED_FUNCTIONS_FRAGMENT = includeFrag;
    }
}