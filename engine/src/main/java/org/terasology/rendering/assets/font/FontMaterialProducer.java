/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.assets.font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.gestalt.naming.Name;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 */
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
                materialData.setParam("texture", texture.get());
                return Optional.of(materialData);
            }
        }
        return Optional.empty();
    }
}
