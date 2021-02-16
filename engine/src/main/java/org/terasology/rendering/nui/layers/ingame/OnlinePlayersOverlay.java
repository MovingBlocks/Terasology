/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.logic.afk.AfkComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.ClientComponent;
import org.terasology.network.PingStockComponent;
import org.terasology.network.events.SubscribePingEvent;
import org.terasology.network.events.UnSubscribePingEvent;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIText;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;

import java.util.Map;

/**
 * Overlay that lists all players that are currently online.
 */
public class OnlinePlayersOverlay extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(OnlinePlayersOverlay.class);

    private UIText text;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        this.text = find("playerList", UIText.class);
        text.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                logger.info("localPlayer is: {}", localPlayer);
                PingStockComponent pingStockComp = localPlayer.getClientEntity().getComponent(PingStockComponent.class);
                if (pingStockComp == null) {
                    String playerListText = determinePlayerListText();
                    return playerListText;
                } else {
                    String playerAndPing = determinePlayerAndPing(pingStockComp);
                    return playerAndPing;
                }
            }
        });
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

    private String determinePlayerAndPing(PingStockComponent pingStockComponent) {
        Map<EntityRef, Long> pingMap = pingStockComponent.getValues();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<EntityRef, Long> entry : pingMap.entrySet()) {
            EntityRef clientEntity = entry.getKey();
            if (clientEntity == null || clientEntity.getComponent(ClientComponent.class) == null) {
                logger.warn("OnlinePlayersOverlay skipping a null client entity or component");
                continue;
            }

            if (!first) {
                sb.append("\n");
            }

            ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
            AfkComponent afkComponent = clientEntity.getComponent(AfkComponent.class);
            if (afkComponent != null) {
                if (afkComponent.afk) {
                    sb.append(FontColor.getColored("[AFK]", Color.red));
                    sb.append(" ");
                }
            }
            sb.append(PlayerUtil.getColoredPlayerName(clientComp.clientInfo));
            sb.append(" ");
            Long pingValue = pingMap.get(clientEntity);
            if (pingValue == null) {
                sb.append("-");
            } else {
                sb.append(pingValue.toString());
                sb.append("ms");
            }
            first = false;
        }
        return sb.toString();
    }

    @Override
    public void onOpened() {
        localPlayer.getClientEntity().send(new SubscribePingEvent());
    }

    @Override
    public void onClosed() {
        localPlayer.getClientEntity().send(new UnSubscribePingEvent());
    }
}
