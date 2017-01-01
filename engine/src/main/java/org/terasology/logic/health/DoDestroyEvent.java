/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.health;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * The Event for Destruction
 */
public class DoDestroyEvent implements Event {
    private EntityRef instigator;
    private EntityRef directCause;
    private Prefab damageType;
    
    /**
     * Constructor of the DoDestroyEvent object
     * 
     * @param instigator     The object which is the instigator of the DoDestroyEvent
     * @param directCause    The object which is the direct cause of the DoDestroyEvent
     * @param damageType    The damage type of the DoDestroyEvent
    */
    public DoDestroyEvent(EntityRef instigator, EntityRef directCause, Prefab damageType) {
        this.instigator = instigator;
        this.directCause = directCause;
        this.damageType = damageType;
    }
    
    /**
     * Returns the instigator data of the DoDestroyEvent object
     * 
     * @return an EntityRef object which is the instigator of the destruction
    */
    public EntityRef getInstigator() {
        return instigator;
    }
    
    /**
     * Returns the instigator data of the DoDestroyEvent object
     * 
     * @return an EntityRef object which is the direct cause of the destruction
     */
    public EntityRef getDirectCause() {
        return directCause;
    }
    
    /**
     * Returns the prefab of the damage type used by the DoDestroyEvent object
     * 
     * @return a Prefab of the damage type used by the DoDestroyEvent object
     */
    public Prefab getDamageType() {
        return damageType;
    }
}
