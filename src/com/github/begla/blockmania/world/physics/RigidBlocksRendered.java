/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.world.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.collision.shapes.TriangleMeshShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.rendering.manager.ShaderManager;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.interfaces.BlockObserver;
import com.github.begla.blockmania.world.main.World;
import javolution.util.FastSet;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class RigidBlocksRendered implements RenderableObject, BlockObserver {

    private class BlockRigidBody extends RigidBody {
        private byte _type;

        public BlockRigidBody(RigidBodyConstructionInfo constructionInfo, byte type) {
            super(constructionInfo);
            _type = type;
        }

        public byte getType() {
            return _type;
        }
    }

    FastSet<BlockRigidBody> _blocks = new FastSet<BlockRigidBody>();
    FastSet<RigidBody> _chunks = new FastSet<RigidBody>();

    CollisionShape _blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

    CollisionDispatcher _dispatcher;
    BroadphaseInterface _broadphase;
    DefaultCollisionConfiguration _defaultCollisionConfiguration;
    SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    DiscreteDynamicsWorld _discreteDynamicsWorld;

    World _parent;

    public RigidBlocksRendered(World parent) {
        _parent = parent;

        _broadphase = new DbvtBroadphase();
        _defaultCollisionConfiguration = new DefaultCollisionConfiguration();
        _dispatcher = new CollisionDispatcher(_defaultCollisionConfiguration);
        _sequentialImpulseConstraintSolver = new SequentialImpulseConstraintSolver();
        _discreteDynamicsWorld = new DiscreteDynamicsWorld(_dispatcher, _broadphase, _sequentialImpulseConstraintSolver, _defaultCollisionConfiguration);
        _discreteDynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));

        CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);

        Vector3f groundPosition = new Vector3f(0, -1, 0);
        Matrix3f rotation = new Matrix3f();
        rotation.setIdentity();

        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(rotation, groundPosition, 1.0f)));
        RigidBodyConstructionInfo groundCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f());
        RigidBody ground = new RigidBody(groundCI);

        _discreteDynamicsWorld.addRigidBody(ground);
    }

    public void resetChunks() {
        for (RigidBody c : _chunks) {
            if (c != null)
                _discreteDynamicsWorld.removeRigidBody(c);
        }

        _chunks.clear();
    }

    public void addBlock(Vector3f position, byte type) {
        Matrix3f rot = new Matrix3f();
        rot.setIdentity();

        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, position, 1.0f)));

        Vector3f fallInertia = new Vector3f();
        _blockShape.calculateLocalInertia(1, fallInertia);

        RigidBodyConstructionInfo blockCI = new RigidBodyConstructionInfo(1, blockMotionState, _blockShape, fallInertia);
        BlockRigidBody block = new BlockRigidBody(blockCI, type);

        _discreteDynamicsWorld.addRigidBody(block);
        _blocks.add(block);
    }

    public void addStaticChunk(Vector3f position, TriangleMeshShape chunkShape) {
        Matrix3f rot = new Matrix3f();
        rot.setIdentity();

        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, position, 1.0f)));

        RigidBodyConstructionInfo blockCI = new RigidBodyConstructionInfo(0, blockMotionState, chunkShape, new Vector3f());
        RigidBody chunk = new RigidBody(blockCI);

        _discreteDynamicsWorld.addRigidBody(chunk);
        _chunks.add(chunk);
    }

    public void render() {
        for (BlockRigidBody b : _blocks) {
            Transform t = new Transform();
            b.getMotionState().getWorldTransform(t);

            GL11.glPushMatrix();

            FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
            float[] mFloat = new float[16];
            t.getOpenGLMatrix(mFloat);

            mBuffer.put(mFloat);
            mBuffer.flip();

            GL11.glTranslatef(-_parent.getPlayer().getPosition().x, -_parent.getPlayer().getPosition().y, -_parent.getPlayer().getPosition().z);
            GL11.glMultMatrix(mBuffer);

            float lightValue = calcLightValueForTransform(t);
            int lightRef = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "light");
            GL20.glUniform1f(lightRef, lightValue);

            BlockManager.getInstance().getBlock(b.getType()).render();
            GL11.glPopMatrix();
        }
    }

    private float calcLightValueForTransform(Transform t) {
        double lightValueSun = ((double) _parent.getWorldProvider().getLightAtPosition(t.origin, Chunk.LIGHT_TYPE.SUN));
        lightValueSun = (lightValueSun / 15.0) * _parent.getDaylight();
        double lightValueBlock = _parent.getWorldProvider().getLightAtPosition(t.origin, Chunk.LIGHT_TYPE.BLOCK);
        lightValueBlock = lightValueBlock / 15.0;

        return (float) Math.max(lightValueSun, lightValueBlock);
    }

    public void update() {
        _discreteDynamicsWorld.stepSimulation(1 / 60f, 5);
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {
    }

    public void blockPlaced(Chunk chunk, BlockPosition pos) {
        for (BlockRigidBody b : _blocks) {
            b.activate();
        }
    }

    public void blockRemoved(Chunk chunk, BlockPosition pos) {
        for (BlockRigidBody b : _blocks) {
            b.activate();
        }
    }
}
