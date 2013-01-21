/*
 * Copyright 2012 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.hunger;

import org.terasology.entitySystem.Component;

/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public final class HungerComponent implements Component {
    // Configuration options
    public int maxContentment = 20;
    public float deregenRate = 0.0f;
    public float waitBeforeDeregen = 0.0f;
    public float waitBeetweenDamage = 0.0f;

    public int currentContentment = 20;

    // Regen info
    public float timeSinceLastEat = 0.0f;
    public float timeSinceLastDamage = 0.0f;
    public float partialDeregen = 0.0f;

    public HungerComponent() {
    }

    public HungerComponent(int maxContentment, float deregenRate, float waitBeforeDeregen) {
        this.maxContentment = maxContentment;
        this.currentContentment = maxContentment;
        this.deregenRate = deregenRate;
        this.waitBeforeDeregen = waitBeforeDeregen;
    }
    
    public int hunger(){
    	return maxContentment-currentContentment;
    }
    
}
