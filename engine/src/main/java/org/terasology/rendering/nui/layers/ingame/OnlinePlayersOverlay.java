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
import org.terasology.i18n.TranslationSystem;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.PingComponent;
import org.terasology.network.Server;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

/**
 * Overlay that lists all players that are currently online.
 */
public class OnlinePlayersOverlay extends CoreScreenLayer {
    private static final Logger logger = LoggerFactory.getLogger(OnlinePlayersOverlay.class);

    private UIText text;
    private UIText pingText;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
        this.text = find("playerList", UIText.class);
        RowLayout row = find("row", RowLayout.class);
        if (networkSystem.getServer() != null) {
            //if multiplayer, add column for ping
            UIText ping = new UIText();
            ping.setReadOnly(true);
            ping.setMultiline(true);
            ping.setFamily(text.getFamily());
            this.pingText = ping;
            row.addWidget(pingText, null);
        }
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

    private String determinePlayerPingText() {
        Iterable<EntityRef> allClients = entityManager.getEntitiesWith(ClientComponent.class);
        StringBuilder sbPing = new StringBuilder();
        boolean first = true;
        for (EntityRef clientEntity : allClients) {
            if (!first) {
                sbPing.append("\n");
            }
            PingComponent pingComponent = clientEntity.getComponent(PingComponent.class);
            if (pingComponent != null && pingComponent.ping > 0) {
                sbPing.append(String.valueOf(pingComponent.ping));
                sbPing.append(" ms");
            }
            first = false;
        }
        return sbPing.toString();
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
        if (pingText != null) {
            pingText.setText(determinePlayerPingText());
        }
    }
    
}
