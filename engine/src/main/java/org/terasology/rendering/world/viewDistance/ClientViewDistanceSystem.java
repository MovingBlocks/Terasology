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
package org.terasology.rendering.world.viewDistance;

import java.util.Arrays;
import java.util.List;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.i18n.TranslationSystem;
import org.terasology.i18n.TranslationSystemImpl;
import org.terasology.logic.notifications.NotificationMessageEvent;
import org.terasology.logic.players.DecreaseViewDistanceButton;
import org.terasology.logic.players.IncreaseViewDistanceButton;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeListener;

/**
 * Handles view distance changes on the client.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientViewDistanceSystem extends BaseComponentSystem {

    @In
    private Config config;

    @In
    private Context context;

    @In
    private WorldRenderer worldRenderer;

    @In
    private LocalPlayer localPlayer;

    private PropertyChangeListener viewDistanceListener;
    private PropertyChangeListener chunkLodsListener;

    private TranslationSystem translationSystem;

    @Override
    public void initialise() {
        viewDistanceListener = evt -> {
            if (evt.getPropertyName().equals(RenderingConfig.VIEW_DISTANCE)) {
                onChangeViewDistanceChange();
            }
        };
        config.getRendering().subscribe(viewDistanceListener);
        chunkLodsListener = evt -> {
            if (evt.getPropertyName().equals(RenderingConfig.CHUNK_LODS)) {
                onChangeViewDistanceChange();
            }
        };
        config.getRendering().subscribe(chunkLodsListener);

        translationSystem = new TranslationSystemImpl(context);
    }

    public void onChangeViewDistanceChange() {
        ViewDistance viewDistance = config.getRendering().getViewDistance();
        int chunkLods = (int) config.getRendering().getChunkLods();

        if (worldRenderer != null) {
            worldRenderer.setViewDistance(viewDistance, chunkLods);
        }

        EntityRef clientEntity = localPlayer.getClientEntity();
        clientEntity.send(new ViewDistanceChangedEvent(viewDistance));
    }

    @Override
    public void shutdown() {
        config.getRendering().unsubscribe(viewDistanceListener);
    }

    /**
     * Increases view distance upon receiving an increase view distance event.
     * @param button The button or key pressed to increase view distance.
     * @param entity The player entity that triggered the view distance increase.
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onIncreaseViewDistance(IncreaseViewDistanceButton button, EntityRef entity) {
        int viewDistance = config.getRendering().getViewDistance().getIndex();
        int maxViewDistance = ViewDistance.values().length - 1;

        //Ensuring that the view distance does not exceed its maximum value.
        if (viewDistance != maxViewDistance) {
            ViewDistance greaterViewDistance = ViewDistance.forIndex(viewDistance + 1);
            String greaterViewDistanceStr = translationSystem.translate(greaterViewDistance.toString());
            fireChangeEvent("Increasing view distance to " + greaterViewDistanceStr + ".", Arrays.asList(entity));
            //Presenting user with a warning if the view distance is set higher than recommended.
            if (greaterViewDistance == ViewDistance.MEGA || greaterViewDistance == ViewDistance.EXTREME) {
                fireChangeEvent("Warning: Increasing view distance to " + greaterViewDistanceStr
                        + " may result in performance issues.", Arrays.asList(entity));
            }
            config.getRendering().setViewDistance(greaterViewDistance);
        }
        button.consume();
    }

    /**
     * Decreases view distance upon receiving a decrease view distance event.
     * @param button The button or key pressed to decrease view distance.
     * @param entity The player entity that triggered the view distance decrease.
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onDecreaseViewDistance(DecreaseViewDistanceButton button, EntityRef entity) {
        int viewDistance = config.getRendering().getViewDistance().getIndex();
        int minViewDistance = 0;

        //Ensuring that the view distance does not fall below its minimum value.
        if (viewDistance != minViewDistance) {
            ViewDistance lesserViewDistance = ViewDistance.forIndex(viewDistance - 1);
            String lesserViewDistanceStr = translationSystem.translate(lesserViewDistance.toString());
            fireChangeEvent("Decreasing view distance to " + lesserViewDistanceStr + ".", Arrays.asList(entity));
            config.getRendering().setViewDistance(lesserViewDistance);
        }
        button.consume();
    }

    /**
     * Fires notification events upon changes to debug parameters.
     * @param message Notification event message.
     * @param entities Entities which will receive the notification event.
     */
    private void fireChangeEvent(String message, List<EntityRef> entities) {
        for (EntityRef client : entities) {
            client.send(new NotificationMessageEvent(message, client));
        }
    }
}
