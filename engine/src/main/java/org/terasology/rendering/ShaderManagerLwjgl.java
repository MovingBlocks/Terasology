/*
 * Copyright 2017 MovingBlocks
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
import org.lwjgl.Version;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.Platform;
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

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides support for loading and applying shaders.
 */
public class ShaderManagerLwjgl implements ShaderManager {
    private static final Logger logger = LoggerFactory.getLogger(ShaderManagerLwjgl.class);

    private GLSLMaterial activeMaterial;
    private GLSLMaterial defaultShaderProgram;
    private GLSLMaterial defaultTexturedShaderProgram;

    private Set<GLSLMaterial> progamaticShaders = Sets.newHashSet();

    public ShaderManagerLwjgl() {
    }

    @Override
    public void initShaders() {
        logCapabilities();
        defaultShaderProgram = addShaderProgram("default");
        defaultTexturedShaderProgram = addShaderProgram("defaultTextured");

        // TODO: Find a better way to do this
        addShaderProgram("post");
        addShaderProgram("ssao");
        addShaderProgram("lightShafts");
        addShaderProgram("sobel");
        addShaderProgram("initialPost");
        addShaderProgram("prePostComposite");
        addShaderProgram("highPass");
        addShaderProgram("blur");
        addShaderProgram("downSampler");
        addShaderProgram("toneMapping");
        addShaderProgram("sky");
        addShaderProgram("chunk");
        if (GL.createCapabilities().OpenGL33) { //TODO remove this "if" when rendering will use OpenGL3 by default
            addShaderProgram("particle");
        } else {
            logger.warn("Your GPU or driver not supports OpenGL 3.3 , particles disabled");
        }
        addShaderProgram("shadowMap");
        addShaderProgram("lightBufferPass");
        addShaderProgram("lightGeometryPass");
        addShaderProgram("ssaoBlur");
    }

    private void logCapabilities() {
        logger.info("Loading Terasology shader manager...");
        logger.info("LWJGL: {} / {}", Version.getVersion(), Platform.get().getName());
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

    // TODO: discuss having a `public removeShaderProgram`, to dispose shader programs no longer in use by any node
    public GLSLMaterial addShaderProgram(String title) {
        return addShaderProgram(title, "engine");
    }

    public GLSLMaterial addShaderProgram(String title, String providingModule) {

        String uri = providingModule + ":" + title;
        Optional<? extends Shader> shader = Assets.getShader(uri);
        checkState(shader.isPresent(), "Failed to resolve %s", uri);
        shader.get().recompile();
        GLSLMaterial material = (GLSLMaterial) Assets.generateAsset(new ResourceUrn(providingModule + ":prog." + title), new MaterialData(shader.get()), Material.class);
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
