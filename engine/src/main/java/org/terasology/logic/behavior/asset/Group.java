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
