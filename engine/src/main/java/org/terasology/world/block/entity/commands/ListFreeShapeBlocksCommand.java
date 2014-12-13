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
package org.terasology.world.block.entity.commands;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.Collections;
import java.util.List;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ListFreeShapeBlocksCommand extends Command {
    @In
    private BlockManager blockManager;

    public ListFreeShapeBlocksCommand() {
        super("listFreeShapeBlocks", false, "Lists available free shape blocks",
                "Lists all the available free shape blocks. These blocks can be created with any shape.");
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Free Shape Blocks");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("-----------------");
        stringBuilder.append(Message.NEW_LINE);
        List<BlockUri> sortedUris = Lists.newArrayList(blockManager.listFreeformBlockUris());
        Collections.sort(sortedUris);
        for (BlockUri uri : sortedUris) {
            stringBuilder.append(uri.toString());
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }
}
