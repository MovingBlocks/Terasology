// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import com.google.common.collect.ImmutableSet;
import org.terasology.assets.AssetDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.engine.core.TerasologyConstants;
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
