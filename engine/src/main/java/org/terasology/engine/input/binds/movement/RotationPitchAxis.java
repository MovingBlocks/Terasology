/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.input.binds.movement;

import org.terasology.input.BindAxisEvent;
import org.terasology.input.ControllerId;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.RegisterRealBindAxis;
import org.terasology.input.SendEventMode;

/**
 * Relates to the vertical screen axis, i.e. look up/down.
 */
@RegisterRealBindAxis(id = "rotationPitch", eventMode = SendEventMode.WHEN_NON_ZERO)
@DefaultBinding(type = InputType.CONTROLLER_AXIS, id = ControllerId.RY_AXIS)
public class RotationPitchAxis extends BindAxisEvent {
}
