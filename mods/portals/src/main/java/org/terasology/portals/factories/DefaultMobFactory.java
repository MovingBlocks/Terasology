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
package org.terasology.portals.factories;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Prefab;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.utilities.FastRandom;
import org.terasology.portals.SpawnerSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DefaultMobFactory {

    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private static final Logger logger = LoggerFactory.getLogger(SpawnerSystem.class);

    /*random*/
    private FastRandom random;
    /*for geting entitys*/
    private EntityManager entityManager;

    /**
     * generates creep
     * @param position
     * @param creep
     * @return enitity
     */
    public EntityRef generate(Vector3f position, Prefab creep) {
    	/*Create new creep*/
        EntityRef entity = entityManager.create(creep.getName());
        /*for changing location*/
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(position);
            loc.setLocalScale((random.randomFloat() + 1.0f) * 0.4f + 0.2f);
            entity.saveComponent(loc);
        }

        /*set mesh*/
        MeshComponent mesh = entity.getComponent(MeshComponent.class);
        if (mesh != null) {
            logger.info("Creating a {} with color {} - if default/black then will overwrite with a random color", creep.getName(), mesh.color);
            // For uninitialized (technically black) GelCubes we just come up with a random color. Well, small list. For now.
            if (mesh.color.equals(new Color4f(0, 0, 0, 1))) {
                int colorId = Math.abs(random.randomInt()) % COLORS.length;
                mesh.color.set(COLORS[colorId].x, COLORS[colorId].y, COLORS[colorId].z, 1.0f);
                entity.saveComponent(mesh);
            }
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
