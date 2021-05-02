// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

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
