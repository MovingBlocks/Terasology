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

import com.google.common.io.CharStreams;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.SystemConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.shader.IShaderParameters;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.Block;

import javax.swing.*;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GLSL Shader Program Instance class.
 *
 * Provides actual shader compilation and manipulation support.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GLSLShaderProgramInstance {

    private static final Logger logger = LoggerFactory.getLogger(GLSLShaderProgramInstance.class);

    private TIntIntMap fragmentPrograms = new TIntIntHashMap();
    private TIntIntMap vertexPrograms = new TIntIntHashMap();
    private TIntIntMap shaderPrograms = new TIntIntHashMap();

    private TIntIntMap uniformLocationMap = new TIntIntHashMap();

    private int availableFeatures = 0;
    private int activeFeatures = 0;

    private IShaderParameters shaderParameters;

    private boolean disposed = false;

    GLSLShaderProgram shaderProgramBase = null;

    public enum ShaderProgramFeatures {
        FEATURE_REFRACTIVE_PASS(0x01),
        FEATURE_ALPHA_REJECT(0x02),
        FEATURE_LIGHT_POINT(0x04),
        FEATURE_LIGHT_DIRECTIONAL(0x08),
        FEATURE_DEFERRED_LIGHTING(0x10),
        FEATURE_USE_MATRIX_STACK(0x20),
        FEATURE_USE_FORWARD_LIGHTING(0x40),
        FEATURE_ALL(0x80);

        private int value;
        private ShaderProgramFeatures(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    private static String includedFunctionsVertex = "", includedFunctionsFragment = "";

    static {
        InputStream vertStream = GLSLShaderProgram.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsVertIncl.glsl");
        InputStream fragStream = GLSLShaderProgram.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsFragIncl.glsl");

        try {
            includedFunctionsVertex = CharStreams.toString(new InputStreamReader(vertStream));
            includedFunctionsFragment = CharStreams.toString(new InputStreamReader(fragStream));
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

    protected static StringBuilder createShaderBuilder() {
        String preProcessorPreamble = "#version 120\n";

        preProcessorPreamble += "float TEXTURE_OFFSET = " + Block.calcRelativeTileSize() + ";\n";
        preProcessorPreamble += "float BLOCK_LIGHT_POW = " + WorldRenderer.BLOCK_LIGHT_POW + ";\n";
        preProcessorPreamble += "float BLOCK_LIGHT_SUN_POW = " + WorldRenderer.BLOCK_LIGHT_SUN_POW + ";\n";
        preProcessorPreamble += "float BLOCK_INTENSITY_FACTOR = " + WorldRenderer.BLOCK_INTENSITY_FACTOR + ";\n";
        preProcessorPreamble += "float SHADOW_MAP_RESOLUTION = " + CoreRegistry.get(Config.class).getRendering().getShadowMapResolution() + ";\n";
        // TODO: This shouldn't be hardcoded
        preProcessorPreamble += "float TEXTURE_OFFSET_EFFECTS = " + 0.0625f + ";\n";

        Config config = CoreRegistry.get(Config.class);
        StringBuilder builder = new StringBuilder().append(preProcessorPreamble);
        if (config.getRendering().isAnimateGrass())
            builder.append("#define ANIMATED_GRASS \n");
        if (config.getRendering().isAnimateWater())
            builder.append("#define ANIMATED_WATER \n");
        if (config.getRendering().getBlurIntensity() == 0)
            builder.append("#define NO_BLUR \n");
        if (config.getRendering().isFlickeringLight())
            builder.append("#define FLICKERING_LIGHT \n");
        if (config.getRendering().isVignette())
            builder.append("#define VIGNETTE \n");
        if (config.getRendering().isBloom())
            builder.append("#define BLOOM \n");
        if (config.getRendering().isMotionBlur())
            builder.append("#define MOTION_BLUR \n");
        if (config.getRendering().isSsao())
            builder.append("#define SSAO \n");
        if (config.getRendering().isFilmGrain())
            builder.append("#define FILM_GRAIN \n");
        if (config.getRendering().isOutline())
            builder.append("#define OUTLINE \n");
        if (config.getRendering().isLightShafts())
            builder.append("#define LIGHT_SHAFTS \n");
        if (config.getRendering().isDynamicShadows())
            builder.append("#define DYNAMIC_SHADOWS \n");
        if (config.getRendering().isNormalMapping())
            builder.append("#define NORMAL_MAPPING \n");
        if (config.getRendering().isParallaxMapping())
            builder.append("#define PARALLAX_MAPPING \n");
        if (config.getRendering().isDynamicShadowsPcfFiltering())
            builder.append("#define DYNAMIC_SHADOWS_PCF \n");
        if (config.getRendering().isVolumetricFog())
            builder.append("#define VOLUMETRIC_FOG \n");
        if (config.getRendering().isCloudShadows())
            builder.append("#define CLOUD_SHADOWS \n");

        for (int i=0; i< SystemConfig.DebugRenderingStages.values().length; ++i) {
            builder.append("#define "+SystemConfig.DebugRenderingStages.values()[i].toString()+" "+SystemConfig.DebugRenderingStages.values()[i].ordinal()+" \n");
        }

        for (int i=0; i< ChunkTessellator.ChunkVertexFlags.values().length; ++i) {
            builder.append("#define "+ChunkTessellator.ChunkVertexFlags.values()[i].toString()+" "+ChunkTessellator.ChunkVertexFlags.values()[i].getValue()+" \n");
        }

        return builder;
    }

    public GLSLShaderProgramInstance(GLSLShaderProgram shaderProgram) {
       this(shaderProgram, null);
    }

    public GLSLShaderProgramInstance(GLSLShaderProgram shaderProgram, IShaderParameters shaderParameters) {
        this.shaderProgramBase = shaderProgram;
        this.shaderParameters = shaderParameters;
        shaderProgram.registerShaderProgramInstance(this);

        updateAvailableFeatures();
        compileAllShaderPermutations();
    }

    private void updateAvailableFeatures() {
        availableFeatures = 0;

        // Check which features are used in the shaders and update the available features mask accordingly
        for (int i=0; i< ShaderProgramFeatures.FEATURE_ALL.ordinal(); ++i) {
            ShaderProgramFeatures feature = ShaderProgramFeatures.values()[i];

            if (shaderProgramBase.getFragShader().contains(feature.toString())) {
                logger.info("Fragment shader feature '" + feature.toString() + "' is available...");
                availableFeatures |= feature.getValue();
            } else if (shaderProgramBase.getVertShader().contains(feature.toString())) {
                logger.info("Vertex shader feature '" + feature.toString() + "' is available...");
                availableFeatures |= feature.getValue();
            }
        }
    }

    private void compileShaderProgram(int featureHash) {
        compileShader(GL20.GL_FRAGMENT_SHADER, featureHash);
        compileShader(GL20.GL_VERTEX_SHADER, featureHash);

        int shaderProgram = GL20.glCreateProgram();
        shaderPrograms.put(featureHash, shaderProgram);

        GL20.glAttachShader(shaderProgram, fragmentPrograms.get(featureHash));
        GL20.glAttachShader(shaderProgram, vertexPrograms.get(featureHash));
        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);
    }

    private void compileAllShaderPermutations() {
        compileShaderProgram(0);

        TIntArrayList compiledPermutations = new TIntArrayList();
        int counter = 1;

        for (int i=1; i<ShaderProgramFeatures.FEATURE_ALL.getValue(); ++i) {
            // Compile all selected features for this shader...
            int maskedHash = (i & availableFeatures);

            if (maskedHash > 0 && !compiledPermutations.contains(maskedHash)) {
                compileShaderProgram(i);
                compiledPermutations.add(maskedHash);

                counter++;
            }
        }

        logger.info("Compiled {} permutations.", counter);

        disposed = false;
    }

    public void recompile() {
        logger.debug("Recompiling shader {}.", shaderProgramBase.getTitle());

        dispose();
        compileAllShaderPermutations();
    }

    public void dispose() {
        logger.debug("Disposing shader {}.", shaderProgramBase.getTitle());

        TIntIntIterator it = shaderPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteProgram(it.value());
        }
        shaderPrograms.clear();

        it = fragmentPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteShader(it.value());
        }
        fragmentPrograms.clear();

        it = vertexPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteShader(it.value());
        }
        vertexPrograms.clear();

        uniformLocationMap.clear();
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    private void compileShader(int type, int featureHash) {
        int shaderId = GL20.glCreateShader(type);
        StringBuilder shader = createShaderBuilder();

        // Add the activated features for this shader
        for (int i=0; i< ShaderProgramFeatures.FEATURE_ALL.ordinal(); ++i) {
            if ((ShaderProgramFeatures.values()[i].getValue() & featureHash) > 0) {
                shader.append("#define ").append(ShaderProgramFeatures.values()[i].name()).append("\n");
            }
        }

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.append(includedFunctionsFragment).append("\n");
        } else {
            shader.append(includedFunctionsVertex).append("\n");
        }

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.append(shaderProgramBase.getFragShader());
        } else if (type == GL20.GL_VERTEX_SHADER) {
            shader.append(shaderProgramBase.getVertShader());
        }

        String debugShaderType = "UNKNOWN";
        if (type == GL20.GL_FRAGMENT_SHADER) {
            fragmentPrograms.put(featureHash, shaderId);
            debugShaderType = "FRAGMENT";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            vertexPrograms.put(featureHash, shaderId);
            debugShaderType = "VERTEX";
        }

        GL20.glShaderSource(shaderId, shader.toString());
        GL20.glCompileShader(shaderId);

        StringBuilder error = new StringBuilder();
        boolean success = printLogInfo(shaderId, error);

        String errorLine = "";
        if (error.length() > 0) {
            try {
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(error.toString());

                int counter = 0;
                while (m.find()) {
                    if (counter++ % 2 == 1) {
                        int lineNumberInt = Integer.valueOf(m.group());

                        Scanner reader = new Scanner(shader.toString());
                        for (int i=0; i<lineNumberInt - 1; ++i) {
                            reader.nextLine();
                        }

                        errorLine = reader.nextLine();
                        errorLine = "Error prone line: '" + errorLine + "'";

                        logger.warn("{}", error);
                        logger.warn("{}", errorLine);

                        break;
                    }
                }

            } catch (Exception e) {
                // Do nothing...
            }
        }

        if (!success) {
            String errorMessage = debugShaderType+ " Shader '"+shaderProgramBase.getTitle()+"' failed to compile. Terasology might not look quite as good as it should now...\n\n"+error+"\n\n"+errorLine;

            logger.error("{}", errorMessage);
            JOptionPane.showMessageDialog(null, errorMessage, "Shader compilation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean printLogInfo(int shaderId, StringBuilder logEntry) {
        int length = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB);

        int compileStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB);
        //int linkStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB);
        //int validateStatus = ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB);

        if (length > 0) {
            logEntry.append(ARBShaderObjects.glGetInfoLogARB(shaderId, length));
        }

        if (compileStatus == 0 /*|| linkStatus == 0 || validateStatus == 0*/) {
            return false;
        }

        logger.info("Shader '" + shaderProgramBase.getTitle() + "' successfully compiled.");
        return true;
    }

    public void enable() {
        GLSLShaderProgramInstance activeProgram = ShaderManager.getInstance().getActiveShaderProgram();

        if (activeProgram != this
                || ShaderManager.getInstance().getActiveFeatures() != activeFeatures) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL20.glUseProgram(getActiveShaderProgramId());

            // Make sure the shader manager knows that this program is currently active
            ShaderManager.getInstance().setActiveShaderProgram(this);

            // Set the shader parameters if available
            if (shaderParameters != null) {
                shaderParameters.applyParameters(this);
            }
        }
    }

    public int getActiveShaderProgramId() {
        return shaderPrograms.get(activeFeatures);
    }

    public void setFloat(String desc, float f) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform1f(id, f);
    }

    public void setFloat2(String desc, float f1, float f2) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform2f(id, f1, f2);
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform3f(id, f1, f2, f3);
    }

    public void setFloat3(String desc, FloatBuffer buffer) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform3(id, buffer);
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform4f(id, f1, f2, f3, f4);
    }

    public void setInt(String desc, int i) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform1i(id, i);
    }

    public void setBoolean(String desc, boolean b) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform1i(id, b ? 1 : 0);
    }

    public void setIntForAllPermutations(String desc, int i) {
        TIntIntIterator it = shaderPrograms.iterator();

        while (it.hasNext()) {
            it.advance();

            GL20.glUseProgram(it.value());
            int id = getUniformLocation(it.value(), desc);
            GL20.glUniform1i(id, i);
        }

        enable();
    }

    public void setBooleanForAllPermutations(String desc, boolean b) {
        TIntIntIterator it = shaderPrograms.iterator();

        while (it.hasNext()) {
            it.advance();

            GL20.glUseProgram(it.value());
            int id = getUniformLocation(it.value(), desc);
            GL20.glUniform1i(id, b ? 1 : 0);
        }

        enable();
    }

    public void setFloat3ForAllPermutations(String desc, float f1, float f2, float f3) {
        TIntIntIterator it = shaderPrograms.iterator();

        while (it.hasNext()) {
            it.advance();

            GL20.glUseProgram(it.value());
            int id = getUniformLocation(it.value(), desc);
            GL20.glUniform3f(id, f1, f2, f3);
        }

        enable();
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform2(id, buffer);
    }

    public void setFloat(String desc, FloatBuffer buffer) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniform1(id, buffer);
    }

    public void setFloat4(String desc, Vector4f vec) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }

        setFloat4(desc, vec.x, vec.y, vec.z, vec.w);
    }

    public void setMatrix4(String desc, FloatBuffer floatBuffer) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }
        
        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniformMatrix4(id, false, floatBuffer);
    }

    public void setMatrix3(String desc, FloatBuffer floatBuffer) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }
        
        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniformMatrix3(id, false, floatBuffer);
    }

    public void setMatrix4(String desc, Matrix4f m) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }
        
        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniformMatrix4(id, false, TeraMath.matrixToFloatBuffer(m));
    }

    public void setMatrix3(String desc, Matrix3f m) {
        int activeShaderProgramId = getActiveShaderProgramId();

        if (activeShaderProgramId <= 0) {
            return;
        }
        
        enable();
        int id = getUniformLocation(activeShaderProgramId, desc);
        GL20.glUniformMatrix3(id, false, TeraMath.matrixToFloatBuffer(m));
    }

    private int generateHash(int activeShaderProgramId, String desc) {
        return (desc + activeShaderProgramId).hashCode();
    }

    private int getUniformLocation(int activeShaderProgramId, String desc) {
        int hash = generateHash(activeShaderProgramId, desc);

        if (uniformLocationMap.containsKey(hash)) {
            return uniformLocationMap.get(hash);
        }

        int id = GL20.glGetUniformLocation(activeShaderProgramId, desc);
        uniformLocationMap.put(hash, id);

        return id;
    }

    public boolean wasSet(String desc) {
        return uniformLocationMap.containsKey(generateHash(getActiveShaderProgramId(), desc));
    }

    public void setCamera(Camera camera) {
        setMatrix4("viewMatrix", camera.getViewMatrix());
        setMatrix4("projMatrix", camera.getProjectionMatrix());
        setMatrix4("viewProjMatrix", camera.getViewProjectionMatrix());
        setMatrix4("invProjMatrix", camera.getInverseProjectionMatrix());
    }

    public int getActiveFeatures() {
        return activeFeatures;
    }

    public void addFeatureIfAvailable(ShaderProgramFeatures feature) {
        if ((availableFeatures & feature.getValue()) == feature.getValue()) {
            activeFeatures |= feature.getValue();
        } else {
            throw new RuntimeException("Feature not available");
        }
    }

    public void removeFeature(ShaderProgramFeatures feature) {
        activeFeatures &= ~feature.getValue();
    }

    public void removeFeatures(int featureHash) {
        activeFeatures &= ~featureHash;
    }

    public GLSLShaderProgram getShaderProgramBase() {
        return shaderProgramBase;
    }

    public IShaderParameters getShaderParameters() {
        return shaderParameters;
    }
}
