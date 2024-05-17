// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.asset;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDeltaFileFormat;
import org.terasology.nui.asset.UIData;
import org.terasology.nui.skin.UISkin;
import org.terasology.engine.utilities.Assets;

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
                logger.atWarn().log("Failed to load skin {} for the delta file {}", skinUri, input.getFilename());
            }
        }
    }
}
