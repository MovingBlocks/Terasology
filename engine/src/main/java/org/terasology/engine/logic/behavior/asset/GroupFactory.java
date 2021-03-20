// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.assets.AssetFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 * Factory class used to assist the creation
 * of new Group assets.
 * @see Group
 */
public class GroupFactory implements AssetFactory<Group, GroupData> {

    @Override
    public Group build(ResourceUrn urn, AssetType<Group, GroupData> type, GroupData data) {
        return new Group(urn, data, type);
    }
}
