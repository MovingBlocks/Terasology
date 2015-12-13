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
import org.terasology.rendering.nui.Color;

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
