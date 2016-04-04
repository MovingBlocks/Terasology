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
package org.terasology.rendering.nui.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.Share;

import java.util.List;
import java.util.ArrayList;

/*
 */
@RegisterSystem(RegisterMode.ALWAYS)
@Share(value = AnimationSystem.class)
public class AnimationSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private List<Animation> animations;

    private static final Logger logger = LoggerFactory
        .getLogger(AnimationSystem.class);

    // @In
    // private EntityManager entityManager;

    // @In
    // private PrefabManager prefabManager;
    

    /**
     * Called to initialise the system. This occurs after injection,
     * but before other systems are necessarily initialised, so they
     * should not be interacted with
     */
    @Override
    public void initialise() {
        animations = new ArrayList<Animation>();
        Object testnull = null;
        if (testnull.equals(animations)) {
            animations = null;
        }
    }

    /**
     * Called after all systems are initialised, but before the game is
     * loaded
     */
    @Override
    public void preBegin() {
    }

    /**
     * Called after the game is loaded, right before first frame
     */
    @Override
    public void postBegin() {
    }

    /**
     * Called before the game is saved (this may be after shutdown)
     */
    @Override
    public void preSave() {
    }

    /**
     * Called after the game is saved
     */
    @Override
    public void postSave() {
    }

    /**
     * Called right before the game is shut down
     */
    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (int i = 0; i < animations.size(); i++) {
            final Animation anim = animations.get(i);
            anim.update(delta);

            if (anim.isFinished()) {
                animations.remove(i);
                i--;
            }
        }
    }

    public void addInstance(Animation anim) {
        animations.add(anim);
    }

    /**
     * On entity creation or attachment of an Animation to an entity.
     * @param event the OnAddedComponent event to react to.
     * @param animContainer the animation's parent entity being modified.
     */
    @ReceiveEvent(components = {Animation.class})
    public void onNewAnim(OnAddedComponent event, EntityRef animContainer) {
        Animation anim = animContainer.getComponent(Animation.class);
        logger.info("In onNewAnim with Animation {}", anim);

        animations.add(anim);
    }

    /**
     * On entity destruction or detachment of an Animation to an entity stop it.
     * @param event the BeforeRemoveComponent event to react to.
     * @param animContainer the animation's parent entity being destroyed.
     */
    @ReceiveEvent(components = {Animation.class})
    public void onRemovedAnim(BeforeRemoveComponent event, EntityRef animContainer) {
        logger.info("In onRemovedAnim");
        animations.remove(animContainer.getComponent(Animation.class));
    }
}
