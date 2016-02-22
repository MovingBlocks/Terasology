/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * This event happens on the server after a AttackRequest has been sent by the client.
 * This event is sent on the entity being attacked.
 */
public class AttackEvent extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef directCause;

    /**
     * @param instigator  The instigator of the damage (which entity caused it)
     * @param directCause Tool used to cause the damage
     */
    public AttackEvent(EntityRef instigator, EntityRef directCause) {
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}
