/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.block.shape;

import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.Rotation;
import org.terasology.math.Yaw;
import org.terasology.physics.bullet.shapes.BulletConvexHullShape;
import org.terasology.physics.shapes.CollisionShape;
import org.terasology.physics.shapes.ConvexHullShape;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockShapeTest extends TerasologyTestingEnvironment {
    private BlockManagerImpl blockManager;
    private AssetManager assetManager;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();

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
