// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 * Event which is triggered when LocalPlayer is setup with a character entity. Allows for detection of when LocalPlayer is
 * completely setup for a character, in case a system needs to wait until it is setup and therefore cannot act
 * an {@link BaseComponentSystem#postBegin()} Event is sent to the character entity.
 * This only triggers at the setup of the local player(once per in game session). It is sent by
 * {@link AwaitCharacterSpawn}
 *
 * API annotation is to allow modules to utilize this event as well.
 */
@API
public class AwaitedLocalCharacterSpawnEvent implements Event {
}
