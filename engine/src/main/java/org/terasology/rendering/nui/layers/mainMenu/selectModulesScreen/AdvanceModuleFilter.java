/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.selectModulesScreen;


import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.SelectModulesConfig;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UICheckbox;


public class AdvanceModuleFilter extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:moduleTypes");
    private UICheckbox libraryCheckBox;
    private UICheckbox assetCheckBox;
    private UICheckbox specialCheckBox;
    private UICheckbox augmentationCheckBox;
    private UICheckbox worldCheckBox;
    private UICheckbox gameplayCheckBox;

    @In
    private Config config;
    private SelectModulesConfig selectModulesConfig;
    private SelectModulesScreen selectModulesScreen;

    @Override
    public void initialise() {

        selectModulesConfig = config.getSelectModulesConfig();

        libraryCheckBox = find("libraryCheckbox", UICheckbox.class);
        libraryCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isLibraryChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsLibraryChecked(value);

                    }
                }
        );

        assetCheckBox = find("assetCheckbox", UICheckbox.class);
        assetCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isAssetChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsAssetChecked(value);

                    }
                }
        );

        worldCheckBox = find("worldCheckbox", UICheckbox.class);
        worldCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isWorldChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsWorldChecked(value);

                    }
                }
        );

        gameplayCheckBox = find("gameplayCheckbox", UICheckbox.class);
        gameplayCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isGameplayChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsGameplayChecked(value);

                    }
                }
        );

        augmentationCheckBox = find("augmentationCheckbox", UICheckbox.class);
        augmentationCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isAugmentationChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsAugmentationChecked(value);

                    }
                }
        );

        specialCheckBox = find("specialCheckbox", UICheckbox.class);
        specialCheckBox.bindChecked(
                new Binding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return selectModulesConfig.isSpecialChecked();
                    }

                    @Override
                    public void set(Boolean value) {
                        selectModulesConfig.setIsSpecialChecked(value);

                    }
                }
        );
        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());

    }
}
