// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.afk.AfkComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerUtil;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.PingStockComponent;
import org.terasology.engine.network.events.SubscribePingEvent;
import org.terasology.engine.network.events.UnSubscribePingEvent;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

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
                PingSubscriberComponent pingComponent = localPlayer.getClientEntity().getComponent(PingSubscriberComponent.class);
                if (pingComponent == null) {
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
