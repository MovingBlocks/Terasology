/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.advancedGameSetupScreen;

import org.terasology.engine.module.StandardModuleExtension;

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
