/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.modes.loadProcesses;

import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.gestalt.module.sandbox.API;

/**
 * Event which is triggered when LocalPlayer is setup with a character entity. Allows for detection of when LocalPlayer is
 * completely setup for a character, in case a system needs to wait until it is setup and therefore cannot act in {@link BaseComponentSystem#postBegin()}
 * Event is sent to the character entity. This only triggers at the setup of the local player(once per in game session). It is sent by
 * {@link AwaitCharacterSpawn}
 *
 * API annotation is to allow modules to utilize this event as well.
 */
@API
public class AwaitedLocalCharacterSpawnEvent implements Event {
}
