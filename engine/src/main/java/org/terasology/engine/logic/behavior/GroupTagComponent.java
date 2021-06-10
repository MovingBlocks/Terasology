// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import com.google.common.collect.Lists;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Every group member has the GroupTag component.
 * This component is used by the collective behavior
 * system to identify pre-defined groups and control
 * its entities. Using a tag for group members allow
 * easy identification/addition/removal of grouped
 * entities.
 */
public class GroupTagComponent implements Component<GroupTagComponent> {

    /**
     * Groups to which the entity belong.
     * This is necessary since a single entity can
     * belong to multiple groups. This list will
     * primarily be used to solve conflicting behaviors
     * triggered by multiple groups. Also, since groups
     * should support roles, this is where an entity can
     * have its roles updated/checked by the collective
     * behavior system.
     * TODO: associate roles with groups.
     */
    public List<String> groups = new ArrayList<String>();

    /**
     * Backup fields are used to preserve the original
     * entity state before joining a group.
     * TODO: assess need for a parametrized matrix
     */
    public BehaviorTree backupBT;

    public Interpreter backupRunningState;

    @Override
    public void copy(GroupTagComponent other) {
        this.groups = Lists.newArrayList(other.groups);
        this.backupBT = other.backupBT;
        this.backupRunningState = other.backupRunningState;
    }
}
