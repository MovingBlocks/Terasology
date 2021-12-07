// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet;

import com.google.common.collect.Lists;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.physics.bullet.world.VoxelBlockFluidWorld;
import org.terasology.engine.physics.bullet.world.VoxelBlockWorld;
import org.terasology.engine.physics.bullet.world.VoxelWorld;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Manages voxel shape and updates collision state between Bullet and Terasology
 */
@RegisterSystem
public class VoxelWorldSystem extends BaseComponentSystem {

    @In
    private PhysicsEngine physics;
    @In
    private ChunkProvider chunkProvider;

    private final List<VoxelWorld> colliders = Lists.newArrayList();

    @Override
    public void initialise() {
        if (physics instanceof BulletPhysics) {
            colliders.add(new VoxelBlockWorld((BulletPhysics) physics));
            colliders.add(new VoxelBlockFluidWorld((BulletPhysics) physics));
        }
        super.initialise();
    }


    @ReceiveEvent(components = BlockComponent.class)
    public void onBlockChange(OnChangedBlock event, EntityRef entity) {
        Vector3ic p = event.getBlockPosition();
        colliders.forEach(k -> k.setBlock(p.x(), p.y(), p.z(), event.getNewType()));
    }

    /**
     * free chunk region from bullet
     *
     * @param beforeChunkUnload
     * @param worldEntity
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkUnloaded(BeforeChunkUnload beforeChunkUnload, EntityRef worldEntity) {
        Vector3ic chunkPos = beforeChunkUnload.getChunkPos();
        colliders.forEach(k -> k.unloadChunk(chunkPos));
    }

    /**
     * new chunks that are loaded need to update pass the data to bullet
     *
     * @param chunkAvailable the chunk
     * @param worldEntity world entity
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        Vector3ic chunkPos = chunkAvailable.getChunkPos();
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        ByteBuffer buffer =
                ByteBuffer.allocateDirect(2 * (Chunks.SIZE_X * Chunks.SIZE_Y * Chunks.SIZE_Z));
        buffer.order(ByteOrder.nativeOrder());
        for (int z = 0; z < Chunks.SIZE_Z; z++) {
            for (int x = 0; x < Chunks.SIZE_X; x++) {
                for (int y = 0; y < Chunks.SIZE_Y; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    colliders.forEach(k -> k.registerBlock(block));
                    buffer.putShort(block.getId());
                }
            }
        }
        buffer.rewind();
        colliders.forEach(k -> k.loadChunk(chunk, buffer.duplicate().asShortBuffer()));
    }
}
