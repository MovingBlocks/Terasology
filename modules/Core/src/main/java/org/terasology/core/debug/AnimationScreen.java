/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.core.debug;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UISlider;

import java.util.ArrayList;

/**
 * A Tool for testing the animations of entities.
 * Animation speed can be edited with this utility
 */
public class AnimationScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(AnimationScreen.class);
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    private EntityRef entityRef;
    private UIButton spawnEntityIdButton;
    private UISlider animationSpeedSlider;
    private UIDropdownScrollable<ResourceUrn> entityDropdown;

    @Override
    public void initialise() {
        spawnEntityIdButton = find("spawnEntityIdButton", UIButton.class);
        entityDropdown = find("entityDropdown", UIDropdownScrollable.class);
        logger.info("Number of available skeletal meshes: " + assetManager.getAvailableAssets(SkeletalMesh.class).size());
        ArrayList skeletalMesh = new ArrayList(assetManager.getAvailableAssets(SkeletalMesh.class));
        if (entityDropdown != null) {
            entityDropdown.setOptions(skeletalMesh);
        }
        animationSpeedSlider = find("entityAnimationSpeedSlider", UISlider.class);
        if (animationSpeedSlider != null) {
            animationSpeedSlider.setMinimum(-0.0f);
            animationSpeedSlider.setIncrement(0.1f);
            animationSpeedSlider.setRange(10.0f);
            animationSpeedSlider.setPrecision(1);
        }
        spawnEntityIdButton.subscribe(widget -> {
            Vector3f localPlayerPosition = localPlayer.getPosition();
            Quat4f localPlayerRotation = localPlayer.getRotation();
            Vector3f offset = localPlayer.getViewDirection();
            offset.scale(2.0f);
            offset.y = 0;
            localPlayerPosition.add(offset);
            Optional<Prefab> prefab = assetManager.getAsset(entityDropdown.getSelection(), Prefab.class);
            if (prefab.isPresent() && prefab.get().getComponent(LocationComponent.class) != null) {
                entityRef = entityManager.create(prefab.get(), localPlayerPosition, localPlayerRotation);

                SkeletalMeshComponent skeletalMeshComponent = entityRef.getComponent(SkeletalMeshComponent.class);
                skeletalMeshComponent.animationRate = animationSpeedSlider.getValue();
                entityRef.saveComponent(skeletalMeshComponent);
                CharacterMovementComponent movementComponent = entityRef.getComponent(CharacterMovementComponent.class);
                movementComponent.speedMultiplier = animationSpeedSlider.getValue();
                entityRef.saveComponent(movementComponent);
            }
        });

    }
}

