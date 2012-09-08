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
package org.terasology.componentSystem.common;

import org.terasology.components.HealthComponent;
import org.terasology.components.ProjectileComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.HitEvent;
import org.terasology.events.NoHealthEvent;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class DamageSystem implements EventHandlerSystem {
    
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
    
        
    @ReceiveEvent(components =  {ProjectileComponent.class})
    public void onProjectileHit(HitEvent event, EntityRef entity) {
    }
    
        
    @ReceiveEvent(components =  {HealthComponent.class})
    public void onHit(HitEvent event, EntityRef entity) {
    }
    
    @ReceiveEvent(components = {HealthComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
	  
    }
}
