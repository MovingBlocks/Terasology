package org.terasology.rendering.logic;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

/**
 * @author Immortius
 */
@RegisterSystem
public class LightFadeSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

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
