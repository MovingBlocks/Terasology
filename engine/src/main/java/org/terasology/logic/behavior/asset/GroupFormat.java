/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.behavior.asset;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.registry.CoreRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RegisterAssetFileFormat
public class GroupFormat extends AbstractAssetFileFormat<GroupData> {

    public GroupFormat() {
        super("group");
    }

    @Override
    public GroupData load(ResourceUrn resourceUrn, List<AssetDataFile> list) throws IOException {
        GroupBuilder builder = CoreRegistry.get(GroupBuilder.class);
        if (builder == null) {
            builder = new GroupBuilder();
            CoreRegistry.put(GroupBuilder.class, builder);
        }
        try (InputStream stream = list.get(0).openStream()) {
            return builder.loadFromJson(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return new GroupData("ERROR",false,"none");
        }

    }
}
