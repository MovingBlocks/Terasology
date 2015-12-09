/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.assets.AssetDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.engine.TerasologyConstants;
import org.terasology.naming.Name;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves references to <code>engine:noise</code> texture assets,
 * <br><br>
 * The noise parameters are parsed from the name of the asset, then TextureDataFactory is used to create
 * a TextureData object which is used to build the Texture.
 *
 */
@RegisterAssetDataProducer
public class NoiseTextureProducer implements AssetDataProducer<TextureData> {

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (TextureUtil.NOISE_RESOURCE_NAME.equals(resourceName)) {
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
        if (TerasologyConstants.ENGINE_MODULE.equals(urn.getModuleName()) && TextureUtil.NOISE_RESOURCE_NAME.equals(urn.getResourceName())) {
            Name fragmentName = urn.getFragmentName();
            if (!fragmentName.isEmpty()) {
                String[] parts = fragmentName.toLowerCase().split("\\.");
                if (parts.length == 5) {
                    String type = parts[0];
                    int size = Integer.parseInt(parts[1]);
                    long seed = Long.parseLong(parts[2]);
                    int min = Integer.parseInt(parts[3]);
                    int max = Integer.parseInt(parts[4]);
                    TextureData textureData;
                    switch (type) {
                        case "white":
                            return Optional.of(TextureDataFactory.createWhiteNoiseTexture(size, seed, min, max));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
