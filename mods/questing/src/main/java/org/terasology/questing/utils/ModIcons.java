package org.terasology.questing.utils;

import org.terasology.model.inventory.Icon;

public class ModIcons {
    private static final String QUESTINGICONS16 = "questing:questingicon16";

    public static void loadIcons() {
        Icon.set("questcard", "questing:questingicon16", 0, 0);
    }

}
