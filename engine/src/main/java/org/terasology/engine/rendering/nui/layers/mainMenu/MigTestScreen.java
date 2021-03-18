// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UIList;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

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
