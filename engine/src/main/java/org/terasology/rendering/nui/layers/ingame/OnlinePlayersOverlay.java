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
package org.terasology.rendering.nui.layers.ingame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * Overlay that lists all players that are currently online.
 */
public class OnlinePlayersOverlay extends CoreScreenLayer {
    private static final Logger logger = LoggerFactory.getLogger(OnlinePlayersOverlay.class);

    private UIText text;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
        this.text = find("playerList", UIText.class);
    }

    private String determinePlayerListText() {
        Iterable<EntityRef> allClients = entityManager.getEntitiesWith(ClientComponent.class);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (EntityRef clientEntity : allClients) {
            if (!first) {
                sb.append("\n");
            }
            ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
            sb.append(PlayerUtil.getColoredPlayerName(clientComp.clientInfo));
            first = false;
        }
        return sb.toString();
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (text != null) {
            String playerListText = determinePlayerListText();
            text.setText(playerListText);
        } else {
            logger.error("no  playerList");
        }
    }

}
