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
package org.terasology.rendering;

import com.google.common.collect.Sets;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.shader.ShaderParameters;
import org.terasology.rendering.shader.ShaderParametersBlock;
import org.terasology.rendering.shader.ShaderParametersChunk;
import org.terasology.rendering.shader.ShaderParametersCombine;
import org.terasology.rendering.shader.ShaderParametersDebug;
import org.terasology.rendering.shader.ShaderParametersDefault;
import org.terasology.rendering.shader.ShaderParametersHdr;
import org.terasology.rendering.shader.ShaderParametersLightBufferPass;
import org.terasology.rendering.shader.ShaderParametersLightGeometryPass;
import org.terasology.rendering.shader.ShaderParametersLightShaft;
import org.terasology.rendering.shader.ShaderParametersOcDistortion;
import org.terasology.rendering.shader.ShaderParametersParticle;
import org.terasology.rendering.shader.ShaderParametersPost;
import org.terasology.rendering.shader.ShaderParametersPrePost;
import org.terasology.rendering.shader.ShaderParametersSSAO;
import org.terasology.rendering.shader.ShaderParametersShadowMap;
import org.terasology.rendering.shader.ShaderParametersSky;
import org.terasology.rendering.shader.ShaderParametersSobel;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides support for loading and applying shaders.
 *
 */
public class ShaderManagerLwjgl implements ShaderManager {

    private static final Logger logger = LoggerFactory.getLogger(ShaderManagerLwjgl.class);

    private GLSLMaterial activeMaterial;
    private GLSLMaterial defaultShaderProgram;
    private GLSLMaterial defaultTexturedShaderProgram;

    private Set<GLSLMaterial> progamaticShaders = Sets.newHashSet();

    public ShaderManagerLwjgl() {
        logger.info("Loading Terasology shader manager...");
        logger.info("LWJGL: {} / {}", Sys.getVersion(), LWJGLUtil.getPlatformName());
        logger.info("GL_VENDOR: {}", GL11.glGetString(GL11.GL_VENDOR));
        logger.info("GL_RENDERER: {}", GL11.glGetString(GL11.GL_RENDERER));
        logger.info("GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("SHADING_LANGUAGE VERSION: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));

        String extStr = GL11.glGetString(GL11.GL_EXTENSIONS);

        // log shader extensions in smaller packages, 
        // because the full string can be extremely long 
        int extsPerLine = 8;

        // starting with OpenGL 3.0, extensions can also listed using
        // GL_NUM_EXTENSIONS and glGetStringi(GL_EXTENSIONS, idx)
        String[] exts = extStr.split(" ");
        if (exts.length > 0) {
            StringBuilder bldr = new StringBuilder(exts[0]);
            for (int i = 1; i < exts.length; i++) {
                if (i % extsPerLine == 0) {
                    logger.info("EXTENSIONS: {}", bldr.toString());
                    bldr.setLength(0);
                } else {
                    bldr.append(" ");
                }
                bldr.append(exts[i]);
            }
            if (bldr.length() > 0) {
                logger.info("EXTENSIONS: {}", bldr.toString());
            }
        }
    }

    @Override
    public void initShaders() {
        defaultShaderProgram = prepareAndStoreShaderProgramInstance("default", new ShaderParametersDefault());
        defaultTexturedShaderProgram = prepareAndStoreShaderProgramInstance("defaultTextured", new ShaderParametersDefault());

        // TODO: Find a better way to do this
        prepareAndStoreShaderProgramInstance("post", new ShaderParametersPost());
        prepareAndStoreShaderProgramInstance("ssao", new ShaderParametersSSAO());
        prepareAndStoreShaderProgramInstance("lightshaft", new ShaderParametersLightShaft());
        prepareAndStoreShaderProgramInstance("sobel", new ShaderParametersSobel());
        prepareAndStoreShaderProgramInstance("prePost", new ShaderParametersPrePost());
        prepareAndStoreShaderProgramInstance("combine", new ShaderParametersCombine());
        prepareAndStoreShaderProgramInstance("highp", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("blur", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("down", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("hdr", new ShaderParametersHdr());
        prepareAndStoreShaderProgramInstance("sky", new ShaderParametersSky());
        prepareAndStoreShaderProgramInstance("chunk", new ShaderParametersChunk());
        prepareAndStoreShaderProgramInstance("particle", new ShaderParametersParticle());
        prepareAndStoreShaderProgramInstance("block", new ShaderParametersBlock());
        prepareAndStoreShaderProgramInstance("shadowMap", new ShaderParametersShadowMap());
        prepareAndStoreShaderProgramInstance("debug", new ShaderParametersDebug());
        prepareAndStoreShaderProgramInstance("ocDistortion", new ShaderParametersOcDistortion());
        prepareAndStoreShaderProgramInstance("lightBufferPass", new ShaderParametersLightBufferPass());
        prepareAndStoreShaderProgramInstance("lightGeometryPass", new ShaderParametersLightGeometryPass());
        prepareAndStoreShaderProgramInstance("simple", new ShaderParametersDefault());
        prepareAndStoreShaderProgramInstance("ssaoBlur", new ShaderParametersDefault());
    }

    @Override
    public void setActiveMaterial(Material material) {
        // TODO: is this the best way to convert the material to the lwjgl version?  Do we need more checks?
        GLSLMaterial glslMaterial = (GLSLMaterial) material;
        if (!glslMaterial.equals(activeMaterial)) {
            activeMaterial = glslMaterial;
        }
    }

    @Override
    public void bindTexture(int slot, Texture texture) {
        if (activeMaterial != null && !activeMaterial.isDisposed()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
            // TODO: Need to be cubemap aware, only need to clear bind when switching from cubemap to 2D and vice versa,
            // TODO: Don't bind if already bound to the same
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }
    }

    @Override
    public Material getActiveMaterial() {
        return activeMaterial;
    }

    @Override
    public void recompileAllShaders() {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.getLoadedAssets(Shader.class).forEach(Shader::recompile);

        assetManager.getLoadedAssets(Material.class).forEach(Material::recompile);

        activeMaterial = null;
    }

    private GLSLMaterial prepareAndStoreShaderProgramInstance(String title, ShaderParameters params) {
        String uri = "engine:" + title;
        Optional<? extends Shader> shader = Assets.getShader(uri);
        checkState(shader.isPresent(), "Failed to resolve %s", uri);
        shader.get().recompile();
        GLSLMaterial material = (GLSLMaterial) Assets.generateAsset(new ResourceUrn("engine:prog." + title), new MaterialData(shader.get()), Material.class);
        material.setShaderParameters(params);
        progamaticShaders.add(material);

        return material;
    }

    /**
     * Enables the default shader program.
     */
    @Override
    public void enableDefault() {
        defaultShaderProgram.enable();
    }

    /**
     * Enables the default shader program.
     */
    @Override
    public void enableDefaultTextured() {
        defaultTexturedShaderProgram.enable();
    }

    @Override
    public void disableShader() {
        GL20.glUseProgram(0);
        activeMaterial = null;
    }

}
