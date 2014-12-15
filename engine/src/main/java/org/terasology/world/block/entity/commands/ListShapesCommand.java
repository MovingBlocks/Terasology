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
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;

import java.util.Collections;
import java.util.List;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ListShapesCommand extends Command {
    public ListShapesCommand() {
        super("listShapes", false, "Lists all available shapes", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Shapes");
        stringBuilder.append(Message.NEW_LINE);
        stringBuilder.append("-----------");
        stringBuilder.append(Message.NEW_LINE);
        List<AssetUri> sortedUris = Lists.newArrayList(Assets.list(AssetType.SHAPE));
        Collections.sort(sortedUris);
        Lists.newArrayList();
        for (AssetUri uri : sortedUris) {
            stringBuilder.append(uri.toSimpleString());
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }
}
