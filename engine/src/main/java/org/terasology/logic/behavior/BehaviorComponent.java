/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.module.sandbox.API;

/**
 * Entities with this component are handled by a behavior tree. Default tree to fetch may be set.
 *
 */
@API
public class BehaviorComponent implements Component {
    public BehaviorTree tree;
}
