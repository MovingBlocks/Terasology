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
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UIList;

import java.util.Arrays;
import java.util.List;

public class MigTestScreen extends CoreScreenLayer {
    @Override
    public void initialise() {
        List<String> values = Arrays.asList("one", "two", "three", "12345678901234567890");
        String selectedValue = values.get(1);

        for (String id : new String[]{"dropdown1", "dropdown2", "dropdown3", "dropdown4"}) {
            find(id, UIDropdown.class).setOptions(values);
            find(id, UIDropdown.class).setSelection(selectedValue);
        }

        for (String id : new String[]{"dropdownScrollable1", "dropdownScrollable2", "dropdownScrollable3", "dropdownScrollable4"}) {
            find(id, UIDropdownScrollable.class).setVisibleOptions(2);
            find(id, UIDropdownScrollable.class).setOptions(values);
            find(id, UIDropdownScrollable.class).setSelection(selectedValue);
        }

        for (String id : new String[]{"list1", "list2", "list3", "list4"}) {
            find(id, UIList.class).setList(values);
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
