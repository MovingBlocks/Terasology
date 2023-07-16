// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame;

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
 * Overlay that lists all players that are currently online and their pings.
 */
public class OnlinePlayersOverlay extends CoreScreenLayer {

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
                return determinePlayerList(getPingMap(pingComponent));
            }
        });
    }

    /**
     * Assemble a map from connected players (or clients) to their ping.
     *
     * If the ping component is null, the connected clients are determined by looking at entities with the {@link ClientComponent}.
     * In this case, the ping values are {@code null}.
     *
     * @param pingComponent component with information on connected players, or {@code null} if not present
     * @return a mapping from connected clients to their respective ping (or {@code null} if the ping cannot be determined)
     */
    private Map<EntityRef, Long> getPingMap(PingComponent pingComponent) {
        //TODO: There's a noticeable delay when opening the overlay before the first ping comes in and all players are shown.
        //      We could either try to match the entity refs here, or pre-fill the component sooner in the ServerPingSystem.
        if (pingComponent != null) {
            return pingComponent.getValues();
        } else {
            Map<EntityRef, Long> pings = new HashMap<>();
            for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
                pings.put(client, null);
            }
            return pings;
        }
    }

    /**
     * Create multi-line string, containing one line per connected player.
     */
    private String determinePlayerList(Map<EntityRef, Long> pings) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<EntityRef, Long> entry : pings.entrySet()) {
            lines.add(determinePlayerLine(entry.getKey(), entry.getValue()));
        }
        return String.join("\n", lines);
    }

    /**
     * Create a single-line string with the player name and their ping.
     *
     * <pre>
     *      [AFK]    Player4612                           42ms
     *      -------- -------------------------------- --------
     *         8                  32                      8
     * </pre>
     */
    private String determinePlayerLine(EntityRef client, Long ping) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        AfkComponent afkComponent = client.getComponent(AfkComponent.class);

        String prefix = (afkComponent != null && afkComponent.afk) ? FontColor.getColored("[AFK]", Color.red) : "";
        String displayName = PlayerUtil.getColoredPlayerName(clientComp.clientInfo);
        String displayPing = (ping != null) ? ping + "ms" : FontColor.getColored("---", Color.grey);
        //TODO: the formatting does not work well since we're not using a mono-spaced font. we should investigate whether we can use
        //      a different UI element or at least a monospaced font to align prefix, player name, and ping better.
        return String.format("%-8s%-32s%8s", prefix, displayName, displayPing);
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
