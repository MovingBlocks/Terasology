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
package org.terasology.logic.selection;

import org.terasology.module.sandbox.API;
import org.terasology.entitySystem.event.Event;

/**
 * This event should be sent by a system after it receives a {@link ApplyBlockSelectionEvent} which marks the end of a
 * region selection. This event marks the start of the binding of the camera position with the selected region.
 * <br>
 * The entity used to send this event must have the {@link org.terasology.world.selection.BlockSelectionComponent}
 */
@API
public class MovableSelectionStartEvent implements Event {
}
