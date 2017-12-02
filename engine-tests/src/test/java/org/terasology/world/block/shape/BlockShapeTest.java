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

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.ObjectArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.util.vector.Vector;
import org.mockito.Mockito;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.Asset;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.VecMath;
import org.terasology.math.Yaw;
import org.terasology.math.geom.Shape;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.*;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class BlockShapeTest extends TerasologyTestingEnvironment {
    private BlockManagerImpl blockManager;
    private AssetManager assetManager;

    @Before
    public void setup() throws Exception {
        super.setup();

        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);
        this.assetManager = assetManager;

    }

    @Test
    public void testConvexHull() {
        BlockShape blockShape =  assetManager.getAsset("engine:halfSlope", BlockShape.class).get();
        CollisionShape shape = blockShape.getCollisionShape(Rotation.rotate(Yaw.CLOCKWISE_90));

        Assert.assertEquals(shape instanceof ConvexHullShape,true);
        if(shape instanceof ConvexHullShape){
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

            ObjectArrayList<javax.vecmath.Vector3f> points = ((ConvexHullShape) shape).getPoints();
            for(int x = 0; x < points.size(); x++){
                fuzzVectorTest(test[x], VecMath.from(points.get(x)));

            }
        }

    }

    private void fuzzVectorTest(Vector3f test,Vector3f actual){
        Assert.assertEquals(test.x,actual.x,.1f);
        Assert.assertEquals(test.y,actual.y,.1f);
        Assert.assertEquals(test.z,actual.z,.1f);
    }



}
