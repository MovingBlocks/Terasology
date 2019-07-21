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

import net.java.games.input.Component.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.ControllerId;
import org.terasology.input.Input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration data for all known controllers.
 */
public class ControllerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ControllerConfig.class);

    private Map<String, ControllerInfo> controllers = new LinkedHashMap<>();

    public ControllerInfo getController(String name) {
        ControllerInfo info = controllers.get(name);
        if (info == null) {
            logger.info("Controller '{}' not found, adding empty ControllerInfo", name);
            info = new ControllerInfo();
            controllers.put(name, info);
        }
        return info;
    }

    public ControllerInfo addController(String name, ControllerInfo info) {
        logger.debug("addController '{}', {}", name, info);
        controllers.put(name, info);
        return controllers.get(name);
    }

    public static class ControllerInfo {
        private boolean enabled;
        private List<Button> buttons = new ArrayList<>();
        private List<Axis> axes = new ArrayList<>();
        private List<Hat> hats = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<Button> getButtons() {
            return buttons;
        }

        public void setButtons(List<Button> buttons) {
            this.buttons = buttons;
        }

        public List<Axis> getAxes() {
            return axes;
        }

        public void setAxes(List<Axis> axes) {
            this.axes = axes;
        }

        public List<Hat> getHats() {
            return hats;
        }

        public void setHats(List<Hat> hats) {
            this.hats = hats;
        }

        // The info has not been populated with the controller config
        public boolean isEmpty() {
            return !isEnabled() && getAxes().size() == 0 && getButtons().size() == 0 && getHats().size() == 0;
        }

        public Axis findAxis(String id) {
            for (Axis axis : axes) {
                if (axis.idName.equals(id)) {
                    return axis;
                }
            }
            return null;
        }

        /**
         * Finds an {@link Axis} given a {@link ControllerId} (extracted from an {@link Input}). Converts the id into an
         * {@link Identifier} to find the axis.
         * @param controllerId The {@link ControllerId} of the axis
         * @return the {@link Axis} or null if not found.
         */
        public Axis findAxisFromControllerId(int controllerId) {
            String axisId = null;
            switch (controllerId) {
                case ControllerId.X_AXIS:
                    axisId = Identifier.Axis.X.getName();
                    break;
                case ControllerId.Y_AXIS:
                    axisId = Identifier.Axis.Y.getName();
                    break;
                case ControllerId.Z_AXIS:
                    axisId = Identifier.Axis.Z.getName();
                    break;
                case ControllerId.RX_AXIS:
                    axisId = Identifier.Axis.RX.getName();
                    break;
                case ControllerId.RY_AXIS:
                    axisId = Identifier.Axis.RY.getName();
                    break;
                case ControllerId.POVX_AXIS:
                case ControllerId.POVY_AXIS:
                    axisId = Identifier.Axis.POV.getName();
                    break;
            }
            return findAxis(axisId);
        }

        public boolean axisIsInverted(Axis axis) {
            return axis != null && axis.inverted;
        }
    }

    public static class Axis {
        private String displayName;
        private String idName;
        private boolean inverted;
        private float deadZone = 0.08f;

        public Axis(String displayName, String idName, boolean inverted, float deadZone) {
            this.displayName = displayName;
            this.idName = idName;
            this.inverted = inverted;
            this.deadZone = deadZone;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getIdName() {
            return idName;
        }

        public void setIdName(String idName) {
            this.idName = idName;
        }

        public boolean isInverted() {
            return inverted;
        }

        public void setInverted(boolean inverted) {
            this.inverted = inverted;
        }

        public float getDeadZone() {
            return deadZone;
        }

        public void setDeadZone(float deadZone) {
            this.deadZone = deadZone;
        }
    }

    public static class Button {
        private String displayName;
        private String idName;

        public Button(String displayName, String idName) {
            this.displayName = displayName;
            this.idName = idName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getIdName() {
            return idName;
        }

        public void setIdName(String idName) {
            this.idName = idName;
        }
    }

    public static class Hat {
        private String name;
    }
}
