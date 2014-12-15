/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.input.internal.commands;

import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.InputSystem;
import org.terasology.input.Keyboard;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.registry.In;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class DvorakCommand extends Command {
    @In
    private InputSystem inputSystem;

    public DvorakCommand() {
        super("dvorak", false, "Switches to typical keybinds for DVORAK", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.COMMA, new SimpleUri("engine:forwards"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.A, new SimpleUri("engine:right"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.O, new SimpleUri("engine:backwards"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.E, new SimpleUri("engine:left"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.C, new SimpleUri("engine:inventory"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.PERIOD, new SimpleUri("engine:useItem"));

        return "Changed key bindings to DVORAK keyboard layout.";
    }
}
