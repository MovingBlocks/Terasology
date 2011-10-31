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
package com.github.begla.blockmania.gui;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.characters.Player;
import javolution.util.FastMap;

import java.util.logging.Level;

/**
 * Handles the player interacting with various tools. Supports both "native" tools written in the core Java engine
 * as well as Groovy-based plugins classified as tools. Important concept: There are two distinctly different ranges:
 * Native tools: 1-50       Built-in tools assigned a spot in the tool range
 * Plugin tools: 51-250     Plugin-based tools - more space for this as we might have more
 * Hotkey bar:              A mapping between 1-10 on the hot bar and the actual available tools via index key on _toolBinding
 * Eventually an inventory system will change what tools are assigned to the 10 available hot bar slots, for now we just pretend
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class ToolBelt {

    /** Map that contains simple native tool index values for a switch here */
    private FastMap<Byte, Byte> _toolStore = new FastMap<Byte, Byte>();

    /** Map that contains advanced plugin tool index values and an associated Groovy command script to execute on use */
    private FastMap<Byte, String> _pluginStore = new FastMap<Byte, String>();

    /** Map that will bind tool index values with hot keys 1-10 */
    private FastMap<Byte, Byte> _toolBinding = new FastMap<Byte, Byte>();

    /** Which slot in the hot bar is the active tool */
    private byte _selectedTool = 1;

    /** Reference back to the parent Player */
    private Player _player;

    /** Default constructor - would do some magic here to add native tools and look for plugin tools */
    public ToolBelt(Player parent) {
        _player = parent;
        // Put the standard place/remove block "tool" in slot 1 (incidentally that corresponds to tool 1)
        _toolBinding.put(new Byte((byte)1), new Byte((byte)1));
    }

    /**
     * Puts a plugin-based Groovy tool into the _pluginStore
     * @param groovyScript  String containing a valid Groovy script to execute upon activating the tool
     */
    public void mapPluginTool(String groovyScript) {
        // Nothing yet - assign the script a byte key we can then later bind to a hot bar slot
    }

    /**
     * Sets _selectedTool to the supplied value, given by the keyboard listener caller
     * @param toolBarSlotIndex  A 1-10 value corresponding to the slot in the tool bar being selected
     */
    public void setSelectedTool(byte toolBarSlotIndex) {
         _selectedTool = toolBarSlotIndex;
    }

    /**
     * Sets _selectedTool to the appropriate index value (the imaginary 10-slot tool belt / hot bar) - via mouse wheel
     * @param wheelMotion   How many "notches" have the mouse wheel rolled
     */
    public void rollSelectedTool(byte wheelMotion) {
        Blockmania.getInstance().getLogger().log(Level.INFO, "Rolling the selected tool by " + wheelMotion);
        System.out.println();
        _selectedTool += wheelMotion;
        // Roll back to the normal 1-10 range if we're now outside it (player may be able to roll the wheel more than a full 10 slots in one go)
        while (_selectedTool > 10) {
            _selectedTool -= 10;
            Blockmania.getInstance().getLogger().log(Level.INFO, "Mouse wheel rolled the selected tool above 10, substracting 10, getting " + _selectedTool);
        }

        while (_selectedTool < 1) {
            _selectedTool += 10;
            Blockmania.getInstance().getLogger().log(Level.INFO, "Mouse wheel rolled the selected tool below 1, adding 10, getting " + _selectedTool);
        }
    }

    /**
     * Activates the selected tool - checks _selectedTool against a switch containing both native tools and plugin tools
     * @param leftMouse  if true then left mouse was used to trigger, otherwise it was right mouse
     */
    public void activateTool(boolean leftMouse) {
        // First we dig out the Byte index that the value in _selectedTool references in the _toolBinding map
        Byte toolIndex = _toolBinding.get(new Byte(_selectedTool));
        String mouse = leftMouse ? "Left" : "Right";
        Blockmania.getInstance().getLogger().log(Level.INFO, mouse + " button tool activation happened for slot " + _selectedTool + ", which is tool " + toolIndex);
        if (toolIndex == null) {
            Blockmania.getInstance().getLogger().log(Level.WARNING, mouse + " button tool activation happened for an 'empty slot' " + toolIndex);
            return;
        }

        // If we're looking at a high-range tool it is from the groovyStore - so look there and execute what we find
        if (toolIndex >= 51) {
            System.out.println("Nothing yet");
        }
        // For low range tools we look for the native tool and if it isn't found there's something wrong
        else {
            switch (toolIndex) {
                // Tool 1 is the built-in place / remove block mode and the default
                case 1:
                    if (leftMouse) {
                        _player.placeBlock(_player.getSelectedBlockType());
                    }
                    else {
                        _player.removeBlock();
                    }
                    break;

                // None of the native tools are selected, what gives :-(
                default:
                    Blockmania.getInstance().getLogger().log(Level.WARNING, "Tool activation happened for an unrecognized tool: " + toolIndex);
                    break;
            }
        }
    }

    public byte getSelectedTool() {
        return _selectedTool;
    }
}
