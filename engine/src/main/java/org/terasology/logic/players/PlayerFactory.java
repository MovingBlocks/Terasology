/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.players;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.ComponentContainer;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.SpiralIterable;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.network.ColorComponent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.WorldProvider;

/**
 * Creates new player instances.
 */
public class PlayerFactory {

    private static final Logger logger = LoggerFactory.getLogger(PlayerFactory.class);

    private EntityManager entityManager;
    private WorldProvider worldProvider;

    public PlayerFactory(EntityManager entityManager, WorldProvider worldProvider) {
        this.entityManager = entityManager;
        this.worldProvider = worldProvider;
    }

    /**
     * Creates a new player character entity. The desired spawning location is derived from
     * the {@link LocationComponent} of the controller.
     * @param controller the controlling client entity
     * @return a new player character entity
     */
    public EntityRef newInstance(EntityRef controller) {

        EntityBuilder builder = entityManager.newBuilder("engine:player");

        float extraSpace = 0.5f;  // spawn a little bit above the ground
        float entityHeight = getHeightOf(builder) + extraSpace;

        LocationComponent location = controller.getComponent(LocationComponent.class);
        Vector3f spawnPosition = findSpawnPos(location.getWorldPosition(), entityHeight);
        location.setWorldPosition(spawnPosition);
        controller.saveComponent(location);

        logger.debug("Spawing player at: {}", spawnPosition);

        builder.getComponent(LocationComponent.class).setWorldPosition(spawnPosition);
        builder.setOwner(controller);

        ClientComponent clientComp = controller.getComponent(ClientComponent.class);
        if (clientComp != null) {
            ColorComponent colorComp = clientComp.clientInfo.getComponent(ColorComponent.class);

            MeshComponent meshComp = builder.getComponent(MeshComponent.class);
            meshComp.color = colorComp.color;
        }

        CharacterComponent playerComponent = builder.getComponent(CharacterComponent.class);
        playerComponent.controller = controller;

        EntityRef player = builder.build();

        Location.attachChild(player, controller, new Vector3f(), new Quat4f(0, 0, 0, 1));

        return player;
    }

    private float getHeightOf(ComponentContainer prefab) {
        BoxShapeComponent box = prefab.getComponent(BoxShapeComponent.class);
        if (box != null) {
            return box.extents.getY();
        }

        CylinderShapeComponent cylinder = prefab.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) {
            return cylinder.height;
        }

        CapsuleShapeComponent capsule = prefab.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) {
            return capsule.height;
        }

        SphereShapeComponent sphere = prefab.getComponent(SphereShapeComponent.class);
        if (sphere != null) {
            return sphere.radius * 2.0f;
        }

        HullShapeComponent hull = prefab.getComponent(HullShapeComponent.class);
        if (hull != null) {
            AABB aabb = hull.sourceMesh.getAABB();
            return aabb.maxY() - aabb.minY();
        }

        logger.warn("entity {} does not have any known extent specification - using default", prefab);
        return 1.0f;
    }

    private Vector3f findSpawnPos(Vector3f targetPos, float entityHeight) {
        int targetBlockX = TeraMath.floorToInt(targetPos.x);
        int targetBlockY = TeraMath.floorToInt(targetPos.y);
        int targetBlockZ = TeraMath.floorToInt(targetPos.z);
        Vector2i center = new Vector2i(targetBlockX, targetBlockZ);
        for (BaseVector2i pos : SpiralIterable.clockwise(center).maxRadius(3).scale(2).build()) {

            Vector3i testPos = new Vector3i(pos.getX(), targetBlockY, pos.getY());
            Vector3i spawnPos = findOpenVerticalPosition(testPos, entityHeight);
            if (spawnPos != null) {
                return new Vector3f(spawnPos.getX(), spawnPos.getY() + entityHeight, spawnPos.getZ());
            }
        }
        return null;
    }

    /**
     * find a spot above the surface that is big enough for this character
     * @param spawnPos the position to check
     * @param height the height of the entity to spawn
     * @return the topmost solid block <code>null</code> if none was found
     */
    private Vector3i findOpenVerticalPosition(Vector3i spawnPos, float height) {
        int consecutiveAirBlocks = 0;
        Vector3i newSpawnPos = new Vector3i(spawnPos);

        // TODO: also start looking downwards if initial spawn pos is in the air
        for (int i = 1; i < 20; i++) {
            if (worldProvider.isBlockRelevant(newSpawnPos)) {
                if (worldProvider.getBlock(newSpawnPos).isPenetrable()) {
                    consecutiveAirBlocks++;
                } else {
                    consecutiveAirBlocks = 0;
                }

                if (consecutiveAirBlocks >= height) {
                    newSpawnPos.subY(consecutiveAirBlocks);
                    return newSpawnPos;
                }
                newSpawnPos.add(0, 1, 0);
            }
        }

        return null;
    }
}
