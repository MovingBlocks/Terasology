// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.terasology.gestalt.assets.AssetData;

/**
 * Every Group asset is described by a GroupData class.
 * @see Group
 */
public class GroupData implements AssetData {

    /**
     * The unique group identifier
     */
    public String groupLabel;

    /**
     * Flags the need for a hivemind
     * structure (when group members
     * need to behave in unison)
     */
    public Boolean needsHive;

    /**
     * The name of the behavior tree
     * used by the group (trees
     * from other modules can be used as
     * long as they are listed as
     * dependencies)
     */
    public String behavior;

    public GroupData() {
    }

    public GroupData(String groupLabel, Boolean needsHive, String behavior) {
        this.groupLabel = groupLabel;
        this.needsHive = needsHive;
        this.behavior = behavior;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public Boolean getNeedsHive() {
        return needsHive;
    }

    public void setNeedsHive(Boolean needsHive) {
        this.needsHive = needsHive;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

}
