// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.physics.engine;

import com.google.api.client.util.Lists;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.registry.In;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

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
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private ChunkProvider chunkProvider;
    @In
    private BlockManager blockManager;

    private List<VoxelWorld> voxelWorlds = Lists.newArrayList();

    @Override
    public void initialise() {
        if (physics instanceof BulletPhysics) {
            voxelWorlds.add(VoxelLiquidWorld.create((BulletPhysics) physics, blockManager));
            voxelWorlds.add(VoxelBlockWorld.create((BulletPhysics) physics, blockManager));
        }
        super.initialise();
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void onBlockChange(OnChangedBlock event, EntityRef entity) {
        voxelWorlds.forEach((voxelWorld -> voxelWorld.setBlock(event.getBlockPosition(), event.getNewType())));
    }

    /**
     * free chunk region from bullet
     *
     * @param beforeChunkUnload
     * @param worldEntity
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkUloaded(BeforeChunkUnload beforeChunkUnload, EntityRef worldEntity) {
        Vector3ic chunkPos = beforeChunkUnload.getChunkPos();
        voxelWorlds.forEach(vw -> vw.freeRegion(chunkPos.x(), chunkPos.y(), chunkPos.z()));
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
                    short blockId = chunk.getBlockId(x, y, z);
                    for (VoxelWorld vw : voxelWorlds) {
                        vw.tryRegister(blockId);
                    }
                    buffer.putShort(blockId);
                }
            }
        }

        buffer.rewind();
        for (VoxelWorld vw : voxelWorlds) {
            vw.setRegion(chunkPos.x(), chunkPos.y(), chunkPos.z(), buffer.duplicate().asShortBuffer());
        }
    }
}
