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
package org.terasology.rendering.cameras;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
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

public class PerspectiveCameraTest {


    @Before
    public void setUp() {
        Config config = new Config(new MockContext());
        config.loadDefaults();
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, config);
    }

    @Test
    public void testPerspectiveCamera() {
        WorldProvider provider = mock(WorldProvider.class);
        DisplayDevice device = mock(DisplayDevice.class);
        when(device.getDisplayWidth()).thenReturn(1280);
        when(device.getDisplayHeight()).thenReturn(720);
        RenderingConfig config = new RenderingConfig();


        PerspectiveCamera camera = new PerspectiveCamera(provider, config, device);
        camera.setOrientation(new Quat4f(45, .001f, .001f));
        camera.updateMatrices();

        Matrix4f viewMatrix = new Matrix4f(-5.253E-1f, 0.000E+0f, 8.509E-1f, 0.000E+0f,
            8.509E-4f, 1.000E+0f, 5.253E-4f, 0.000E+0f,
            -8.509E-1f, 1.000E-3f, -5.253E-1f, 0.000E+0f,
            -0.000E+0f, -0.000E+0f, -0.000E+0f, 1.000E+0f);
        Matrix4f projectionMatrix = new Matrix4f(5.0273395f, 0.0f, 0.0f, 0.0f
            , 0.0f, 8.937492f, 0.0f, 0.0f
            , 0.0f, 0.0f, -1.00004f, -0.20000401f
            , 0.0f, 0.0f, -1.0f, 0.0f);
        Matrix4f normViewMatrix = new Matrix4f(-.525322f, 0.0f, 0.8509036f, -0.0f
            , 8.509035E-4f, 0.9999995f, 5.25322E-4f, -0.0f,
            -0.85090315f, 0.001f, -0.5253218f, -0.0f,
            0.0f, 0.0f, 0.0f, 1.0f);

        Matrix4f reflectedNormViewMatrix =  new Matrix4f( -5.253E-1f, 0.000E+0f, 8.509E-1f,   0.000E+0f,
            8.509E-4f, -1.000E+0f, 5.253E-4f,  0.000E+0f,
            -8.509E-1f,  -1.000E-3f, -5.253E-1f,  0.000E+0f,
            0.000E+0f,  0.000E+0f,  0.000E+0f,  1.000E+0f);

        TeraAssert.assertEquals(viewMatrix, camera.getViewMatrix(), 0.001f);
        TeraAssert.assertEquals(projectionMatrix, camera.getProjectionMatrix(), 0.001f);
        TeraAssert.assertEquals(normViewMatrix, camera.getNormViewMatrix(), 0.001f);
        camera.setReflected(true);
        TeraAssert.assertEquals(reflectedNormViewMatrix, camera.getNormViewMatrix(), 0.001f);
    }
}
