// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.terasology.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetSupplementalFileFormat;
import org.terasology.engine.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 */
@RegisterAssetSupplementalFileFormat
public class TextureInfoFormat extends AbstractAssetAlterationFileFormat<TextureData> {

    private Gson gson;

    public TextureInfoFormat() {
        super("texinfo");
        gson = new GsonBuilder().registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory()).create();
    }

    @Override
    public void apply(AssetDataFile input, TextureData assetData) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input.openStream(), Charsets.UTF_8)) {
            TextureMetadata metadata = gson.fromJson(reader, TextureMetadata.class);
            if (metadata.filterMode != null) {
                assetData.setFilterMode(metadata.filterMode);
            }
            if (metadata.wrapMode != null) {
                assetData.setWrapMode(metadata.wrapMode);
            }
            if (metadata.type != null) {
                assetData.setType(metadata.type);
            }
        }
    }

    private static class TextureMetadata {
        Texture.FilterMode filterMode;
        Texture.WrapMode wrapMode;
        Texture.Type type;
    }
}
