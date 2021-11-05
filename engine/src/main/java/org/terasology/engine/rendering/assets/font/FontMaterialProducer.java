// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.material.MaterialData;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.gestalt.naming.Name;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RegisterAssetDataProducer
public class FontMaterialProducer implements AssetDataProducer<MaterialData> {

    private static final Logger logger = LoggerFactory.getLogger(FontMaterialProducer.class);

    private static final Name RESOURCE_NAME = new Name("font");
    private static final ResourceUrn FONT_SHADER_URN = new ResourceUrn("engine", "font");

    private AssetManager assetManager;

    public FontMaterialProducer(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        return Collections.emptySet();
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<MaterialData> getAssetData(ResourceUrn urn) throws IOException {
        if (RESOURCE_NAME.equals(urn.getResourceName()) && !urn.getFragmentName().isEmpty()) {
            Optional<? extends Shader> fontShader = assetManager.getAsset(FONT_SHADER_URN, Shader.class);
            if (!fontShader.isPresent()) {
                logger.error("Unable to resolve font shader");
                return Optional.empty();
            }

            Optional<Texture> texture = assetManager.getAsset(new ResourceUrn(urn.getModuleName(), urn.getFragmentName()), Texture.class);
            if (texture.isPresent()) {
                MaterialData materialData = new MaterialData(fontShader.get());
                materialData.setParam("tex", texture.get());
                return Optional.of(materialData);
            }
        }
        return Optional.empty();
    }
}
