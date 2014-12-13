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
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.registry.In;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class NEOCommand extends Command {
    @In
    private InputSystem inputSystem;

    public NEOCommand() {
        super("neo", false, "Switches to typical key binds for NEO 2 keyboard layout", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.V, new SimpleUri("engine:forwards"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.I, new SimpleUri("engine:backwards"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.U, new SimpleUri("engine:left"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.A, new SimpleUri("engine:right"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.L, new SimpleUri("engine:useItem"));
        inputSystem.linkBindButtonToKey(Keyboard.KeyId.G, new SimpleUri("engine:inventory"));

        return "Changed key bindings to NEO 2 keyboard layout.";
    }
}
