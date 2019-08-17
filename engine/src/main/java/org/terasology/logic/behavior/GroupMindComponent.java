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
import org.terasology.entitySystem.entity.EntityRef;

import java.util.HashSet;
import java.util.Set;

/**
 * This component  complements GroupTagComponent
 * in cases where there's the need of an unison
 * behavior by all entities belonging to a group.
 * @see GroupTagComponent
 */
public class GroupMindComponent implements Component {
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
}
