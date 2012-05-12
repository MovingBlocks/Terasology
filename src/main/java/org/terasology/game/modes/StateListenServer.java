/*
 * Copyright 2012
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

package org.terasology.game.modes;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.game.GameEngine;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;

import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class StateListenServer implements GameState {

    private Logger logger = Logger.getLogger(getClass().getName());

    private String worldName;
    private String worldSeed;

    private PersistableEntityManager entityManager;

    public StateListenServer(String worldName) {
        this(worldName, null);
    }

    public StateListenServer(String worldName, String seed) {
        this.worldName = worldName;
        this.worldSeed = worldSeed;
    }

    @Override
    public void init(GameEngine engine) {
        // TODO: Change to better mod support, should be enabled via config
        ModManager modManager = new ModManager();
        for (Mod mod : modManager.getMods()) {
            mod.setEnabled(true);
        }
        modManager.saveModSelectionToConfig();
        cacheTextures();

        entityManager = new EntitySystemBuilder().build();
    }

    private void cacheTextures() {
        for (AssetUri textureURI : AssetManager.list(AssetType.TEXTURE)) {
            AssetManager.load(textureURI);
        }
    }

    @Override
    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deactivate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleInput(float delta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(float delta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void render() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
