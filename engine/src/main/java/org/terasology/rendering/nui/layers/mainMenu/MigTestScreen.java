/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.Arrays;

/**
 * Created by synopia on 06.01.14.
 */
public class MigTestScreen extends CoreScreenLayer {
    @Override
    public void initialise() {
        find("list1", UIList.class).setList(Arrays.asList("one", "two", "12345678901234567890"));
        find("list2", UIList.class).setList(Arrays.asList("one", "two", "12345678901234567890"));
        find("list3", UIList.class).setList(Arrays.asList("one", "two", "12345678901234567890"));
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
