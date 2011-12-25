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
package com.github.begla.blockmania.tools;

import com.github.begla.blockmania.world.characters.Player;

import java.util.HashMap;

/**
 * Handles the player interacting with various tools. Supports both "native" tools written in the core Java engine
 * as well as Groovy-based plugins classified as tools. Important concept: There are two distinctly different ranges:
 * Native tools: 1-50       Built-in tools assigned a spot in the tool range
 * Plugin tools: 51-250     Plugin-based tools - more space for this as we might have more
 * Hotkey bar:              A mapping between 1-10 on the hot bar and the actual available tools via index key on _toolBinding
 * Eventually an inventory system will change what tools are assigned to the 10 available hot bar slots, for now we just pretend
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class ToolManager {

    /**
     * Map that contains simple native tool index values for a switch here
     */
    private final HashMap<Byte, Tool> _toolStore = new HashMap<Byte, Tool>();

    /**
     * Map that contains advanced plugin tool index values and an associated Groovy command script to execute on use
     */
    private final HashMap<Byte, Tool> _pluginStore = new HashMap<Byte, Tool>();

    /**
     * Map that will bind tool index values with hot keys 1-10
     */
    private final HashMap<Byte, Byte> _toolBinding = new HashMap<Byte, Byte>();

    /**
     * Reference back to the parent Player
     */
    private final Player _player;

    /**
     * Default constructor - would do some magic here to add native tools and look for plugin tools
     */
    public ToolManager(Player parent) {
        _player = parent;

        initNativeTools();
    }

    /**
     * Initializes the native tools.
     */
    public void initNativeTools() {

        // Put the standard place/remove block "tool" in slot 1 (incidentally that corresponds to tool 1)
        _toolBinding.put((byte) 1, (byte) 1);
        // ... and init. the corresponding tool object
        _toolStore.put((byte) 1, new BlockPlacementRemovalTool(_player));

        // Init. other native tools as before
        _toolBinding.put((byte) 2, (byte) 2);
        _toolStore.put((byte) 2, new MultipleSelectionTool(_player));

        _toolBinding.put((byte) 3, (byte) 3);
        _toolStore.put((byte) 3, new RectangleSelectionTool(_player));

        _toolBinding.put((byte) 4, (byte) 4);
        _toolStore.put((byte) 4, new RigidBlockRemovalTool(_player));

        _toolBinding.put((byte) 5, (byte) 5);
        _toolStore.put((byte) 5, new ExplosionTool(_player));
    }

    /**
     * Puts a plugin-based Groovy tool into the _pluginStore
     *
     * @param groovyTool A Tool supplied via Groovy class submitted by a Groovy script plugin
     */
    public void mapPluginTool(Tool groovyTool) {
        // Cheating with the hard coded index for now
        _toolBinding.put((byte) 6, (byte) 51);
        _pluginStore.put((byte) 51, groovyTool);
        // Blockmania.getInstance().getLogger().log(Level.INFO, "ToolManager.mapPluginTool called with Tool: " + groovyTool);
    }

    public Tool getToolForIndex(Byte toolIndex) {
        if (toolIndex == null) {
            return null;
        }

        // If we're looking at a high-range tool it is from the groovyStore - so look there and return what we find
        if (toolIndex >= 51) {
            if (_pluginStore.containsKey(toolIndex)) {
                return _pluginStore.get(toolIndex);
            }
        }
        // For low range tools we look for the native tool and if it isn't found there's something wrong
        else {
            if (_toolStore.containsKey(toolIndex)) {
                return _toolStore.get(toolIndex);
            }
        }

        return null;
    }
}
