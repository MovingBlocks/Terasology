/*
 * Copyright 2013 MovingBlocks
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
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.primitives.ChunkVertexFlag;
import org.terasology.rendering.shader.ShaderParametersSSAO;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.loader.WorldAtlas;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GLSL Shader Program Instance class.
 * <p/>
 * Provides actual shader compilation and manipulation support.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GLSLShader extends AbstractAsset<ShaderData> implements Shader {

    private static final Logger logger = LoggerFactory.getLogger(GLSLShader.class);

    private static String includedFunctionsVertex = "";
    private static String includedFunctionsFragment = "";
    private static String includedDefines = "";
    private static String includedUniforms = "";

    static {
        try (
                InputStream vertStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsVertIncl.glsl");
                InputStream fragStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalFunctionsFragIncl.glsl");
                InputStream uniformsStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalUniformsIncl.glsl");
                InputStream definesStream = GLSLShader.class.getClassLoader().getResourceAsStream("org/terasology/include/globalDefinesIncl.glsl")
        ) {
            includedFunctionsVertex = CharStreams.toString(new InputStreamReader(vertStream));
            includedFunctionsFragment = CharStreams.toString(new InputStreamReader(fragStream));
            includedDefines = CharStreams.toString(new InputStreamReader(definesStream));
            includedUniforms = CharStreams.toString(new InputStreamReader(uniformsStream));
        } catch (IOException e) {
            logger.error("Failed to load Include shader resources");
        }
    }

    private TIntIntMap fragmentPrograms = new TIntIntHashMap();
    private TIntIntMap vertexPrograms = new TIntIntHashMap();

    private EnumSet<ShaderProgramFeature> availableFeatures = Sets.newEnumSet(Collections.<ShaderProgramFeature>emptyList(), ShaderProgramFeature.class);

    private ShaderData shaderProgramBase = null;
    private Map<String, ShaderParameterMetadata> parameters = Maps.newHashMap();

    public GLSLShader(AssetUri uri, ShaderData data) {
        super(uri);
        reload(data);
    }

    public Set<ShaderProgramFeature> getAvailableFeatures() {
        return availableFeatures;
    }

    public int linkShaderProgram(int featureHash) {
        int shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, fragmentPrograms.get(featureHash));
        GL20.glAttachShader(shaderProgram, vertexPrograms.get(featureHash));
        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);
        return shaderProgram;
    }

    @Override
    public void recompile() {
        compileAllShaderPermutations();
        // TODO: reload materials
    }

    @Override
    public ShaderParameterMetadata getParameter(String desc) {
        return parameters.get(desc);
    }

    @Override
    public Iterable<ShaderParameterMetadata> listParameters() {
        return parameters.values();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing shader {}.", getURI());

        TIntIntIterator it = fragmentPrograms.iterator();
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
        shaderProgramBase = null;
    }

    @Override
    public boolean isDisposed() {
        return shaderProgramBase == null;
    }

    @Override
    public void reload(ShaderData data) {
        logger.debug("Recompiling shader {}.", getURI());

        dispose();
        shaderProgramBase = data;
        parameters.clear();
        for (ShaderParameterMetadata metadata : shaderProgramBase.getParameterMetadata()) {
            parameters.put(metadata.getName(), metadata);
        }
        updateAvailableFeatures();
        recompile();
    }

    private static StringBuilder createShaderBuilder() {
        String preProcessorPreamble = "#version 120\n";

        // TODO: Implement a system for this - this has gotten way out of hand.
        if (CoreRegistry.get(WorldAtlas.class) != null) {
            preProcessorPreamble += "#define TEXTURE_OFFSET " + CoreRegistry.get(WorldAtlas.class).getRelativeTileSize() + "\n";
        } else {
            preProcessorPreamble += "#define TEXTURE_OFFSET 0.06125\n";
        }
        preProcessorPreamble += "#define BLOCK_LIGHT_POW " + WorldRenderer.BLOCK_LIGHT_POW + "\n";
        preProcessorPreamble += "#define BLOCK_LIGHT_SUN_POW " + WorldRenderer.BLOCK_LIGHT_SUN_POW + "\n";
        preProcessorPreamble += "#define BLOCK_INTENSITY_FACTOR " + WorldRenderer.BLOCK_INTENSITY_FACTOR + "\n";
        preProcessorPreamble += "#define SHADOW_MAP_RESOLUTION " + (float) CoreRegistry.get(Config.class).getRendering().getShadowMapResolution() + "\n";
        preProcessorPreamble += "#define SSAO_KERNEL_ELEMENTS " + ShaderParametersSSAO.SSAO_KERNEL_ELEMENTS + "\n";
        preProcessorPreamble += "#define SSAO_NOISE_SIZE " + ShaderParametersSSAO.SSAO_NOISE_SIZE + "\n";
        // TODO: This shouldn't be hardcoded
        preProcessorPreamble += "#define TEXTURE_OFFSET_EFFECTS " + 0.0625f + "\n";

        Config config = CoreRegistry.get(Config.class);
        StringBuilder builder = new StringBuilder().append(preProcessorPreamble);
        if (config.getRendering().isAnimateGrass()) {
            builder.append("#define ANIMATED_GRASS \n");
        }
        if (config.getRendering().isAnimateWater()) {
            builder.append("#define ANIMATED_WATER \n");
        }
        if (config.getRendering().getBlurIntensity() == 0) {
            builder.append("#define NO_BLUR \n");
        }
        if (config.getRendering().isFlickeringLight()) {
            builder.append("#define FLICKERING_LIGHT \n");
        }
        if (config.getRendering().isVignette()) {
            builder.append("#define VIGNETTE \n");
        }
        if (config.getRendering().isBloom()) {
            builder.append("#define BLOOM \n");
        }
        if (config.getRendering().isMotionBlur()) {
            builder.append("#define MOTION_BLUR \n");
        }
        if (config.getRendering().isSsao()) {
            builder.append("#define SSAO \n");
        }
        if (config.getRendering().isFilmGrain()) {
            builder.append("#define FILM_GRAIN \n");
        }
        if (config.getRendering().isOutline()) {
            builder.append("#define OUTLINE \n");
        }
        if (config.getRendering().isLightShafts()) {
            builder.append("#define LIGHT_SHAFTS \n");
        }
        if (config.getRendering().isDynamicShadows()) {
            builder.append("#define DYNAMIC_SHADOWS \n");
        }
        if (config.getRendering().isNormalMapping()) {
            builder.append("#define NORMAL_MAPPING \n");
        }
        if (config.getRendering().isParallaxMapping()) {
            builder.append("#define PARALLAX_MAPPING \n");
        }
        if (config.getRendering().isDynamicShadowsPcfFiltering()) {
            builder.append("#define DYNAMIC_SHADOWS_PCF \n");
        }
        if (config.getRendering().isVolumetricFog()) {
            builder.append("#define VOLUMETRIC_FOG \n");
        }
        if (config.getRendering().isCloudShadows()) {
            builder.append("#define CLOUD_SHADOWS \n");
        }

        for (RenderingDebugConfig.DebugRenderingStage stage : RenderingDebugConfig.DebugRenderingStage.values()) {
            builder.append("#define ").append(stage.getDefineName()).append(" int(").append(stage.getIndex()).append(") \n");
        }

        for (ChunkVertexFlag vertexFlag : ChunkVertexFlag.values()) {
            builder.append("#define ").append(vertexFlag.getDefineName()).append(" int(").append(vertexFlag.getValue()).append(") \n");
        }

        return builder;
    }

    private void updateAvailableFeatures() {
        availableFeatures.clear();

        // Check which features are used in the shaders and update the available features mask accordingly
        for (ShaderProgramFeature feature : ShaderProgramFeature.values()) {

            // TODO: Have our own shader language and parse this stuff out properly
            if (shaderProgramBase.getFragmentProgram().contains(feature.toString())) {
                logger.info("Fragment shader feature '" + feature.toString() + "' is available...");
                availableFeatures.add(feature);
            } else if (shaderProgramBase.getVertexProgram().contains(feature.toString())) {
                logger.info("Vertex shader feature '" + feature.toString() + "' is available...");
                availableFeatures.add(feature);
            }
        }
    }

    private void compileShaders(Set<ShaderProgramFeature> features) {
        compileShader(GL20.GL_FRAGMENT_SHADER, features);
        compileShader(GL20.GL_VERTEX_SHADER, features);
    }

    private void compileAllShaderPermutations() {
        compileShaders(Collections.<ShaderProgramFeature>emptySet());

        int counter = 1;

        for (Set<ShaderProgramFeature> permutation : Sets.powerSet(availableFeatures)) {
            compileShaders(permutation);
            counter++;
        }
        logger.info("Compiled {} permutations.", counter);
    }

    private void compileShader(int type, Set<ShaderProgramFeature> features) {
        int shaderId = GL20.glCreateShader(type);
        StringBuilder shader = createShaderBuilder();

        // Add the activated features for this shader
        for (ShaderProgramFeature feature : features) {
            shader.append("#define ").append(feature.name()).append("\n");
        }

        shader.append("\n");

        shader.append(includedDefines);
        shader.append(includedUniforms);

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.append(includedFunctionsFragment).append("\n");
        } else {
            shader.append(includedFunctionsVertex).append("\n");
        }

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.append(shaderProgramBase.getFragmentProgram());
        } else if (type == GL20.GL_VERTEX_SHADER) {
            shader.append(shaderProgramBase.getVertexProgram());
        }

        String debugShaderType = "UNKNOWN";
        int featureHash = ShaderProgramFeature.getBitset(features);
        if (type == GL20.GL_FRAGMENT_SHADER) {
            fragmentPrograms.put(featureHash, shaderId);
            debugShaderType = "FRAGMENT";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            vertexPrograms.put(featureHash, shaderId);
            debugShaderType = "VERTEX";
        }

        // Dump all final shader sources to the log directory
        final String strippedTitle = getURI().toString().replace(":", "-");

        Path path = PathManager.getInstance().getLogPath().resolve(debugShaderType.toLowerCase() + "_" + strippedTitle + "_" + featureHash + ".glsl");
        try (BufferedWriter writer = Files.newBufferedWriter(path, TerasologyConstants.CHARSET)) {
            writer.write(shader.toString());
        } catch (Exception e) {
            logger.error("Failed to dump shader source.");
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
                        for (int i = 0; i < lineNumberInt - 1; ++i) {
                            reader.nextLine();
                        }

                        errorLine = reader.nextLine();
                        errorLine = "Error prone line: '" + errorLine + "'";

                        logger.warn("{} \n Line: {}", error, errorLine);

                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Error parsing shader compile error: {}", error, e);
            }
        }

        if (!success) {
            String errorMessage = debugShaderType + " Shader '" + getURI() + "' failed to compile. Terasology might not look quite as good as it should now...\n\n"
                    + error + "\n\n" + errorLine;

            logger.error(errorMessage);
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

        logger.info("Shader '{}' successfully compiled.", getURI());
        return true;
    }

}
