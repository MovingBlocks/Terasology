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
package org.terasology.rendering.iconmesh;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.gestalt.naming.Name;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 */
@RegisterAssetDataProducer
public class IconMeshDataProducer implements AssetDataProducer<MeshData> {

    public static final Name ICON_DISCRIMINATOR = new Name("Icon");

    private AssetManager assetManager;

    public IconMeshDataProducer(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (ICON_DISCRIMINATOR.equals(resourceName)) {
            return ImmutableSet.copyOf(Collections2.transform(assetManager.resolve(resourceName.toString(), TextureRegionAsset.class),
                    new Function<ResourceUrn, Name>() {
                        @Nullable
                        @Override
                        public Name apply(ResourceUrn input) {
                            return input.getModuleName();
                        }
                    }));
        }
        return Collections.emptySet();
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<MeshData> getAssetData(ResourceUrn urn) throws IOException {
        if (ICON_DISCRIMINATOR.equals(urn.getResourceName())) {
            ResourceUrn textureUrn = new ResourceUrn(urn.getModuleName().toString() + ResourceUrn.RESOURCE_SEPARATOR + urn.getFragmentName().toString());
            Optional<TextureRegionAsset> textureRegionAsset = assetManager.getAsset(textureUrn, TextureRegionAsset.class);
            if (textureRegionAsset.isPresent() && !textureRegionAsset.get().getTexture().isDisposed()) {
                return Optional.of(IconMeshFactory.generateIconMeshData(textureRegionAsset.get()));
            }
        }
        return Optional.empty();
    }

}
