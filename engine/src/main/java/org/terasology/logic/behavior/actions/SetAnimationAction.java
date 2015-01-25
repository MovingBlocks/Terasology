/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior.actions;

import org.terasology.engine.ComponentFieldUri;
import org.terasology.logic.behavior.ActionName;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.properties.OneOf;

import java.util.List;
import java.util.Random;

/**
 * Plays a animation from a animation set and sets the animation pool to pick animation to play from.
 * <p/>
 * The node stays forever in status RUNNING.
 */
@API
@ActionName("animation")
public class SetAnimationAction extends BaseAction {
    @OneOf.Provider(name = "animations")
    private ComponentFieldUri play;

    @OneOf.Provider(name = "animations")
    private ComponentFieldUri loop;

    private transient Random random;

    public SetAnimationAction() {
        random = new Random();
    }

    @Override
    public void construct(Actor actor) {
        SkeletalMeshComponent skeletalMesh = actor.skeletalMesh();
        if (play != null) {
            List<?> animationListToPlay = (List<?>) actor.getComponentField(play);
            if (animationListToPlay != null) {
                skeletalMesh.animation = (MeshAnimation) animationListToPlay.get(
                        random.nextInt(animationListToPlay.size()));
            } else {
                // ignore error, effect is visible
                skeletalMesh.animation = null;
            }
        }
        if (loop != null) {
            skeletalMesh.animationPool.clear();
            List<?> animationListToLoop = (List<?>) actor.getComponentField(loop);
            if (animationListToLoop != null) {
                for (Object object : animationListToLoop) {
                    skeletalMesh.animationPool.add((MeshAnimation) object);
                }
            }
        }
        skeletalMesh.loop = true;
        actor.save(skeletalMesh);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        return BehaviorState.SUCCESS;
    }
}
