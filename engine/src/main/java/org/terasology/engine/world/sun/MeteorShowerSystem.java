// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Occasionally spawns a meteor shower - a brief burst of streaking particles above each connected player -
 * when night falls. See https://github.com/MovingBlocks/Terasology/issues/97: a rare special-effect
 * event in the sky, distinct from the constant day/night cycle.
 * <p>
 * The roll happens once per {@link OnDuskEvent}, i.e. at most once per night, matching the issue's own
 * "rare, not constant" requirement. Position is per connected player rather than a single world-wide
 * event, since {@code CoreAssets:meteorShowerParticleEffect} entities are ordinary world-space particle
 * emitters (see {@link org.terasology.engine.particles.components.ParticleEmitterComponent}), not part of
 * the sky dome itself - they need to be near enough each player to actually render.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MeteorShowerSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MeteorShowerSystem.class);

    private static final String METEOR_SHOWER_PREFAB = "CoreAssets:meteorShowerParticleEffect";

    /** Rolled once per night; keep it low; a meteor shower every single night stops being rare. */
    private static final float CHANCE_PER_NIGHT = 0.15f;

    private static final int MIN_METEORS = 3;
    private static final int MAX_METEORS = 7;

    /** Spawn height above the player - high enough to read as "in the sky", not "over your head". */
    private static final float MIN_HEIGHT_OFFSET = 40f;
    private static final float MAX_HEIGHT_OFFSET = 80f;

    /** Horizontal spread around the player, so meteors don't all converge on one point overhead. */
    private static final float HORIZONTAL_SPREAD = 60f;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;

    private Random random;

    @Override
    public void initialise() {
        random = new FastRandom();
    }

    @ReceiveEvent
    public void onDusk(OnDuskEvent event, EntityRef worldEntity) {
        if (random.nextFloat() >= CHANCE_PER_NIGHT) {
            return;
        }

        logger.debug("perfProbe meteorShower: starting tonight");
        for (Client client : networkSystem.getPlayers()) {
            EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
            LocationComponent location = character.getComponent(LocationComponent.class);
            if (location == null) {
                continue;
            }
            spawnShowerAround(location.getWorldPosition(new Vector3f()));
        }
    }

    /** Public so tests can inspect exactly what a shower produced, against the real registered instance. */
    public List<EntityRef> spawnShowerAround(Vector3f playerPosition) {
        List<EntityRef> spawned = new ArrayList<>();
        int meteorCount = random.nextInt(MIN_METEORS, MAX_METEORS + 1);
        for (int i = 0; i < meteorCount; i++) {
            // Sampling x/z independently within [-SPREAD, SPREAD] would let the diagonal distance
            // reach SPREAD*sqrt(2) - a radius+angle sample keeps every meteor within HORIZONTAL_SPREAD
            // blocks of the player, matching what the constant's name promises.
            float angle = random.nextFloat(0, (float) (2 * Math.PI));
            float radius = random.nextFloat(0, HORIZONTAL_SPREAD);
            Vector3f spawnPos = new Vector3f(
                    playerPosition.x + radius * (float) Math.cos(angle),
                    playerPosition.y + random.nextFloat(MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET),
                    playerPosition.z + radius * (float) Math.sin(angle));

            EntityBuilder meteorBuilder = entityManager.newBuilder(METEOR_SHOWER_PREFAB);
            if (meteorBuilder.hasComponent(LocationComponent.class)) {
                meteorBuilder.getComponent(LocationComponent.class).setWorldPosition(spawnPos);
                spawned.add(meteorBuilder.build());
            }
        }
        return spawned;
    }
}
