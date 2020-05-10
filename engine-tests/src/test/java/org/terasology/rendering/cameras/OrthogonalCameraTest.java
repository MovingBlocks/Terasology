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
package org.terasology.rendering.cameras;

import org.joml.Matrix4f;
import org.junit.Before;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.internal.ContextImpl;
import org.terasology.context.internal.MockContext;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.math.geom.Quat4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.TeraAssert;
import org.terasology.world.WorldProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrthogonalCameraTest {

    @Before
    public void setUp() {
        Config config = new Config(new MockContext());
        config.loadDefaults();
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, config);
    }

    @Test
    public void testOrthogonalCameraTest() {
        WorldProvider provider = mock(WorldProvider.class);
        DisplayDevice device = mock(DisplayDevice.class);
        when(device.getDisplayWidth()).thenReturn(1280);
        when(device.getDisplayHeight()).thenReturn(720);
        RenderingConfig config = new RenderingConfig();

        OrthographicCamera camera = new OrthographicCamera(10,3,2,8);
        camera.setOrientation(new Quat4f(45, .001f, .001f));
        camera.updateMatrices();

        Matrix4f viewMatrix = new Matrix4f(-5.253E-1f,  8.509E-4f, 8.509E-1f,  0.000E+0f,
            0.000E+0f,  1.000E+0f,  1.000E-3f,  0.000E+0f,
            -8.509E-1f,  5.253E-4f, -5.253E-1f,  0.000E+0f,
            -0.000E+0f, -0.000E+0f, -0.000E+0f,  1.000E+0f);
        Matrix4f projectionMatrix = new Matrix4f(-0.2857143f,0.0f,0.0f,1.8571428f,
            0.0f,-0.33333334f,0.0f,1.6666666f,
            0.0f,0.0f,-0.001f,-0.0f,
            0.0f,0.0f,0.0f,1.0f);
        Matrix4f normViewMatrix = new Matrix4f(-5.253E-1f, 8.509E-4f, 8.509E-1f,  0.000E+0f,
            0.000E+0f, 1.000E+0f,  1.000E-3f,  0.000E+0f,
            -8.509E-1f, 5.253E-4f, -5.253E-1f,  0.000E+0f,
            -0.000E+0f,-0.000E+0f, -0.000E+0f,  1.000E+0f);

        TeraAssert.assertEquals(viewMatrix,camera.getViewMatrix(), 0.001f);
        TeraAssert.assertEquals(projectionMatrix,camera.getProjectionMatrix(), 0.001f);
        TeraAssert.assertEquals(normViewMatrix,camera.getNormViewMatrix(),0.001f);
        camera.setReflected(true);
        TeraAssert.assertEquals(new Matrix4f(),camera.getNormViewMatrix(),0.001f);

    }

}
