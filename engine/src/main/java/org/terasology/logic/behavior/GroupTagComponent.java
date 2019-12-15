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
package org.terasology.logic.behavior;

import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.Interpreter;
import org.terasology.logic.behavior.asset.BehaviorTree;

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
public class GroupTagComponent implements Component {

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
}
