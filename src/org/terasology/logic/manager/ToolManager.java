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
package org.terasology.logic.manager;

import org.terasology.logic.characters.Player;
import org.terasology.logic.tools.BlueprintTool;
import org.terasology.logic.tools.DefaultBlockTool;
import org.terasology.logic.tools.ExplosionTool;
import org.terasology.logic.tools.Tool;

import java.util.HashMap;

/**
 * Handles the player interacting with various tools. Supports both "native" tools written in the core Java engine
 * as well as Groovy-based plugins classified as tools. Important concept: There are two distinctly different ranges:
 * <p/>
 * Native tools: 1-50       Built-in tools assigned a spot in the tool range
 * Plugin tools: 51-250     Plugin-based tools - more space for this as we might have more
 * <p/>
 * TODO: Add dynamic loading of Groovy-based tools
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
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
     * Reference back to the parent Player
     */
    private final Player _player;

    /**
     * Default constructor - would do some magic here to add native tools and look for plugin tools
     * @param parent The parent player
     */
    public ToolManager(Player parent) {
        _player = parent;

        initNativeTools();
    }

    /**
     * Initializes the native tools.
     */
    public void initNativeTools() {
        _toolStore.put((byte) 1, new DefaultBlockTool(_player));
        //_toolStore.put((byte) 2, new MultipleSelectionTool(_player));
        _toolStore.put((byte) 2, new BlueprintTool(_player));
        _toolStore.put((byte) 3, new ExplosionTool(_player));
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
