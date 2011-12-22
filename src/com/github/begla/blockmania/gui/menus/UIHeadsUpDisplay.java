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
package com.github.begla.blockmania.gui.menus;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.debug.BlockmaniaProfiler;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.gui.components.*;
import com.github.begla.blockmania.gui.framework.UIDisplayRenderer;
import com.github.begla.blockmania.world.chunk.ChunkMeshGenerator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;
import java.util.HashMap;

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
    private final UIPieChart _pieChart;

    private long _lastStatUpdate;

    /**
     * Init. the HUD.
     */
    public UIHeadsUpDisplay() {
        _crosshair = new UICrosshair();

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

        _pieChart = new UIPieChart();
        addDisplayElement(_pieChart);
    }


    /**
     * Renders the HUD on the screen.
     */
    public void render() {
        super.render();

        if (!isVisible())
            return;
    }

    public void update() {
        super.update();

        _healthBar.setPosition(new Vector2f(_toolbar.getPosition().x, _toolbar.getPosition().y - _toolbar.getSize().y + 8f));

        _crosshair.setVisible((Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.crosshair"));
        _crosshair.setPosition(new Vector2f(Display.getDisplayMode().getWidth() / 2, Display.getDisplayMode().getHeight() / 2));

        boolean enableDebug = (Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug");
        _debugLine1.setVisible(enableDebug);
        _debugLine2.setVisible(enableDebug);
        _debugLine3.setVisible(enableDebug);
        _debugLine4.setVisible(enableDebug);

        if (Blockmania.getInstance().getTime() - _lastStatUpdate > 200) {
            HashMap<String, Double> profilerResult = BlockmaniaProfiler.getResults();
            _pieChart.setData(profilerResult);
            _lastStatUpdate = Blockmania.getInstance().getTime();
        }

        _pieChart.setVisible(enableDebug);
        _pieChart.setPosition(new Vector2f(Display.getWidth() - _pieChart.getSize().x - 16f, 128f));

        double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
        _debugLine1.setText(String.format("%s (fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f)", ConfigurationManager.getInstance().getConfig().get("System.gameTitle"), Blockmania.getInstance().getAverageFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
        _debugLine2.setText(String.format("%s", Blockmania.getInstance().getActiveWorldRenderer().getPlayer()));
        _debugLine3.setText(String.format("%s", Blockmania.getInstance().getActiveWorldRenderer()));
        _debugLine4.setText(String.format("total vus: %s | active threads: %s", ChunkMeshGenerator.getVertexArrayUpdateCount(), Blockmania.getInstance().getThreadPool().getActiveCount()));
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
