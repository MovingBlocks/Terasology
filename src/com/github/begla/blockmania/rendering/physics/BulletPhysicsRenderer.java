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
package com.github.begla.blockmania.rendering.physics;

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
import com.github.begla.blockmania.logic.manager.ShaderManager;
import com.github.begla.blockmania.logic.world.BlockObserver;
import com.github.begla.blockmania.logic.world.Chunk;
import com.github.begla.blockmania.model.blocks.BlockManager;
import com.github.begla.blockmania.model.structures.BlockPosition;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.rendering.world.WorldRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Renders blocks using the Bullet physics library.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BulletPhysicsRenderer implements RenderableObject, BlockObserver {

    private class BlockRigidBody extends RigidBody {
        private final byte _type;

        public BlockRigidBody(RigidBodyConstructionInfo constructionInfo, byte type) {
            super(constructionInfo);
            _type = type;
        }

        public byte getType() {
            return _type;
        }
    }

    final ArrayList<BlockRigidBody> _blocks = new ArrayList<BlockRigidBody>();
    final HashSet<RigidBody> _chunks = new HashSet<RigidBody>();

    final CollisionShape _blockShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

    final CollisionDispatcher _dispatcher;
    final BroadphaseInterface _broadphase;
    final DefaultCollisionConfiguration _defaultCollisionConfiguration;
    final SequentialImpulseConstraintSolver _sequentialImpulseConstraintSolver;
    final DiscreteDynamicsWorld _discreteDynamicsWorld;

    final WorldRenderer _parent;

    public BulletPhysicsRenderer(WorldRenderer parent) {
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
        if (chunkShape == null)
            return;

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

            GL11.glTranslated(-_parent.getPlayer().getPosition().x, -_parent.getPlayer().getPosition().y, -_parent.getPlayer().getPosition().z);
            GL11.glMultMatrix(mBuffer);

            float lightValue = _parent.getRenderingLightValueAt(new Vector3d(t.origin));
            int lightRef = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "light");
            GL20.glUniform1f(lightRef, lightValue);

            BlockManager.getInstance().getBlock(b.getType()).render();
            GL11.glPopMatrix();
        }
    }

    public void update() {
        cleanUp();

        _discreteDynamicsWorld.stepSimulation(1 / 60f, 5);
    }

    private void cleanUp() {
        if (_blocks.isEmpty())
            return;

        BlockRigidBody b = _blocks.remove(0);

        if (b.isActive() && _blocks.size() < 64) {
            _blocks.add(b);
            return;
        }

        _discreteDynamicsWorld.removeRigidBody(b);
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {
    }

    public void removeAllBlocks() {
        for (BlockRigidBody b : _blocks) {
            _discreteDynamicsWorld.removeRigidBody(b);
        }

        _blocks.clear();
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
