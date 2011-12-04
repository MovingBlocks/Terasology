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
package com.github.begla.blockmania.gui.implementation;

import com.github.begla.blockmania.gui.framework.BlockmaniaDisplayElement;

import javax.vecmath.Vector2f;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class UIButton extends BlockmaniaDisplayElement {

    public UIButton(Vector2f position, Vector2f size) {
        super(position, size);
    }

}
