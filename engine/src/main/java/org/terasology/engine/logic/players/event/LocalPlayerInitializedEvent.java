/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.players.event;

import org.terasology.entitySystem.event.Event;

/**
 * This event gets sent when the {@link org.terasology.logic.players.LocalPlayer} object is ready to be used.
 * <br/>
 * The object can be injected using {@link org.terasology.registry.In}.
 * This event corresponds with its isValid() method returning true for the first time.
 */
public class LocalPlayerInitializedEvent implements Event {
}
