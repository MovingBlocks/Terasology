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
package org.terasology.logic.console.server.commands;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.In;

/**
 * @author Martin Steiger, Limeth
 */
@RegisterSystem
public class SaveCommand extends Command {
    @In
    private StorageManager storageManager;

    public SaveCommand() {
        super("save", true, "Triggers the creation of a save game", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        storageManager.requestSaving();
        return "Save requested.";
    }
}
