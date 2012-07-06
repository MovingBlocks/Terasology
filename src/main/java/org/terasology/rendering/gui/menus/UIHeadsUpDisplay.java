/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.events.MinionMessageEvent;
import org.terasology.mods.miniions.rendering.gui.components.UIMessageQueue;
import org.terasology.mods.miniions.rendering.gui.components.UIMinionbar;
import org.terasology.rendering.gui.components.*;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIHeadsUpDisplay extends UIDisplayWindow implements EventHandlerSystem {

    protected EntityManager entityManager;

    /* DISPLAY ELEMENTS */
    private final UICrosshair _crosshair;
    private final UIText _debugLine1;
    private final UIText _debugLine2;
    private final UIText _debugLine3;
    private final UIText _debugLine4;

    private final UIToolbar _toolbar;
    private final UIMinionbar _minionbar;
    private final UIMessageQueue _messagequeue;
    private final UIHealthBar _healthBar;
    private final UIBuff _buffBar;

    /**
     * Init. the HUD.
     */
    public UIHeadsUpDisplay() {
        _crosshair = new UICrosshair();
        _crosshair.setVisible(true);

        _debugLine1 = new UIText(new Vector2f(4, 4));
        _debugLine2 = new UIText(new Vector2f(4, 22));
        _debugLine3 = new UIText(new Vector2f(4, 38));
        _debugLine4 = new UIText(new Vector2f(4, 54));

        addDisplayElement(_crosshair);
        addDisplayElement(_debugLine1);
        addDisplayElement(_debugLine2);
        addDisplayElement(_debugLine3);
        addDisplayElement(_debugLine4);

        _toolbar = new UIToolbar();
        _toolbar.setVisible(true);
        addDisplayElement(_toolbar);

        _minionbar = new UIMinionbar();
        _minionbar.setVisible(true);
        addDisplayElement(_minionbar);

        _messagequeue = new UIMessageQueue();
        _messagequeue.setVisible(true);
        addDisplayElement(_messagequeue);

        _healthBar = new UIHealthBar();
        _healthBar.setVisible(true);
        addDisplayElement(_healthBar);

        _buffBar = new UIBuff();
        _buffBar.setVisible(true);
        addDisplayElement(_buffBar);
        setVisible(true);

        CoreRegistry.get(EventSystem.class).registerEventHandler(this);
    }


    /**
     * Renders the HUD on the screen.
     */
    public void render() {
        super.render();
    }

    public void update() {
        super.update();

        _healthBar.setPosition(new Vector2f(_toolbar.getPosition().x, _toolbar.getPosition().y - _toolbar.getSize().y + 8f));
        _crosshair.setPosition(new Vector2f(Display.getWidth() / 2, Display.getHeight() / 2));

        boolean enableDebug = Config.getInstance().isDebug();
        _debugLine1.setVisible(enableDebug);
        _debugLine2.setVisible(enableDebug);
        _debugLine3.setVisible(enableDebug);
        _debugLine4.setVisible(enableDebug);

        if (enableDebug) {
            CameraTargetSystem cameraTarget = CoreRegistry.get(CameraTargetSystem.class);
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
            Timer timer = CoreRegistry.get(Timer.class);
            _debugLine1.setText(String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f", timer.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            _debugLine2.setText(String.format("%s", cameraTarget.toString()));
            _debugLine3.setText(String.format("%s", CoreRegistry.get(WorldRenderer.class)));
            _debugLine4.setText(String.format("total vus: %s | active threads: %s", ChunkTessellator.getVertexArrayUpdateCount(), CoreRegistry.get(GameEngine.class).getActiveTaskCount()));
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {MinionComponent.class})
    public void onMessageReceived(MinionMessageEvent event, EntityRef entityref) {
        _messagequeue.addIconToQueue(event.getMinionMessage());
    }
}
