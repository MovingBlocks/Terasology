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
package org.terasology.portals;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.SimpleAIComponent;
import org.terasology.entitySystem.Prefab;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.FastRandom;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DefaultMobFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private static final Logger logger = LoggerFactory.getLogger(SpawnerSystem.class);

    /** Random source */
    private FastRandom random;
    /** For getting entities*/
    private EntityManager entityManager;

    /**
     * Creates a prefab in a given position in the world
     * @param position Where to create the prefab
     * @param prefab Which prefab to create
     * @return A reference to the entity created
     */
    public EntityRef generate(Vector3f position, Prefab prefab) {
        // Create new prefab
        EntityRef entity = entityManager.create(prefab.getName(), position);

        // Set mesh - this is currently probably specific to GelCubes that use a Mesh rather than a SkeletalMesh ...
        MeshComponent mesh = entity.getComponent(MeshComponent.class);
        if (mesh != null) {
            logger.info("Spawning a prefab with a normal mesh: {}", prefab);
            // For changing location (and size?) - needs to be changed around sometime
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null) {
                loc.setWorldPosition(position);
                loc.setLocalScale((random.randomFloat() + 1.0f) * 0.4f + 0.2f);
                entity.saveComponent(loc);
            }

            logger.info("Creating a {} with color {} - if default/black then will overwrite with a random color", prefab.getName(), mesh.color);
            // For uninitialized (technically black) GelCubes we just come up with a random color. Well, small list. For now.
            if (mesh.color.equals(new Color4f(0, 0, 0, 1))) {
                int colorId = Math.abs(random.randomInt()) % COLORS.length;
                mesh.color.set(COLORS[colorId].x, COLORS[colorId].y, COLORS[colorId].z, 1.0f);
                entity.saveComponent(mesh);
            }
        // Theory is that anything else is a SkeletalMesh, or at least Oreons are ...
        } else if (entity.getComponent(SkeletalMeshComponent.class) != null) {
            logger.info("Spawning a prefab with a SKELETAL mesh: {}", prefab);
            CharacterMovementComponent movecomp = entity.getComponent(CharacterMovementComponent.class);
            movecomp.height = 0.31f;
            entity.saveComponent(movecomp);

            // Temp hack - make portal spawned fancy mobs bounce around like idiots too just so they do something
            entity.addComponent((new SimpleAIComponent()));
        } else {
            logger.info("Was given a prefab with no mesh, can't spawn :-( {}", prefab);
        }

        return entity;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
