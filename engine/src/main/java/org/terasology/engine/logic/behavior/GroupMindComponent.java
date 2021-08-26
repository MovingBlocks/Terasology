// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * This component  complements GroupTagComponent
 * in cases where there's the need of an unison
 * behavior by all entities belonging to a group.
 * @see GroupTagComponent
 */
public class GroupMindComponent implements Component<GroupMindComponent> {
    /**
     * The unique group identifier with which the component will be associated
     */
    public String groupLabel;
    /**
     * The name of the behavior tree used by the group (trees from other modules
     * can be used as long as they are listed as dependencies).
     */
    public String behavior;
    /**
     * The identifiers for each of the entities belonging to the group.
     */
    public Set<EntityRef> groupMembers = new HashSet<>();

    @Override
    public void copyFrom(GroupMindComponent other) {
        this.groupLabel = other.groupLabel;
        this.behavior = other.behavior;
        this.groupMembers = Sets.newHashSet(other.groupMembers);
    }
}
