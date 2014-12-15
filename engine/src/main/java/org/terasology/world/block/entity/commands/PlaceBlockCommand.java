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

import com.google.common.base.Joiner;
import org.terasology.logic.console.internal.Command;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author Immortius, Limeth
 */
// TODO: Fix this up for multiplayer (cannot at the moment due to the use of camera), also apply required permission
@RegisterSystem
public class PlaceBlockCommand extends Command {
    // TODO: Remove once camera is handled better
    @In
    private WorldRenderer renderer;

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider world;

    public PlaceBlockCommand() {
        super("placeBlock", false, "Places a block in front of the player", "Places the specified block in front of " +
                "the player. The block is set directly into the world and might override existing blocks." +
                "After placement the block can be destroyed like any regular placed block.");
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("blockName", String.class, true)
        };
    }

    public String execute(EntityRef sender, String blockName) {
        Camera camera = renderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily blockFamily;

        List<BlockUri> matchingUris = blockManager.resolveAllBlockFamilyUri(blockName);
        if (matchingUris.size() == 1) {
            blockFamily = blockManager.getBlockFamily(matchingUris.get(0));

        } else if (matchingUris.isEmpty()) {
            throw new IllegalArgumentException("No block found for '" + blockName + "'");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Non-unique block name, possible matches: ");
            Joiner.on(", ").appendTo(builder, matchingUris);
            return builder.toString();
        }

        if (world != null) {
            world.setBlock(new Vector3i((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z), blockFamily.getArchetypeBlock());

            StringBuilder builder = new StringBuilder();
            builder.append(blockFamily.getArchetypeBlock());
            builder.append(" block placed at position (");
            builder.append((int) spawnPos.x).append((int) spawnPos.y).append((int) spawnPos.z).append(")");
            return builder.toString();
        }
        throw new IllegalArgumentException("Sorry, something went wrong!");
    }

    //TODO Implement the suggest method
}
