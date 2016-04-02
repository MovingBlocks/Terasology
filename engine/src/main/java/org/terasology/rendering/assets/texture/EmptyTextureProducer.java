/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.assets.texture;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.engine.TerasologyConstants;
import org.terasology.naming.Name;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RegisterAssetDataProducer
public class EmptyTextureProducer implements AssetDataProducer<TextureData> {

    private static final Logger logger = LoggerFactory.getLogger(EmptyTextureProducer.class);

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (TextureUtil.EMPTY_RESOURCE_NAME.equals(resourceName)) {
            return ImmutableSet.of(TerasologyConstants.ENGINE_MODULE);
        }
        return Collections.emptySet();
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<TextureData> getAssetData(ResourceUrn urn) throws IOException {
        if (TerasologyConstants.ENGINE_MODULE.equals(urn.getModuleName()) && TextureUtil.EMPTY_RESOURCE_NAME.equals(urn.getResourceName())) {
            Name fragmentName = urn.getFragmentName();
            if (!fragmentName.isEmpty()) {
                String[] parts = fragmentName.toLowerCase().split("\\.");
                if (parts.length == 3) {
                    int width = Integer.parseInt(parts[0]);
                    int height = Integer.parseInt(parts[1]);
                    return Optional.of(new TextureData(width,height, Texture.WrapMode.CLAMP, Texture.FilterMode.LINEAR));
                }
            }
        }
        logger.info("illegal texture urn:" + urn.toString());
        return Optional.empty();
    }
}
