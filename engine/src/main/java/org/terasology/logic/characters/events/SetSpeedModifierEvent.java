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

import org.terasology.entitySystem.event.Event;
import org.terasology.logic.characters.CharacterSpeedModifierComponent;
import org.terasology.logic.characters.MovementMode;

/**
 */



public class SetSpeedModifierEvent implements Event{
    private float factor;
    private FactorType factorType;

    public enum FactorType
    {
        NONE,
        UNHINGED,
        HOVERCAM
    }

    public SetSpeedModifierEvent(float factor, String factorType) {
        this.factor = factor;
        this.factorType = getFactor(factorType);
    }

    public FactorType getFactor(String type) {
        switch (type) {
            case "UNHINGED":
                return FactorType.UNHINGED;
            case "HOVERCAM":
                return FactorType.HOVERCAM;
            default:
                return FactorType.NONE;
        }
    }

    public float getFactorValue() {
        return factor;
    }

    public FactorType getFactorType()
    {
        return factorType;
    }
}