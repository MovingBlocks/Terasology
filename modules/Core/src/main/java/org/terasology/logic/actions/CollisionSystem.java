/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.actions;

import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterSoundComponent;
import org.terasology.logic.characters.CharacterSoundSystem;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;


@RegisterSystem(RegisterMode.AUTHORITY)
public class CollisionSystem extends BaseComponentSystem {

    @In
    private org.terasology.engine.Time time;

    private Random random = new FastRandom();

    /**
     * React to any vertical collision of an entity that has a
     * health component and establishes if the character should
     * take damage and what's the amount
     * @param event vertical collision
     * @param entity
     */
    @ReceiveEvent(components = {HealthComponent.class})
    public void onLand(VerticalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        float speed = Math.abs(event.getVelocity().y);

        if (speed > health.fallingDamageSpeedThreshold) {
            int damage = (int) ((speed - health.fallingDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                entity.send(new DoDamageEvent(damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL));
            }
        }
    }

    /**
     * Same as method onLand but now it's about horizontal collision
     * @param event horizontal collision
     * @param entity
     */
    @ReceiveEvent(components = {HealthComponent.class})
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);

        Vector3f vel = new Vector3f(event.getVelocity());
        vel.y = 0;
        float speed = vel.length();

        if (speed > health.horizontalDamageSpeedThreshold) {
            int damage = (int) ((speed - health.horizontalDamageSpeedThreshold) * health.excessSpeedDamageMultiplier);
            if (damage > 0) {
                entity.send(new DoDamageEvent(damage, EngineDamageTypes.PHYSICAL.get(), EntityRef.NULL, EntityRef.NULL));
            }
        }
    }

    /**
     * For a horizontal collision we check if we can play
     * the character sounds for landing
     * @param event vertical collision
     * @param entity
     * @param characterSounds
     * @param healthComponent
     */
    @ReceiveEvent
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity, CharacterSoundComponent characterSounds, HealthComponent healthComponent) {
        Vector3f horizVelocity = new Vector3f(event.getVelocity());
        horizVelocity.y = 0;
        float velocity = horizVelocity.length();

        if (velocity > healthComponent.horizontalDamageSpeedThreshold) {
            if (characterSounds.lastSoundTime + CharacterSoundSystem.MIN_TIME < time.getGameTimeInMs()) {
                StaticSound sound = random.nextItem(characterSounds.landingSounds);
                if (sound != null) {
                    entity.send(new PlaySoundEvent(sound, characterSounds.landingVolume));
                    characterSounds.lastSoundTime = time.getGameTimeInMs();
                    entity.saveComponent(characterSounds);
                }
            }
        }
    }
}
