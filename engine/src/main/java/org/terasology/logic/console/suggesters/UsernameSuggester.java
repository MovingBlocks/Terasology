// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ClientInfoComponent;

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
