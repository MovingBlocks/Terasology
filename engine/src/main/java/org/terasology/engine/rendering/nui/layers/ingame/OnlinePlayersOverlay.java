// Copyright 2023 The Terasology Foundation
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
import org.terasology.engine.network.PingComponent;
import org.terasology.engine.network.events.SubscribePingEvent;
import org.terasology.engine.network.events.UnSubscribePingEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                PingComponent pingComponent = localPlayer.getClientEntity().getComponent(PingComponent.class);
                if (pingComponent == null) {
                    Map<EntityRef, Long> pings = new HashMap<>();
                    for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
                        pings.put(client, null);
                    }
                    String playerListText = determinePlayerList(pings);
                    return playerListText;
                } else {
                    String playerAndPing = determinePlayerList(pingComponent.getValues());
                    return playerAndPing;
                }
            }
        });
    }

    private String determinePlayerList(Map<EntityRef, Long> pings) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<EntityRef, Long> entry : pings.entrySet()) {
            lines.add(determinePlayerLine(entry.getKey(), entry.getValue()));
        }
        return String.join("\n", lines);
    }

    private String determinePlayerLine(EntityRef client, Long ping) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        AfkComponent afkComponent = client.getComponent(AfkComponent.class);

        String prefix = (afkComponent != null && afkComponent.afk) ? FontColor.getColored("[AFK]", Color.red) : "";

        String displayName = PlayerUtil.getColoredPlayerName(clientComp.clientInfo);

        String displayPing = (ping != null) ? ping + "ms" : FontColor.getColored("---", Color.grey);

        return String.format("%-12s%-32s%8s", prefix, displayName, displayPing);
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
