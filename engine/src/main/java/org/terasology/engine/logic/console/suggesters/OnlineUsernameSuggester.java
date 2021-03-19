// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ClientComponent;

import java.util.Set;

/**
 *
 * Suggests user names of all online users
 *
 */
public final class OnlineUsernameSuggester implements CommandParameterSuggester<String> {
    private final EntityManager entityManager;

    public OnlineUsernameSuggester(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Set<String> suggest(EntityRef sender, Object... resolvedParameters) {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        Set<String> clientNames = Sets.newHashSet();

        for (EntityRef client : clients) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

            clientNames.add(displayNameComponent.name);
        }

        return clientNames;
    }
}
