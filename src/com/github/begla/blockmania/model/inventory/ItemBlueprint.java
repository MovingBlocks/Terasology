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
import com.github.begla.blockmania.model.blueprints.Blueprint;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class ItemBlueprint extends Item {

    private Blueprint _blueprint;

    public ItemBlueprint(Player parent) {
        super(parent);

        setIconWithAtlasPos(10, 3);
        _toolId = (byte) 2;
        _stackSize = 1;
    }

    public Blueprint getBlueprint() {
        return _blueprint;
    }

    public void setBlueprint(Blueprint bp) {
        _blueprint = bp;
    }

}
