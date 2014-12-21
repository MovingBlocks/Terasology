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
package org.terasology.logic.console.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.Console;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

import java.util.Collection;
import java.util.Set;

/**
 * A class used for suggesting command parameter values
 *
 * @author Limeth
 */
public interface CommandParameterSuggester<T> {
    /**
     * @param resolvedParameters Currently entered values of the types declared in the command method
     * @return An array of suggested matches.
     */
    T[] suggest(EntityRef sender, Object... resolvedParameters);

    public static class UsernameSuggester implements CommandParameterSuggester<String> {
        @Override
        public String[] suggest(EntityRef sender, Object... resolvedParameters) {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            Set<String> clientNames = Sets.newHashSet();

            for (EntityRef client : clients) {
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

                clientNames.add(displayNameComponent.name);
            }

            return clientNames.toArray(new String[clientNames.size()]);
        }
    }

    public static class CommandNameSuggester implements CommandParameterSuggester<String> {
        @Override
        public String[] suggest(EntityRef sender, Object... resolvedParameters) {
            Console console = CoreRegistry.get(Console.class);
            Collection<ICommand> commands = console.getCommands();
            String[] suggestions = new String[commands.size()];
            int i = 0;

            for (ICommand command : commands) {
                suggestions[i] = command.getName();
                i++;
            }

            return suggestions;
        }
    }

    public static class LoadedPrefabSuggester implements CommandParameterSuggester<Prefab> {
        @Override
        public Prefab[] suggest(EntityRef sender, Object... resolvedParameters) {
            AssetManager assetManager = CoreRegistry.get(AssetManager.class);
            Iterable<PojoPrefab> loadedPrefabs = assetManager.listLoadedAssets(AssetType.PREFAB, PojoPrefab.class);

            return Iterables.toArray(loadedPrefabs, Prefab.class);
        }
    }

    public static class BlockFamilySuggester implements CommandParameterSuggester<BlockFamily> {
        @Override
        public BlockFamily[] suggest(EntityRef sender, Object... resolvedParameters) {
            Iterable<BlockFamily> iterable = CoreRegistry.get(BlockManager.class).listAvailableBlockFamilies();

            return Iterables.toArray(iterable, BlockFamily.class);
        }
    }
}
