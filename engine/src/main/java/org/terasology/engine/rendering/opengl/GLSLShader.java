// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.shader.ShaderData;
import org.terasology.engine.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.primitives.ChunkVertexFlag;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * GLSL Shader Program Instance class.
 * <p>
 * Provides actual shader compilation and manipulation support.
 * </p>
 */
public class GLSLShader extends Shader {
    // TODO this should be handled another way, we need to get ssao parameters here
    public static int ssaoNoiseSize = 4;

    private static final Logger logger = LoggerFactory.getLogger(GLSLShader.class);

    private static String includedFunctionsVertex = "";
    private static String includedFunctionsFragment = "";
    private static String includedDefines = "";
    private static String includedUniforms = "";

    static {
        try (
                InputStreamReader vertStream = getInputStreamReaderFromResource("org/terasology/engine/include/globalFunctionsVertIncl.glsl");
                InputStreamReader fragStream = getInputStreamReaderFromResource("org/terasology/engine/include/globalFunctionsFragIncl.glsl");
                InputStreamReader uniformsStream = getInputStreamReaderFromResource("org/terasology/engine/include/globalUniformsIncl.glsl");
                InputStreamReader definesStream = getInputStreamReaderFromResource("org/terasology/engine/include/globalDefinesIncl.glsl")
        ) {
            includedFunctionsVertex = CharStreams.toString(vertStream);
            includedFunctionsFragment = CharStreams.toString(fragStream);
            includedDefines = CharStreams.toString(uniformsStream);
            includedUniforms = CharStreams.toString(definesStream);
        } catch (IOException e) {
            logger.error("Failed to load Include shader resources");
        }
    }

    // TODO this should be handled another way, we need to get ssao parameters here
    public int ssaoKernelElements = 32;

    private EnumSet<ShaderProgramFeature> availableFeatures = Sets.newEnumSet(Collections.emptyList(), ShaderProgramFeature.class);
    private ShaderData shaderProgramBase;
    private final LwjglGraphicsProcessing graphicsProcessing;
    private Map<String, ShaderParameterMetadata> parameters = Maps.newHashMap();

    private Config config = CoreRegistry.get(Config.class);

    private DisposalAction disposalAction;


