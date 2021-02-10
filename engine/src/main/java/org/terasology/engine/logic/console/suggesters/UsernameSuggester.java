/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.network.ClientInfoComponent;

import java.util.Set;

/**
 * Suggests user names of all users even if they aren't online.
 */
public final class UsernameSuggester implements CommandParameterSuggester<String> {
    private final EntityManager entityManager;

    public UsernameSuggester(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Set<String> suggest(EntityRef sender, Object... resolvedParameters) {
        Set<String> clientNames = Sets.newHashSet();
        for (EntityRef clientInfo : entityManager.getEntitiesWith(ClientInfoComponent.class)) {
            DisplayNameComponent displayNameComponent = clientInfo.getComponent(DisplayNameComponent.class);
            clientNames.add(displayNameComponent.name);
        }

        return clientNames;
    }
}
