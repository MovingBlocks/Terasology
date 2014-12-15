/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.internal.commands.core;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.console.internal.CoreCommand;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.asset.UIData;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
@CoreCommand
public class ReloadUICommand extends Command {
    public ReloadUICommand() {
        super("reloadUI", false, "Reloads a ui and clears the HUD. Use at your own risk", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
            CommandParameter.single("ui", String.class, true)
        };
    }

    public String execute(EntityRef sender, String ui)
    {
        CoreRegistry.get(NUIManager.class).clear();

        AssetUri uri = new AssetUri(AssetType.UI_ELEMENT, ui);
        UIData uiData = CoreRegistry.get(AssetManager.class).loadAssetData(uri, UIData.class);
        if (uiData != null) {
            CoreRegistry.get(AssetManager.class).generateAsset(uri, uiData);
            return "Success";
        } else {
            return "Unable to resolve ui '" + ui + "'";
        }
    }

    //TODO Add the suggestion method
}
