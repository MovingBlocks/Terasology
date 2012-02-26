/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.rendering.gui.components.UICrosshair;
import org.terasology.rendering.gui.components.UIHealthBar;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UIToolbar;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.primitives.ChunkTessellator;

import javax.vecmath.Vector2f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIHeadsUpDisplay extends UIDisplayRenderer {

    /* DISPLAY ELEMENTS */
    private final UICrosshair _crosshair;
    private final UIText _debugLine1;
    private final UIText _debugLine2;
    private final UIText _debugLine3;
    private final UIText _debugLine4;
    private final UIDebugConsole _console;

    private final UIToolbar _toolbar;
    private final UIHealthBar _healthBar;

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

        _console = new UIDebugConsole();
        addDisplayElement(_console);

        _toolbar = new UIToolbar();
        _toolbar.setVisible(true);
        addDisplayElement(_toolbar);

        _healthBar = new UIHealthBar();
        _healthBar.setVisible(true);
        addDisplayElement(_healthBar);

        update();
    }


    /**
     * Renders the HUD on the screen.
     */
    public void render() {
        super.render();
    }

    public void update() {
        super.update();
        setOverlay(!_console.isVisible());

        _healthBar.setPosition(new Vector2f(_toolbar.getPosition().x, _toolbar.getPosition().y - _toolbar.getSize().y + 8f));
        _crosshair.setPosition(new Vector2f(Display.getWidth() / 2, Display.getHeight() / 2));

        boolean enableDebug = (Boolean) ConfigurationManager.getInstance().getServerSetting("World.Debug.debug");
        _debugLine1.setVisible(enableDebug);
        _debugLine2.setVisible(enableDebug);
        _debugLine3.setVisible(enableDebug);
        _debugLine4.setVisible(enableDebug);

        if (enableDebug) {
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
            _debugLine1.setText(String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f", Terasology.getInstance().getAverageFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            _debugLine2.setText(String.format("%s", Terasology.getInstance().getActiveWorldRenderer().getPlayer()));
            _debugLine3.setText(String.format("%s", Terasology.getInstance().getActiveWorldRenderer()));
            _debugLine4.setText(String.format("total vus: %s | active threads: %s", ChunkTessellator.getVertexArrayUpdateCount(), Terasology.getInstance().activeTasks()));
        }
    }

    @Override
    public void processKeyboardInput(int key) {
        super.processKeyboardInput(key);

        if (!isVisible())
            return;

        if (key == Keyboard.KEY_TAB) {
            _console.setVisible(!_console.isVisible());
        }
    }

    public UIDebugConsole getDebugConsole() {
        return _console;
    }
}
