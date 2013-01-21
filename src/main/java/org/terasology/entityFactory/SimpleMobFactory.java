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
package org.terasology.entityFactory;

import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.rendering.logic.MeshComponent;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class SimpleMobFactory {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMobFactory.class);


    /** For getting entities*/
    private EntityManager entityManager;

    /**
     * Creates a prefab in a given position in the world
     * @param Vector3f position Where to create the prefab
     * @param prefab Which prefab to create
     * @param Vector3f color of mob (r,b,g)
     * @param float scale of mob
     * @return A reference to the entity created
     */
    public EntityRef generate(Vector3f position, Prefab prefab, Vector3f color, float scale) {
    	// Create new prefab
        EntityRef entity = entityManager.create(prefab.getName());
        // For changing location
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale(scale);
            entity.saveComponent(loc);
        }

        // Set mesh
        MeshComponent mesh = entity.getComponent(MeshComponent.class);
        if (mesh != null) {
            logger.info("Creating a {} with color {}", prefab.getName(), mesh.color);
        }
        return entity;
    }
    
    /**
     * Creates a entity in a given position in the world
     * @param Vector3f position Where to create the prefab
     * @param EntityRef entity to be cloned
     * @param Vector3f color of mob (r,b,g)
     * @param float scale of mob
     * @return A reference to the entity created
     */
    public EntityRef generate(Vector3f position, EntityRef cloneEntity, Vector3f color, float scale) {
    	// Create new entity from old
        EntityRef newEntity = entityManager.copy(cloneEntity);
        // For changing location
        LocationComponent loc = newEntity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale(scale);
            newEntity.saveComponent(loc);
        }

        // Set mesh
        MeshComponent mesh = newEntity.getComponent(MeshComponent.class);
        if (mesh != null) {
            logger.info("Clonning with color {}", mesh.color);
        }
        return newEntity;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
