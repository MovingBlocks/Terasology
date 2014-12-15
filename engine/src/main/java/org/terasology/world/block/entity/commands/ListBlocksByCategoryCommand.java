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
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.Collections;
import java.util.List;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ListBlocksByCategoryCommand extends Command {
    @In
    private BlockManager blockManager;

    public ListBlocksByCategoryCommand() {
        super("listBlocksByCategory", false, "Lists all blocks by category", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String category : blockManager.getBlockCategories()) {
            stringBuilder.append(category);
            stringBuilder.append(Message.NEW_LINE);
            stringBuilder.append("-----------");
            stringBuilder.append(Message.NEW_LINE);
            List<BlockUri> categoryBlocks = Lists.newArrayList(blockManager.getBlockFamiliesWithCategory(category));
            Collections.sort(categoryBlocks);
            for (BlockUri uri : categoryBlocks) {
                stringBuilder.append(uri.toString());
                stringBuilder.append(Message.NEW_LINE);
            }
            stringBuilder.append(Message.NEW_LINE);
        }
        return stringBuilder.toString();
    }
}
