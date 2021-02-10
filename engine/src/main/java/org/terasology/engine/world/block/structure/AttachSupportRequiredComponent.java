/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.structure;

import org.terasology.entitySystem.Component;

/**
 * Component for block entities that are attached to another block and depend on this block allowing to have been
 * attached to. The side it can be attached to is variable (i.e. torch attaches to 4 sides or bottom).
 *
 * If the supporting block is removed or changed to one that does not support attachment, the block
 * (attachment) will be destroyed. The check for which side this block entity is attached to is done via BlockMeshPart.
 *
 */
public class AttachSupportRequiredComponent implements Component {
}
