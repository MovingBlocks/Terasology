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

package org.terasology.input.binds.inventory;

import org.terasology.input.ActivateMode;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.ControllerId;

/**
 */
@RegisterBindButton(id = "toolbarNext", description = "${engine:menu#next-toolbar-item}", mode = ActivateMode.PRESS, category = "inventory")
@DefaultBinding(type = InputType.MOUSE_WHEEL, id = 1)
@DefaultBinding(type = InputType.CONTROLLER_BUTTON, id = ControllerId.ONE)
public class ToolbarNextButton extends BindButtonEvent {
}
