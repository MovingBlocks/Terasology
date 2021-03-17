// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;

/**
 */
@RegisterSystem
public class LightFadeSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = LightFadeComponent.class)
    public void startLightFade(OnActivatedComponent event, EntityRef entity, LightComponent light) {
        light.simulateFading = true;
        entity.saveComponent(light);
    }

    @ReceiveEvent(components = LightFadeComponent.class)
    public void stopLightFade(BeforeDeactivateComponent event, EntityRef entity, LightComponent light) {
        light.simulateFading = false;
        entity.saveComponent(light);
    }

    @Override
    public void update(float delta) {
        for (EntityRef fadingLight : entityManager.getEntitiesWith(LightFadeComponent.class, LightComponent.class)) {
            LightFadeComponent fade = fadingLight.getComponent(LightFadeComponent.class);
            LightComponent light = fadingLight.getComponent(LightComponent.class);

            // Fade
            float ambientChange = delta * fade.ambientFadeRate;
            float diffuseChange = delta * fade.diffuseFadeRate;

            boolean incomplete = false;
            if (Math.abs(fade.targetAmbientIntensity - light.lightAmbientIntensity) < ambientChange) {
                light.lightAmbientIntensity = fade.targetAmbientIntensity;
            } else if (light.lightAmbientIntensity < fade.targetAmbientIntensity) {
                light.lightAmbientIntensity += ambientChange;
                incomplete = true;
            } else {
                light.lightAmbientIntensity -= ambientChange;
                incomplete = true;
            }

            if (Math.abs(fade.targetDiffuseIntensity - light.lightDiffuseIntensity) < diffuseChange) {
                light.lightDiffuseIntensity = fade.targetDiffuseIntensity;
            } else if (light.lightDiffuseIntensity < fade.targetDiffuseIntensity) {
                light.lightDiffuseIntensity += diffuseChange;
                incomplete = true;
            } else {
                light.lightDiffuseIntensity -= diffuseChange;
                incomplete = true;
            }

            // If fade complete, remove fade and maybe light
            if (incomplete) {
                fadingLight.saveComponent(light);
            } else {
                if (fade.removeLightAfterFadeComplete) {
                    fadingLight.removeComponent(LightComponent.class);
                } else {
                    fadingLight.saveComponent(light);
                }

                fadingLight.removeComponent(LightFadeComponent.class);
            }
        }
    }
}
