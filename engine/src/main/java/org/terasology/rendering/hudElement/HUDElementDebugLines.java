/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.hudElement;

import java.util.Locale;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.logic.manager.HUDElement;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.WorldProvider;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 *         <p/>
 *         TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class HUDElementDebugLines extends UIDisplayContainer implements HUDElement {

    protected EntityManager entityManager;
    private Time time;

    private UILabel debugLine1;
    private UILabel debugLine2;
    private UILabel debugLine3;
    private UILabel debugLine4;

    private final Config config = CoreRegistry.get(Config.class);

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public HUDElementDebugLines() {
        setId("debugLines");
    }

    @Override
    public void update() {
        super.update();

        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);

        boolean enableDebug = config.getSystem().isDebugEnabled();
        debugLine1.setVisible(enableDebug);
        debugLine2.setVisible(enableDebug);
        debugLine3.setVisible(enableDebug);
        debugLine4.setVisible(enableDebug);

        if (enableDebug) {
            CameraTargetSystem cameraTarget = CoreRegistry.get(CameraTargetSystem.class);
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
            debugLine1.setText(String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f",
                    time.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            if (entityManager != null) {
                debugLine2.setText(String.format("Active Entities: %s, Current Target: %s", entityManager.getActiveEntityCount(), cameraTarget.toString()));
            }
            Vector3f pos = CoreRegistry.get(LocalPlayer.class).getPosition();
            float yaw = (character != null) ? character.yaw : 0;
            debugLine3.setText(String.format(Locale.US, "Pos (%.2f, %.2f, %.2f), Yaw %.2f", pos.x, pos.y, pos.z, yaw));
            debugLine4.setText(String.format("total vus: %s | active threads: %s | worldTime: %.2f",
                    ChunkTessellator.getVertexArrayUpdateCount(), CoreRegistry.get(GameEngine.class).getActiveTaskCount(),
                    CoreRegistry.get(WorldProvider.class).getTime().getDays()));
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        time = CoreRegistry.get(Time.class);

        debugLine1 = new UILabel();
        debugLine1.setPosition(new Vector2f(4, 4));
        debugLine2 = new UILabel();
        debugLine2.setPosition(new Vector2f(4, 22));
        debugLine3 = new UILabel();
        debugLine3.setPosition(new Vector2f(4, 38));
        debugLine4 = new UILabel();
        debugLine4.setPosition(new Vector2f(4, 54));

        addDisplayElement(debugLine1);
        addDisplayElement(debugLine2);
        addDisplayElement(debugLine3);
        addDisplayElement(debugLine4);

        localPlayer = CoreRegistry.get(LocalPlayer.class);
    }

	@Override
	public void open() {
	}

	@Override
	public void willShutdown() {
	}

    @Override
    public void shutdown() {
    }
}