    public GLSLShader(ResourceUrn urn, AssetType<?, ShaderData> assetType, ShaderData data,
                      DisposalAction disposalAction, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType, disposalAction);
        this.disposalAction = disposalAction;
        this.graphicsProcessing = graphicsProcessing;
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }


    public static GLSLShader create(ResourceUrn urn, AssetType<?, ShaderData> assetType, ShaderData data,
                                    LwjglGraphicsProcessing graphicsProcessing) {
        return new GLSLShader(urn, assetType, data, new GLSLShader.DisposalAction(urn, graphicsProcessing), graphicsProcessing);
    }

    private static InputStreamReader getInputStreamReaderFromResource(String resource) {
        InputStream resourceStream = GLSLShader.class.getClassLoader().getResourceAsStream(resource);
        return new InputStreamReader(resourceStream, Charsets.UTF_8);
    }

    // made package-private after CheckStyle suggestion
    Set<ShaderProgramFeature> getAvailableFeatures() {
        return availableFeatures;
    }

    // made package-private after CheckStyle suggestion
    int linkShaderProgram(int featureHash) {
        int shaderProgram = GL20.glCreateProgram();

        GL20.glAttachShader(shaderProgram, disposalAction.fragmentPrograms.get(featureHash));
        GL20.glAttachShader(shaderProgram, disposalAction.vertexPrograms.get(featureHash));
        if (shaderProgramBase.getGeometryProgram() != null) {
            GL20.glAttachShader(shaderProgram, disposalAction.geometryPrograms.get(featureHash));
        }
        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);
        return shaderProgram;
    }

    @Override
    public void recompile() {
        graphicsProcessing.asynchToDisplayThread(() -> {
            registerAllShaderPermutations();
        });
    }


    @Override
    public ShaderParameterMetadata getParameter(String desc) {
        return parameters.get(desc);
    }

    @Override
    public Iterable<ShaderParameterMetadata> listParameters() {
        return parameters.values();
    }

    private StringBuilder createShaderBuilder() {
        String preProcessorPreamble = "";

        // TODO: Implement a system for this - this has gotten way out of hand.
        WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
        if (worldAtlas != null) {
            preProcessorPreamble += "#define TEXTURE_OFFSET " + worldAtlas.getRelativeTileSize() + "\n";
        } else {
            preProcessorPreamble += "#define TEXTURE_OFFSET 0.06125\n";
        }
        RenderingConfig renderConfig = config.getRendering();

        preProcessorPreamble += "#define BLOCK_LIGHT_POW " + WorldRenderer.BLOCK_LIGHT_POW + "\n";
        preProcessorPreamble += "#define BLOCK_LIGHT_SUN_POW " + WorldRenderer.BLOCK_LIGHT_SUN_POW + "\n";
        preProcessorPreamble += "#define BLOCK_INTENSITY_FACTOR " + WorldRenderer.BLOCK_INTENSITY_FACTOR + "\n";
        preProcessorPreamble += "#define SHADOW_MAP_RESOLUTION " + (float) renderConfig.getShadowMapResolution() + "\n";
        preProcessorPreamble += "#define SSAO_KERNEL_ELEMENTS " + ssaoKernelElements + "\n";
        preProcessorPreamble += "#define SSAO_NOISE_SIZE " + ssaoNoiseSize + "\n";
        // TODO: This shouldn't be hardcoded
        preProcessorPreamble += "#define TEXTURE_OFFSET_EFFECTS " + 0.0625f + "\n";

        StringBuilder builder = new StringBuilder().append(preProcessorPreamble);
        if (renderConfig.isVolumetricFog()) {
            builder.append("#define VOLUMETRIC_FOG \n");
        }

        if (renderConfig.isAnimateGrass()) {
            builder.append("#define ANIMATED_GRASS \n");
        }
        if (renderConfig.isAnimateWater()) {
            builder.append("#define ANIMATED_WATER \n");
        }
        if (renderConfig.getBlurIntensity() == 0) {
            builder.append("#define NO_BLUR \n");
        }
        if (renderConfig.isFlickeringLight()) {
            builder.append("#define FLICKERING_LIGHT \n");
        }
        if (renderConfig.isVignette()) {
            builder.append("#define VIGNETTE \n");
        }
        if (renderConfig.isBloom()) {
            builder.append("#define BLOOM \n");
        }
        if (renderConfig.isMotionBlur()) {
            builder.append("#define MOTION_BLUR \n");
        }
        if (renderConfig.isSsao()) {
            builder.append("#define SSAO \n");
        }
        if (renderConfig.isFilmGrain()) {
            builder.append("#define FILM_GRAIN \n");
        }
        if (renderConfig.isOutline()) {
            builder.append("#define OUTLINE \n");
        }
        if (renderConfig.isLightShafts()) {
            builder.append("#define LIGHT_SHAFTS \n");
        }
        if (renderConfig.isDynamicShadows()) {
            builder.append("#define DYNAMIC_SHADOWS \n");
        }
        if (renderConfig.isNormalMapping()) {
            builder.append("#define NORMAL_MAPPING \n");
        }
        if (renderConfig.isParallaxMapping()) {
            builder.append("#define PARALLAX_MAPPING \n");
        }
        if (renderConfig.isDynamicShadowsPcfFiltering()) {
            builder.append("#define DYNAMIC_SHADOWS_PCF \n");
        }
        if (renderConfig.isCloudShadows()) {
            builder.append("#define CLOUD_SHADOWS \n");
        }
        if (renderConfig.isLocalReflections()) {
            builder.append("#define LOCAL_REFLECTIONS \n");
        }
        if (renderConfig.isInscattering()) {
            builder.append("#define INSCATTERING \n");
        }
        // TODO A 3D wizard should take a look at this. Configurable for the moment to make better comparisons possible.
        if (renderConfig.isClampLighting()) {
            builder.append("#define CLAMP_LIGHTING \n");
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
                logger.debug("Fragment shader feature '{}' is available...", feature);
                availableFeatures.add(feature);
            } else if (shaderProgramBase.getVertexProgram().contains(feature.toString())) {
                logger.debug("Vertex shader feature '{}' is available...", feature);
                availableFeatures.add(feature);
            }
        }
    }

    /**
     * Compiles all combination of available features and stores them in two maps for
     * lookup based on a unique hash of features.
     */
    private void registerAllShaderPermutations() {
        Set<Set<ShaderProgramFeature>> allPermutations = Sets.powerSet(availableFeatures);

        for (Set<ShaderProgramFeature> permutation : allPermutations) {
            int featureHash = ShaderProgramFeature.getBitset(permutation);

            int fragShaderId = compileShader(GL20.GL_FRAGMENT_SHADER, permutation);
            int vertShaderId = compileShader(GL20.GL_VERTEX_SHADER, permutation);
            if (shaderProgramBase.getGeometryProgram() != null) {
                int geomShaderId = compileShader(GL32.GL_GEOMETRY_SHADER, permutation);
                disposalAction.geometryPrograms.put(featureHash, geomShaderId);
            }

            disposalAction.fragmentPrograms.put(featureHash, fragShaderId);
            disposalAction.vertexPrograms.put(featureHash, vertShaderId);
        }

        logger.debug("Compiled {} permutations for {}.", allPermutations.size(), getUrn()); //NOPMD
    }

    private String assembleShader(int type, Set<ShaderProgramFeature> features) {
        StringBuilder shader = createShaderBuilder();

        // Add the activated features for this shader
        for (ShaderProgramFeature feature : features) {
            shader.append("#define ").append(feature.name()).append("\n");
        }

        shader.append("\n");

        shader.append(includedDefines);
        shader.append(includedUniforms);

        if (type == GL20.GL_FRAGMENT_SHADER) {
            shader.insert(0, "#version " + shaderProgramBase.getFragmentProgramVersion() + "\n");
            shader.append(includedFunctionsFragment);
            shader.append("\n");
            shader.append(shaderProgramBase.getFragmentProgram());
        } else if (type == GL32.GL_GEOMETRY_SHADER) {
            shader.insert(0, "#version " + shaderProgramBase.getGeometryProgramVersion() + "\n");
            shader.append(shaderProgramBase.getGeometryProgram());
        } else {
            shader.insert(0, "#version " + shaderProgramBase.getVertexProgramVersion() + "\n");
            shader.append(includedFunctionsVertex);
            shader.append("\n");
            shader.append(shaderProgramBase.getVertexProgram());
        }

        return shader.toString();
    }

    private void dumpCode(int type, Set<ShaderProgramFeature> features, String sourceCode) {
        String debugShaderType = "UNKNOWN";
        int featureHash = ShaderProgramFeature.getBitset(features);
        if (type == GL20.GL_FRAGMENT_SHADER) {
            debugShaderType = "FRAGMENT";
        } else if (type == GL20.GL_VERTEX_SHADER) {
            debugShaderType = "VERTEX";
        }

        // Dump all final shader sources to the log directory
        final String strippedTitle = getUrn().toString().replace(":", "-");

        // example: fragment_shader-engine-font_0.glsl
        String fname = debugShaderType.toLowerCase() + "_" + strippedTitle + "_" + featureHash + ".glsl";
        Path path = PathManager.getInstance().getShaderLogPath().resolve(fname);
        logger.info("Dumped Shader Path: {}", path);
        try (BufferedWriter writer = Files.newBufferedWriter(path, TerasologyConstants.CHARSET)) {
            writer.write(sourceCode);
        } catch (IOException e) {
            logger.error("Failed to dump shader source.");
        }
    }

    private int compileShader(int type, Set<ShaderProgramFeature> features) {
        int shaderId = GL20.glCreateShader(type);

        String shader = assembleShader(type, features);

        if (config.getRendering().isDumpShaders()) {
            dumpCode(type, features, shader);
        }

        GL20.glShaderSource(shaderId, shader);
        GL20.glCompileShader(shaderId);

        int[] compileStatus = new int[1];
        GL30.glGetShaderiv(shaderId, GL30.GL_COMPILE_STATUS, compileStatus);
        if (compileStatus[0] == GL33.GL_FALSE) {
            dumpCode(type, features, assembleShader(type, features));

            throw new RuntimeException(String.format("Shader '%s' failed to compile for features '%s'.%n%n"
                            + "Shader Info: %n%s%n",
                    getUrn(), features, GL30.glGetShaderInfoLog(shaderId)));
        }

        return shaderId;
    }

    @Override
    protected void doReload(ShaderData data) {
        try {
            GameThread.synch(() -> {
                logger.debug("Recompiling shader {}.", getUrn()); //NOPMD

                disposalAction.disposeData();
                shaderProgramBase = data;
                parameters.clear();
                for (ShaderParameterMetadata metadata : shaderProgramBase.getParameterMetadata()) {
                    parameters.put(metadata.getName(), metadata);
                }
                updateAvailableFeatures();
                try {
                    registerAllShaderPermutations();
                } catch (RuntimeException e) {
                    logger.warn("{}", e.getMessage()); //NOPMD
                }
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e); //NOPMD
        }
    }

    public static class DisposalAction implements DisposableResource {

        private final ResourceUrn urn;
        private final LwjglGraphicsProcessing graphicsProcessing;

        private final TIntIntMap fragmentPrograms = new TIntIntHashMap();
        private final TIntIntMap vertexPrograms = new TIntIntHashMap();
        private final TIntIntMap geometryPrograms = new TIntIntHashMap();

        // made package-private after CheckStyle's suggestion
        public DisposalAction(ResourceUrn urn, LwjglGraphicsProcessing graphicsProcessing) {
            this.urn = urn;
            this.graphicsProcessing = graphicsProcessing;
        }

        private void disposeData() {
            disposePrograms(fragmentPrograms);
            disposePrograms(vertexPrograms);
            disposePrograms(geometryPrograms);
        }

        private void disposePrograms(TIntIntMap programs) {
            final TIntIntMap disposedPrograms = new TIntIntHashMap(programs);
            graphicsProcessing.asynchToDisplayThread(() -> {
                TIntIntIterator it = disposedPrograms.iterator();
                while (it.hasNext()) {
                    it.advance();
                    GL20.glDeleteShader(it.value());
                }
            });
            programs.clear();
        }

        @Override
        public void close() {
            logger.debug("Disposing shader {}.", urn);
            try {
                GameThread.synch(this::disposeData);
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
