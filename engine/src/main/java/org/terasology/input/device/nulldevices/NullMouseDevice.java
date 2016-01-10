/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.input.device.nulldevices;

import com.google.common.collect.Queues;

import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.geom.Vector2i;

import java.util.Queue;

/**
 */
public class NullMouseDevice implements MouseDevice {
    @Override
    public Vector2i getPosition() {
        return new Vector2i();
    }

    @Override
    public Vector2i getDelta() {
        return new Vector2i();
    }

    @Override
    public boolean isButtonDown(int button) {
        return false;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public Queue<MouseAction> getInputQueue() {
        return Queues.newArrayDeque();
    }

    @Override
    public void setGrabbed(boolean grabbed) {
    }
}
