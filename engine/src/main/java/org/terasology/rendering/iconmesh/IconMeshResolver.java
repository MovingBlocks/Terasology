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

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetResolver;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.texture.TextureRegion;

/**
 * @author Immortius
 */
public class IconMeshResolver implements AssetResolver<Mesh, MeshData> {

    public static final String ICON_DISCRIMINATOR = "icon";

    @Override
    public AssetUri resolve(String partialUri) {
        String[] parts = partialUri.split("\\.", 2);
        if (parts.length == 2 && parts[0].equals(ICON_DISCRIMINATOR)) {
            AssetUri uri = Assets.resolveAssetUri(AssetType.TEXTURE, parts[1]);
            if (uri == null) {
                uri = Assets.resolveAssetUri(AssetType.SUBTEXTURE, parts[1]);
            }
            if (uri != null) {
                return new AssetUri(AssetType.MESH, uri.getModuleName(), partialUri);
            }
        }
        return null;
    }

    @Override
    public Mesh resolve(AssetUri uri, AssetFactory<MeshData, Mesh> factory) {
        String[] parts = uri.getAssetName().split("\\.", 2);
        if (parts.length == 2 && parts[0].equals(ICON_DISCRIMINATOR)) {
            TextureRegion region = Assets.getTextureRegion(uri.getModuleName() + AssetUri.MODULE_SEPARATOR + parts[1]);
            if (region != null) {
                return IconMeshFactory.generateIconMesh(uri, region);
            }
        }
        return null;
    }
}
