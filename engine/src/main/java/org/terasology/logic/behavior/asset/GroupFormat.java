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
import java.util.List;

/**
 * This class is responsible for
 * associating the correct file extensions
 * (.group) with the Group asset class.
 * It is also the actual responsible
 * for invoking the serialization method
 * from the builder class.
 * @see Group
 * @see GroupBuilder
 */
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

        return builder.loadFromJson(list.get(0).openStream());
    }
}
