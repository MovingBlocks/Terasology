// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block.shape;

import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Yaw;
import org.terasology.engine.physics.bullet.shapes.BulletConvexHullShape;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.physics.shapes.ConvexHullShape;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockShapeTest extends TerasologyTestingEnvironment {
    private BlockManagerImpl blockManager;
    private AssetManager assetManager;

    @BeforeEach
    public void setup() throws Exception {

        this.assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);

    }

    @Test
    public void testConvexHull() {
        BlockShape blockShape = assetManager.getAsset("engine:halfSlope", BlockShape.class).get();
        CollisionShape shape = blockShape.getCollisionShape(Rotation.rotate(Yaw.CLOCKWISE_90));

        assertTrue(shape instanceof ConvexHullShape);
        Vector3f[] test = new Vector3f[]{new Vector3f(0.49999997f, 0.0f, 0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(0.49999997f, 0.0f, -0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(0.49999997f, 0.0f, -0.49999997f),
            new Vector3f(0.49999997f, 0.0f, 0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(0.49999997f, 0.0f, -0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, -0.49999997f),
            new Vector3f(-0.49999997f, -0.49999997f, 0.49999997f),
            new Vector3f(0.49999997f, 0.0f, 0.49999997f)};

        BulletConvexHullShape bulletConvexHullShape = (BulletConvexHullShape) shape;

        //TODO: Test fails because native library is not loaded
        for (int x = 0; x < ((btConvexHullShape) bulletConvexHullShape.underlyingShape).getNumPoints(); x++) {
            fuzzVectorTest(test[x],  ((btConvexHullShape) bulletConvexHullShape.underlyingShape).getScaledPoint(x));
        }
    }

    private void fuzzVectorTest(Vector3f test, Vector3f actual) {
        assertEquals(test.x, actual.x, .1f);
        assertEquals(test.y, actual.y, .1f);
        assertEquals(test.z, actual.z, .1f);
    }



}
