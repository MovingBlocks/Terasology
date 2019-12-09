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

import org.junit.Before;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.internal.ContextImpl;
import org.terasology.context.internal.MockContext;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.registry.CoreRegistry;
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

        Matrix4f viewMatrix = new Matrix4f(5.0273395f, 0.0f, 0.0f, 0.0f
                , 0.0f, 8.937492f, 0.0f, 0.0f
                , 0.0f, 0.0f, -1.00004f, -0.20000401f
                , 0.0f, 0.0f, -1.0f, 0.0f);
        Matrix4f projectionMatrix = new Matrix4f(5.0273395f, 0.0f, 0.0f, 0.0f
                , 0.0f, 8.937492f, 0.0f, 0.0f
                , 0.0f, 0.0f, -1.00004f, -0.20000401f
                , 0.0f, 0.0f, -1.0f, 0.0f);
        Matrix4f normViewMatrix = new Matrix4f(0.27303523f, 0.0f, -0.962004f, -0.0f
                , -0.96200305f, 0.0014514439f, -0.27303496f, -0.0f,
                0.0013962948f, 0.99999887f, 3.962953f, -0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);


        camera.getViewMatrix().epsilonEquals(viewMatrix, 0.5f);
        camera.getProjectionMatrix().epsilonEquals(projectionMatrix, 0.5f);
        camera.getNormViewMatrix().epsilonEquals(normViewMatrix, 0.5f);
        camera.setReflected(true);
        camera.getNormViewMatrix().epsilonEquals(normViewMatrix, 0.5f);
    }
}
