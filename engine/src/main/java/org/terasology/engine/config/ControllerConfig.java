/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration data for all known controllers.
 */
public class ControllerConfig {

    private Map<String, ControllerInfo> controllers = new LinkedHashMap<>();

    public ControllerInfo getController(String name) {
        ControllerInfo info = controllers.get(name);
        if (info == null) {
            info = new ControllerInfo();
            controllers.put(name, info);
        }
        return info;
    }

    public static class ControllerInfo {
        private boolean invertX = true;
        private boolean invertY = true;
        private boolean invertZ = true;
        private float movementDeadZone = 0.08f;
        private float rotationDeadZone = 0.08f;

        public boolean isInvertX() {
            return invertX;
        }

        public void setInvertX(boolean invertX) {
            this.invertX = invertX;
        }

        public boolean isInvertY() {
            return invertY;
        }

        public void setInvertY(boolean invertY) {
            this.invertY = invertY;
        }

        public boolean isInvertZ() {
            return invertZ;
        }

        public void setInvertZ(boolean invertZ) {
            this.invertZ = invertZ;
        }

        public float getMovementDeadZone() {
            return movementDeadZone;
        }

        public void setMovementDeadZone(float movementDeadZone) {
            this.movementDeadZone = movementDeadZone;
        }

        public float getRotationDeadZone() {
            return rotationDeadZone;
        }

        public void setRotationDeadZone(float rotationDeadZone) {
            this.rotationDeadZone = rotationDeadZone;
        }
    }
}
