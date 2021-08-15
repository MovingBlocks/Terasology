// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.advancedGameSetupScreen;

import org.terasology.engine.core.module.StandardModuleExtension;

public enum CheckboxAssociationEnum {
    IS_LIBRARY(StandardModuleExtension.IS_LIBRARY),
    IS_ASSET(StandardModuleExtension.IS_ASSET),
    IS_IS_WORLD(StandardModuleExtension.IS_WORLD),
    IS_GAMEPLAY(StandardModuleExtension.IS_GAMEPLAY),
    IS_AUGMENTATION(StandardModuleExtension.IS_AUGMENTATION),
    IS_SPECIAL(StandardModuleExtension.IS_SPECIAL),
    SERVER_SIDE_ONLY(StandardModuleExtension.SERVER_SIDE_ONLY);

    private String checkboxName;
    private StandardModuleExtension standardModuleExtension;

    CheckboxAssociationEnum(StandardModuleExtension standardModuleExtension) {
        this.standardModuleExtension = standardModuleExtension;
        this.checkboxName = getCheckboxNameFor(standardModuleExtension);
    }

    public String getCheckboxName() {
        return checkboxName;
    }

    public StandardModuleExtension getStandardModuleExtension() {
        return standardModuleExtension;
    }

    private static String getCheckboxNameFor(StandardModuleExtension standardModuleExtension) {
        StringBuilder builder = new StringBuilder(standardModuleExtension.getKey());
        final int prefixIndex = builder.indexOf("is");

        if (prefixIndex == 0) {
            builder.delete(prefixIndex, prefixIndex + 2);
            builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));
        }

        return builder.append("Checkbox").toString();
    }
}
