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

import org.terasology.gestalt.assets.AssetFactory;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

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
