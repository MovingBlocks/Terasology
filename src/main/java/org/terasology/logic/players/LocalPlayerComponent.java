/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.players;

import org.terasology.entitySystem.Component;
import org.terasology.network.NoReplicate;

/**
 * @author Immortius <immortius@gmail.com>
 */
@NoReplicate
public final class LocalPlayerComponent implements Component {
    // View Direction should be in another component, possible Creature?
    public float viewYaw = 0;
    public float viewPitch = 0;


    // Should this be in another component? Player probably.
    public boolean isDead = false;
    public float respawnWait = 0;

    // Should be here I think (only the local player needs to know the slot),
    // but equipped item will need to be reflected elsewhere so it can
    // be replicated to all players
    public int selectedTool = 0;
    public float handAnimation = 0;
}
