// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import com.google.common.collect.ImmutableSet;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.Color;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves references to engine:color#RRGGBBAA texture assets,
 * where RR is the red hex value in lowercase,
 * and GG, BB, and AA are green, blue, and alpha, respectively.
 * <br><br>
 * The color is parsed from the name of the asset, then TextureDataFactory is used to create
 * a TextureData object which is used to build the Texture.
 *
 */
@RegisterAssetDataProducer
public class ColorTextureProducer implements AssetDataProducer<TextureData> {

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (TextureUtil.COLOR_RESOURCE_NAME.equals(resourceName)) {
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
        if (TerasologyConstants.ENGINE_MODULE.equals(urn.getModuleName()) && TextureUtil.COLOR_RESOURCE_NAME.equals(urn.getResourceName())) {
            Name fragmentName = urn.getFragmentName();
            if (!fragmentName.isEmpty()) {
                Color color = TextureUtil.getColorForColorName(fragmentName.toLowerCase());
                return Optional.of(TextureDataFactory.newInstance(color));
            }
        }
        return Optional.empty();
    }
}
