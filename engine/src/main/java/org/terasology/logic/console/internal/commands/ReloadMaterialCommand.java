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
package org.terasology.logic.console.internal.commands;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.MaterialData;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class ReloadMaterialCommand extends Command {
    public ReloadMaterialCommand() {
        super("reloadMaterial", false, "Reloads a material", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
            CommandParameter.single("material", String.class, true)
        };
    }

    public String execute(EntityRef sender, String material)
    {
        AssetUri uri = new AssetUri(AssetType.MATERIAL, material);
        MaterialData materialData = CoreRegistry.get(AssetManager.class).loadAssetData(uri, MaterialData.class);
        if (materialData != null) {
            CoreRegistry.get(AssetManager.class).generateAsset(uri, materialData);
            return "Success";
        } else {
            return "Unable to resolve material '" + material + "'";
        }
    }

    //TODO Add the suggestion method
}