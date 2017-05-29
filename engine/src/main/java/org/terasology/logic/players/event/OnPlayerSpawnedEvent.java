/*
 * Copyright 2013 MovingBlocks
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
 * This event gets sent when the player spawns.
 * <br/>
 * <b>Note:</b> that this should be used only as a one time event i.e. when
 * the player spawns for the first time in the game.
 * On every subsequent spawn a onPlayerRespawnedEvent is sent.
 */
public class OnPlayerSpawnedEvent implements Event {
}
