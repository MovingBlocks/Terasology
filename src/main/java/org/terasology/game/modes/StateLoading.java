/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.Queues;
import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.loadProcesses.CacheBlocks;
import org.terasology.game.modes.loadProcesses.CacheTextures;
import org.terasology.game.modes.loadProcesses.CreateWorldEntity;
import org.terasology.game.modes.loadProcesses.InitialiseCommandSystem;
import org.terasology.game.modes.loadProcesses.InitialiseEntitySystem;
import org.terasology.game.modes.loadProcesses.InitialiseSystems;
import org.terasology.game.modes.loadProcesses.InitialiseWorld;
import org.terasology.game.modes.loadProcesses.LoadEntities;
import org.terasology.game.modes.loadProcesses.LoadPrefabs;
import org.terasology.game.modes.loadProcesses.PrepareLocalWorld;
import org.terasology.game.modes.loadProcesses.PrepareWorld;
import org.terasology.game.modes.loadProcesses.RegisterBlocks;
import org.terasology.game.modes.loadProcesses.RegisterInputSystem;
import org.terasology.game.modes.loadProcesses.RegisterMods;
import org.terasology.game.modes.loadProcesses.RegisterSystems;
import org.terasology.game.modes.loadProcesses.RespawnPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.windows.UIScreenLoading;
import org.terasology.world.WorldInfo;

import java.util.Queue;

/**
 * @author Immortius
 */
public class StateLoading implements GameState {

    private static final Logger logger = LoggerFactory.getLogger(StateLoading.class);
    
    private GameEngine gameEngine;
    
    private WorldInfo worldInfo;
    private Queue<LoadProcess> loadProcesses = Queues.newArrayDeque();
    private LoadProcess current;
    private int currentExpectedSteps = 0;
    private int completedSteps = 0;

    private GUIManager guiManager;

    private UIScreenLoading loadingScreen;

    public StateLoading(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
        this.guiManager = CoreRegistry.get(GUIManager.class);
    }

    @Override
    public void init(GameEngine engine) {
    	gameEngine = engine;
    	
        loadProcesses.add(new RegisterMods(worldInfo));
        loadProcesses.add(new CacheTextures());
        loadProcesses.add(new RegisterBlocks(worldInfo));
        loadProcesses.add(new CacheBlocks());
        loadProcesses.add(new InitialiseEntitySystem());
        loadProcesses.add(new LoadPrefabs());
        loadProcesses.add(new RegisterInputSystem());
        loadProcesses.add(new RegisterSystems());
        loadProcesses.add(new InitialiseCommandSystem());
        loadProcesses.add(new InitialiseWorld(worldInfo));
        loadProcesses.add(new InitialiseSystems());
        loadProcesses.add(new LoadEntities(worldInfo));
        loadProcesses.add(new CreateWorldEntity());
        loadProcesses.add(new PrepareLocalWorld());
        loadProcesses.add(new PrepareWorld());
        loadProcesses.add(new RespawnPlayer());

        popStep();
        GUIManager guiManager = CoreRegistry.get(GUIManager.class);
        loadingScreen = (UIScreenLoading) guiManager.openWindow("loading");
        loadingScreen.updateStatus(current.getMessage(), completedSteps / (currentExpectedSteps * 100f));
    }

    private void popStep() {
        current = null;
        currentExpectedSteps = 0;
        while (currentExpectedSteps == 0 && !loadProcesses.isEmpty()) {
            current = loadProcesses.remove();
            logger.debug(current.getMessage());
            currentExpectedSteps = current.begin();
        }
        completedSteps = 0;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
    	try {
	    	long startTime = 1000 * Sys.getTime() / Sys.getTimerResolution();
	
	        while (current != null && 1000 * Sys.getTime() / Sys.getTimerResolution() - startTime < 20) {
	            if (current.step()) {
	                popStep();
	            } else {
	                completedSteps++;
	            }
	        }
	        if (current == null) {
	            CoreRegistry.get(GUIManager.class).closeAllWindows();
	            CoreRegistry.get(GameEngine.class).changeState(new StateSinglePlayer());
	        } else {
	            if (currentExpectedSteps > 0) {
	                loadingScreen.updateStatus(current.getMessage(), 100f * completedSteps / currentExpectedSteps);
	            } else {
	                loadingScreen.updateStatus(current.getMessage(), 0);
	            }
	            guiManager.update();
	        }
    	} catch (NeedToReinitStateLoading e) {
    		loadProcesses = Queues.newArrayDeque();
    		currentExpectedSteps = 0;
    	    completedSteps = 0;
    	    
    	    worldInfo.setSeed("");
    		
    		this.init(gameEngine);
    	}
    }

    @Override
    public void render() {
        CoreRegistry.get(GUIManager.class).render();
    }

}
