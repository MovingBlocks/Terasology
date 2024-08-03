// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering;

import com.google.common.collect.Sets;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.material.MaterialData;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.opengl.GLSLMaterial;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides support for loading and applying shaders.
 */
public class ShaderManagerLwjgl implements ShaderManager {
    private static final Logger logger = LoggerFactory.getLogger(ShaderManagerLwjgl.class);

    private GLSLMaterial activeMaterial;

    private Set<GLSLMaterial> progamaticShaders = Sets.newHashSet();

    public ShaderManagerLwjgl() {
    }

    @Override
    public void initShaders() {
        logCapabilities();
        addShaderProgram("default");
        addShaderProgram("blockSelection");
        addShaderProgram("particle");
    }

    @SuppressWarnings("PMD.GuardLogStatement")
    private void logCapabilities() {
        logger.info("Loading Terasology shader manager...");
        logger.info("LWJGL: {} / {}", Version.getVersion(), Platform.get().getName());
        logger.info("GL_VENDOR: {}", GL11.glGetString(GL11.GL_VENDOR));
        logger.info("GL_RENDERER: {}", GL11.glGetString(GL11.GL_RENDERER));
        logger.info("GL_VERSION: {}", GL11.glGetString(GL11.GL_VERSION));
        logger.info("SHADING_LANGUAGE VERSION: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));

        int[] extension = new int[1];
        GL30.glGetIntegerv(GL30.GL_NUM_EXTENSIONS, extension);

        // log shader extensions in smaller packages,
        // because the full string can be extremely long
        int extsPerLine = 8;

        // starting with OpenGL 3.0, extensions can also listed using
        // GL_NUM_EXTENSIONS and glGetStringi(GL_EXTENSIONS, idx)
        if (extension[0] > 0) {
            StringBuilder bldr = new StringBuilder();
            for (int i = 1; i < extension[0]; i++) {
                if (i % extsPerLine == 0) {
                    logger.info("EXTENSIONS: {}", bldr);
                    bldr.setLength(0);
                } else {
                    bldr.append(" ");
                }
                bldr.append(GL30.glGetStringi(GL30.GL_EXTENSIONS, i));
            }
            if (bldr.length() > 0) {
                logger.info("EXTENSIONS: {}", bldr);
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
        GLSLMaterial material =
                (GLSLMaterial) Assets.generateAsset(new ResourceUrn(providingModule + ":prog." + title),
                        new MaterialData(shader.get()), Material.class);
        progamaticShaders.add(material);

        return material;
    }

    @Override
    public void disableShader() {
        GL20.glUseProgram(0);
        activeMaterial = null;
    }
}
