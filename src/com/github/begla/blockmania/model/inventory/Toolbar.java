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
package com.github.begla.blockmania.model.inventory;

import com.github.begla.blockmania.logic.characters.Player;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class Toolbar {

    private int _selectedSlot;
    private int _slotBinding[] = new int[9];

    /**
     * Reference back to the parent Player
     */
    private final Player _player;

    /**
     * Init a new toolbar.
     *
     * @param parent The parent player
     */
    public Toolbar(Player parent) {
        _player = parent;
        initDefaultBindings();
    }

    private void initDefaultBindings() {
        for (int i = 0; i < 9; i++) {
            _slotBinding[i] = i;
        }
    }

    public void rollSelectedSlot(byte wheelMotion) {
        _selectedSlot = (_selectedSlot + wheelMotion) % 9;

        if (_selectedSlot < 0)
            _selectedSlot = 9 + _selectedSlot;
    }

    public Item getItemForSlot(int slot) {
        return _player.getInventory().getItemInSlot(_slotBinding[slot]);
    }

    public Item getItemForSelectedSlot() {
        return _player.getInventory().getItemInSlot(_slotBinding[getSelectedSlot()]);
    }

    public int getSelectedSlot() {
        return _selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot < 0)
            return;

        _selectedSlot = slot % 9;
    }

    public int size() {
        return _slotBinding.length;
    }

}
