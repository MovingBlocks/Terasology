// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.physics.engine;

import com.badlogic.gdx.physics.bullet.collision.VoxelCollisionAlgorithmWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import com.badlogic.gdx.physics.bullet.collision.btVoxelShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import gnu.trove.set.hash.TShortHashSet;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.registry.In;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.terasology.physics.bullet.BulletPhysics.AABB_SIZE;

/**
 * Manages voxel shape and updates collision state between Bullet and Terasology for
 * the liquid blocks
 */
@RegisterSystem
public class VoxelLiquidWorldSystem extends BaseComponentSystem {

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

    private final TShortHashSet registered = new TShortHashSet();

    private btRigidBodyConstructionInfo blockConsInf;
    private btVoxelShape worldShape;
    private VoxelCollisionAlgorithmWrapper wrapper;
    private btRigidBody rigidBody;

    @Override
    public void initialise() {
        if (physics instanceof BulletPhysics) {
            btDiscreteDynamicsWorld discreteDynamicsWorld = ((BulletPhysics) physics).getDiscreteDynamicsWorld();

            wrapper = new VoxelCollisionAlgorithmWrapper(Chunks.SIZE_X, Chunks.SIZE_Y,
                Chunks.SIZE_Z);
            worldShape = new btVoxelShape(wrapper, new Vector3f(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE),
                new Vector3f(AABB_SIZE, AABB_SIZE, AABB_SIZE));

            Matrix4f matrix4f = new Matrix4f();
            btDefaultMotionState blockMotionState = new btDefaultMotionState(matrix4f);

            blockConsInf = new btRigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
            rigidBody = new btRigidBody(blockConsInf);
            rigidBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | rigidBody.getCollisionFlags()); // voxel world is added to static collision flag
            short mask = (short) StandardCollisionGroup.LIQUID.getFlag(); // interacts with liquid only
            discreteDynamicsWorld.addRigidBody(rigidBody, physics.combineGroups(StandardCollisionGroup.LIQUID), mask); // adds rigid body to world
        }

        super.initialise();
    }


    /**
     * try to update voxel info for the wrapper for the associated block id. nothing if block already registred
     * @param id block id
     */
    private void tryRegister(short id) {
        if (id != 0 && !registered.contains(id)) {
            Block block = blockManager.getBlock(id);
            register(block);
        }
    }

    /**
     * try to update voxel info for the wrapper for the associated block. nothing if block already registred
     * @param block the block
     */
    private void tryRegister(Block block) {
        if (!registered.contains(block.getId())) {
            register(block);
        }
    }
    /**
     * update voxel info for the wrapper for the associated block id
     * @param block the block
     */
    private void register(Block block) {
        btCollisionShape shape = ((BulletCollisionShape) block.getCollisionShape()).underlyingShape;
        btVoxelInfo info = new btVoxelInfo(shape != null && block.isTargetable(),
                shape != null && !block.isPenetrable(), block.getId(), shape, block.getCollisionOffset(),
                block.getFriction(), block.getRestitution(), block.getFriction());
        wrapper.setVoxelInfo(info);
        registered.add(block.getId());
    }


    @ReceiveEvent(components = BlockComponent.class)
    public void onBlockChange(OnChangedBlock event, EntityRef entity) {
        tryRegister(event.getNewType());
        wrapper.setBlock(event.getBlockPosition().x, event.getBlockPosition().y, event.getBlockPosition().z,
            event.getNewType().getId());
    }

    /**
     * free chunk region from bullet
     * @param beforeChunkUnload
     * @param worldEntity
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkUloaded(BeforeChunkUnload beforeChunkUnload, EntityRef worldEntity) {
        Vector3ic chunkPos = beforeChunkUnload.getChunkPos();
        wrapper.freeRegion(chunkPos.x(), chunkPos.y(), chunkPos.z());
    }

    /**
     * new chunks that are loaded need to update pass the data to bullet
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
                    tryRegister(blockId);
                    buffer.putShort(blockId);
                }
            }
        }
        buffer.rewind();
        wrapper.setRegion(chunkPos.x(), chunkPos.y(), chunkPos.z(), buffer.asShortBuffer());
    }
}
