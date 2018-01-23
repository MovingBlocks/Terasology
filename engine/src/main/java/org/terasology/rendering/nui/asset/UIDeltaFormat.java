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
import org.terasology.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetDeltaFileFormat;
import org.terasology.utilities.Assets;

import java.io.IOException;
import java.io.InputStreamReader;

@RegisterAssetDeltaFileFormat
public class UIDeltaFormat extends AbstractAssetAlterationFileFormat<UIData> {
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

            assetData.getRootWidget().setSkin(Assets.getSkin(moduleName + ":" + skinUri).get());
        }
    }
}
