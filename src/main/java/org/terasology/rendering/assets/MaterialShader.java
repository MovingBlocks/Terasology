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

package org.terasology.rendering.assets;

import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.metadata.ParamMetadata;
import org.terasology.rendering.assets.metadata.ParamType;
import org.terasology.rendering.assets.metadata.ShaderMetadata;
import org.terasology.rendering.shader.ShaderProgram;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Immortius
 */
public class MaterialShader implements Asset {
    private static String IncludedFunctionsVertex = "", IncludedFunctionsFragment = "";

    private static final Logger logger = LoggerFactory.getLogger(MaterialShader.class);

    private final AssetUri uri;
    private String vertShader;
    private String fragShader;

    private int fragmentProgram = 0;
    private int vertexProgram = 0;
    private boolean valid = false;
    private Map<String, ParamType> params = Maps.newHashMap();

    public MaterialShader(AssetUri uri, String vertShader, String fragShader, ShaderMetadata metadata) {
        this.uri = uri;

        this.vertShader = vertShader;
        this.fragShader = fragShader;

        for (ParamMetadata paramData : metadata.getParameters()) {
            params.put(paramData.getName(), paramData.getType());
        }

        compileShaderProgram();
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    public void recompile() {
        logger.debug("Recompiling shader {}.", uri);

        dispose();
        compileShaderProgram();
    }

    public void dispose() {
        logger.debug("Disposing shader {}.", uri);

        GL20.glDeleteProgram(fragmentProgram);
        fragmentProgram = 0;

        GL20.glDeleteProgram(vertexProgram);
        vertexProgram = 0;
    }

    public boolean isDisposed() {
        return fragmentProgram == 0 && vertexProgram == 0;
    }

    public boolean isValid() {
        return valid;
    }

    public ParamMetadata getParameter(String desc) {
        if (params.containsKey(desc)) {
            return new ParamMetadata(desc, params.get(desc));
        }
        return null;
    }

    public Iterable<ParamMetadata> listParameters() {
        return new Iterable<ParamMetadata>() {
            @Override
            public Iterator<ParamMetadata> iterator() {
                return new Iterator<ParamMetadata>() {
                    Iterator<Map.Entry<String, ParamType>> internal = params.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return internal.hasNext();
                    }

                    @Override
                    public ParamMetadata next() {
                        Map.Entry<String, ParamType> entry = internal.next();
                        return new ParamMetadata(entry.getKey(), entry.getValue());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Remove not supported");
                    }
                };
            }
        };
    }

    public int generateShaderInstance() {
        int shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, fragmentProgram);
        GL20.glAttachShader(shaderProgram, vertexProgram);
        GL20.glLinkProgram(shaderProgram);
        if (GL20.glGetProgram(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            logger.error("Failed to link shader {}.", GL20.glGetProgramInfoLog(shaderProgram, GL20.GL_LINK_STATUS));
            GL20.glDeleteProgram(shaderProgram);
            return 0;
        }
        GL20.glValidateProgram(shaderProgram);
        if (GL20.glGetProgram(shaderProgram, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            logger.error("Failed to validate shader {}.", GL20.glGetProgramInfoLog(shaderProgram, GL20.GL_VALIDATE_STATUS));
            GL20.glDeleteProgram(shaderProgram);
            return 0;
        }
        return shaderProgram;
    }

    private void compileShaderProgram() {
        String finalVert = ShaderProgram.createShaderBuilder().append(IncludedFunctionsVertex).append('\n').append(vertShader).toString();
        String finalFrag = ShaderProgram.createShaderBuilder().append(IncludedFunctionsFragment).append('\n').append(fragShader).toString();

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

    public static String getIncludedFunctionsVertex() {
        return IncludedFunctionsVertex;
    }

    public static String getIncludedFunctionsFragment() {
        return IncludedFunctionsFragment;
    }

    static {
        InputStream vertStream = MaterialShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsVertIncl.glsl");
        InputStream fragStream = MaterialShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsFragIncl.glsl");
        try {
            IncludedFunctionsVertex = CharStreams.toString(new InputStreamReader(vertStream));
            IncludedFunctionsFragment = CharStreams.toString(new InputStreamReader(fragStream));
        } catch (IOException e) {
            logger.error("Failed to load Include shader resources");
        } finally {
            // JAVA7: Clean up
            try {
                vertStream.close();
            } catch (IOException e) {
                logger.error("Failed to close globalFunctionsVertIncl.glsl stream");
            }
            try {
                fragStream.close();
            } catch (IOException e) {
                logger.error("Failed to close globalFunctionsFragIncl.glsl stream");
            }
        }
    }

}
