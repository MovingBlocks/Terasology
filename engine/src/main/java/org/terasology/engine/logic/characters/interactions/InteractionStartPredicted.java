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
package org.terasology.logic.characters.interactions;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.module.sandbox.API;

/**
 * Sent to the client by itself at the start of an interaction between a character and a target.
 * <br><br>
 * THe event is sent to the target entity.
 * <br><br>
 * This event is not intended to be sent by modules.
 * <br><br>
 * When event handler runs, the  predictedInteractionTarget field of the instigator's
 * CharacterComponent will already be updated to the new value.
 *
 */
@API
public class InteractionStartPredicted implements Event {
    private EntityRef instigator;

    protected InteractionStartPredicted() {
    }

    public InteractionStartPredicted(EntityRef instigator) {
        this.instigator = instigator;
    }

    /**
     * @return he character which started the interaction.
     */
    public EntityRef getInstigator() {
        return instigator;
    }
}
