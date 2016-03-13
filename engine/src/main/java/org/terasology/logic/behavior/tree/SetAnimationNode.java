/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior.tree;

import java.util.List;
import java.util.Random;

import org.terasology.engine.ComponentFieldUri;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.properties.OneOf;

/**
 * Plays a animation from a animation set and sets the animation pool to pick animation to play from.
 *
 * The node stays forever in status RUNNING.
 *
 */
@API
public class SetAnimationNode extends Node {
    @OneOf.Provider(name = "animations")
    private ComponentFieldUri play;

    @OneOf.Provider(name = "animations")
    private ComponentFieldUri loop;

    public SetAnimationNode() {
    }

    @Override
    public SetAnimationTask createTask() {
        return new SetAnimationTask(this);
    }

    public static class SetAnimationTask extends Task {
        private Random random;

        public SetAnimationTask(SetAnimationNode node) {
            super(node);
            random = new Random();
        }

        @Override
        public void onInitialize() {
            SkeletalMeshComponent skeletalMesh = actor().getComponent(SkeletalMeshComponent.class);
            if (getNode().play != null) {
                List<?> animationListToPlay = (List<?>) actor().getComponentField(getNode().play);
                if (animationListToPlay != null) {
                    skeletalMesh.animation = (MeshAnimation) animationListToPlay.get(
                            random.nextInt(animationListToPlay.size()));
                } else {
                    // ignore error, effect is visible
                    skeletalMesh.animation = null;
                }
            }
            if (getNode().loop != null) {
                skeletalMesh.animationPool.clear();
                List<?> animationListToLoop = (List<?>) actor().getComponentField(getNode().loop);
                if (animationListToLoop != null) {
                    for (Object object : animationListToLoop) {
                        skeletalMesh.animationPool.add((MeshAnimation) object);
                    }
                }
            }
            skeletalMesh.loop = true;
            actor().save(skeletalMesh);
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public Status update(float dt) {
            return Status.SUCCESS;
        }

        @Override
        public SetAnimationNode getNode() {
            return (SetAnimationNode) super.getNode();
        }

    }
}
