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
package org.terasology.rendering.assets.texture;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.terasology.gestalt.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetSupplementalFileFormat;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;

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
