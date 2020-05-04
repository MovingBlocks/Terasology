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
package org.terasology.math;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.terasology.testUtil.TeraAssert;

public class TransformTest {

    @Test
    public void transformOriginTest() {
        Transform transform = new Transform(new Vector3f(5f,0,30f), new Quaternionf(), 1);

        TeraAssert.assertEquals(new Vector3f(5f,0,30f), transform.origin,0.0001f);
        TeraAssert.assertEquals(new Quaternionf(), transform.rotation,0.0001f);
        Assert.assertEquals(1,transform.scale,0.001f);
        TeraAssert.assertEquals(new Matrix3f(1,0,0,0,1,0,0,0,1),transform.getBasis(), 0.0001f);
    }

    @Test
    public void transformOrientationTest() {
        Transform transform = new Transform(new Vector3f(), new Quaternionf().rotationYXZ(.5f,0,0), 1);

        TeraAssert.assertEquals(new Vector3f(), transform.origin,0.0001f);
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(.5f,0,0), transform.rotation,0.0001f);
        Assert.assertEquals(1,transform.scale,0.001f);
        TeraAssert.assertEquals(new Matrix3f(0.88f,0.00f,0.48f,
            0.00f,1.00f,0.00f,
            -0.48f,0.00f,0.88f),transform.getBasis(), 0.01f);
    }

    @Test
    public void transformScaleTest() {
        Transform transform = new Transform(new Vector3f(), new Quaternionf(), 5);

        TeraAssert.assertEquals(new Vector3f(), transform.origin,0.0001f);
        TeraAssert.assertEquals(new Quaternionf(), transform.rotation,0.0001f);
        Assert.assertEquals(5,transform.scale,0.001f);
        TeraAssert.assertEquals(new Matrix3f(5,0.00f,0f,
            0.00f,5.00f,0.00f,
            0f,0.00f,5f),transform.getBasis(), 0.0001f);
    }

    @Test
    public void transformTest() {
        Transform transform = new Transform(new Vector3f(10, 10, 10), new Quaternionf().rotationYXZ(0.5f, 0.5f, 0.5f), 1);

        TeraAssert.assertEquals(new Matrix3f(0.88034654f, -0.21902415f, 0.4207355f
            , 0.4207355f, 0.77015114f, -0.47942555f
            , -0.21902415f, 0.599079f, 0.77015114f), transform.getBasis(), .0001f);
        TeraAssert.assertEquals(new Vector3f(10, 10, 10),transform.origin,.0001f);
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(0.5f, 0.5f, 0.5f),transform.rotation,.0001f);
        Assert.assertEquals(1,transform.scale,0.001);
    }
}
