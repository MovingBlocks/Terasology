/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.asset;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetDeltaFileFormat;
import org.terasology.nui.asset.UIData;
import org.terasology.nui.skin.UISkin;
import org.terasology.utilities.Assets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Enables UI screens (.ui) to have delta files.
 * The delta files share the same extension as UI screens.
 *
 * Note that in the current form, only skinning is supported via delta files.
 * Further, the skin you are overriding the existing skin with should reside in the same module as the delta file.
*/
@RegisterAssetDeltaFileFormat
public class UIDeltaFormat extends AbstractAssetAlterationFileFormat<UIData> {
    private static final Logger logger = LoggerFactory.getLogger(UIDeltaFormat.class);

    public UIDeltaFormat() {
        super("ui");
    }

    @Override
    public void apply(AssetDataFile input, UIData assetData) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(input.openStream(), Charsets.UTF_8))) {
            JsonElement jsonElement = new JsonParser().parse(reader);

            String skinUri = jsonElement.getAsJsonObject().get("skin").getAsString();
            String filePath = input.toString();
            String moduleName = filePath.substring(1, filePath.indexOf('/', 1));

            Optional<UISkin> skin = Assets.getSkin(moduleName + ":" + skinUri);
            if (skin.isPresent()) {
                assetData.getRootWidget().setSkin(skin.get());
            } else {
                logger.warn("Failed to load skin " + skinUri + " for the delta file " + input.getFilename());
            }
        }
    }
}
