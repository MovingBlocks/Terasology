// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.annotations.RegisterAssetType;

/**
 * The main Group asset class. This is the reference type
 * used by the asset manager to find all groups loaded
 * from .group files. The annotation below indicates that
 * all .group files will be under the assets/groups folder.
 * A factory class is used to assist the asset creation.
 * Using the Group asset is illustrated in the
 * WildAnimalsMadness module.
 */
@RegisterAssetType(folderName = "groups", factoryClass = GroupFactory.class)
public class Group extends Asset<GroupData> {

    private GroupData groupData;

    public Group(ResourceUrn urn, GroupData data, AssetType<?, GroupData> type) {
        super(urn, type);
        reload(data);
    }

    @Override
    protected void doReload(GroupData data) {
        this.groupData = data;
    }

    public GroupData getGroupData() {
        return groupData;
    }
}
