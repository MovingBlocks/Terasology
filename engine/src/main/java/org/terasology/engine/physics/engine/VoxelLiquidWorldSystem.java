/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.engine.physics.engine;

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
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.bullet.BulletPhysics;
import org.terasology.engine.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.terasology.engine.physics.bullet.BulletPhysics.AABB_SIZE;

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
     * update voxel info for the wrapper for the associated block id
     * @param block the block
     */
    private void tryRegister(Block block) {
        short id = block.getId();
        if (!registered.contains(id)) {
            btCollisionShape shape = ((BulletCollisionShape) block.getCollisionShape()).underlyingShape;
            btVoxelInfo info = new btVoxelInfo(shape != null && block.isLiquid(),
                shape != null && block.isLiquid(), id, shape, block.getCollisionOffset(),
                block.getFriction(), block.getRestitution(), block.getFriction());
            wrapper.setVoxelInfo(info);
            registered.add(id);
        }
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
                    Block block = chunk.getBlock(x, y, z);
                    tryRegister(block);
                    buffer.putShort(block.getId());
                }
            }
        }
        buffer.rewind();
        wrapper.setRegion(chunkPos.x(), chunkPos.y(), chunkPos.z(), buffer.asShortBuffer());
    }
}
