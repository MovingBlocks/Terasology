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

// TODO: should dynamically generate checkbox list from boolean
// StandardModuleExtensions rather than hardcoding associations here
public enum CheckboxAssociationEnum {
    IS_LIBRARY("libraryCheckbox", StandardModuleExtension.IS_LIBRARY),
    IS_ASSETPLAY("assetCheckbox", StandardModuleExtension.IS_ASSETPLAY),
    IS_IS_WORLD("worldCheckbox", StandardModuleExtension.IS_WORLD),
    IS_GAMEPLAY("gameplayCheckbox", StandardModuleExtension.IS_GAMEPLAY),
    IS_AUGMENTATION("augmentationCheckbox", StandardModuleExtension.IS_AUGMENTATION),
    IS_SPECIAL("specialCheckbox", StandardModuleExtension.IS_SPECIAL),
    SERVER_SIDE_ONLY("serverSideOnlyCheckbox", StandardModuleExtension.SERVER_SIDE_ONLY);

    private String checkboxName;
    private StandardModuleExtension standardModuleExtension;

    private CheckboxAssociationEnum(String checkboxName, StandardModuleExtension standardModuleExtension) {
        this.checkboxName = checkboxName;
        this.standardModuleExtension = standardModuleExtension;
    }

    public String getCheckboxName() {
        return checkboxName;
    }

    public StandardModuleExtension getStandardModuleExtension() {
        return standardModuleExtension;
    }
}
