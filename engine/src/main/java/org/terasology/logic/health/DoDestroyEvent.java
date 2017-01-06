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
 */
public class DoDestroyEvent implements Event {
    private EntityRef instigator;
    private EntityRef directCause;
    private Prefab damageType;
    private String instigatorString;
    private String damageTypeString;

    public DoDestroyEvent(EntityRef instigator, EntityRef directCause, Prefab damageType) {
        this.instigator = instigator;
        this.directCause = directCause;
        this.damageType = damageType;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public String getInstigatorString() {
        if (instigator == null)
            return "";
        if (instigator.getParentPrefab() != null) {
            instigatorString = instigator.getParentPrefab().getName();
            instigatorString = instigatorString.replaceAll("[A-Za-z]*:([A-Za-z]*)", "$1");
            instigatorString = instigatorString.replaceAll("([A-Z])", " $1");
            instigatorString = Character.toUpperCase(instigatorString.charAt(0)) + instigatorString.substring(1);
            return instigatorString;
        }
        else
            return "";
    }

    public EntityRef getDirectCause() {
        return directCause;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public String getDamageTypeString() {
        damageTypeString = damageType.getName().toString();
        damageTypeString = damageTypeString.replaceAll("[A-Za-z]*:([A-Za-z]*)", "$1");
        damageTypeString = damageTypeString.replaceAll("([A-Z])", " $1");
        damageTypeString = Character.toUpperCase(damageTypeString.charAt(0)) + damageTypeString.substring(1);
        return damageTypeString;
    }
}
