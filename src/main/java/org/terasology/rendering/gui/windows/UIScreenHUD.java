/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.windows;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.components.CuredComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PoisonedComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.events.MinionMessageEvent;
import org.terasology.mods.miniions.rendering.gui.components.UIMessageQueue;
import org.terasology.mods.miniions.rendering.gui.components.UIMinionbar;
import org.terasology.rendering.gui.components.UIBuff;
import org.terasology.rendering.gui.components.UIItemCell;
import org.terasology.rendering.gui.components.UIItemContainer;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIScreenHUD extends UIDisplayWindow implements EventHandlerSystem {

    protected EntityManager entityManager;

    /* DISPLAY ELEMENTS */
    private final UIGraphicsElement[] _hearts;
    private final UIGraphicsElement crosshair;
    private final UIText debugLine1;
    private final UIText debugLine2;
    private final UIText debugLine3;
    private final UIText debugLine4;

    private final UIItemContainer toolbar;
    private final UIMinionbar minionbar;
    private final UIMessageQueue messagequeue;
    //private final UIHealthBar healthBar;
    private final UIBuff buffBar;

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        maximize();
        
        _hearts = new UIGraphicsElement[10];

        // Create hearts
        for (int i = 0; i < 10; i++) {
            _hearts[i] = new UIGraphicsElement(AssetManager.loadTexture("engine:icons"));
            _hearts[i].setVisible(true);
            _hearts[i].setTextureSize(new Vector2f(9f, 9f));
            _hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f)); //106f for poison
            _hearts[i].setSize(new Vector2f(18f, 18f));
            _hearts[i].setPosition(new Vector2f(18f * i, 18f));

            addDisplayElement(_hearts[i]);
        }
        
        crosshair = new UIGraphicsElement(AssetManager.loadTexture("engine:gui"));
        crosshair.setTextureSize(new Vector2f(20f, 20f));
        crosshair.setTextureOrigin(new Vector2f(24f, 24f));
        crosshair.setSize(new Vector2f(40f, 40f));
        crosshair.setVisible(true);

        debugLine1 = new UIText(new Vector2f(4, 4));
        debugLine2 = new UIText(new Vector2f(4, 22));
        debugLine3 = new UIText(new Vector2f(4, 38));
        debugLine4 = new UIText(new Vector2f(4, 54));

        addDisplayElement(crosshair, "crosshair");
        addDisplayElement(debugLine1);
        addDisplayElement(debugLine2);
        addDisplayElement(debugLine3);
        addDisplayElement(debugLine4);

        toolbar = new UIItemContainer(9);
        toolbar.setVisible(true);
        addDisplayElement(toolbar);

        minionbar = new UIMinionbar();
        minionbar.setVisible(true);
        addDisplayElement(minionbar);

        messagequeue = new UIMessageQueue();
        messagequeue.setVisible(true);
        addDisplayElement(messagequeue);

        /*
        healthBar = new UIHealthBar();
        healthBar.setVisible(true);
        addDisplayElement(healthBar);
         */
        
        buffBar = new UIBuff();
        buffBar.setVisible(true);
        addDisplayElement(buffBar);

        CoreRegistry.get(EventSystem.class).registerEventHandler(this);
        
        update();
        layout();
    }

    public void update() {
        super.update();
        
        boolean enableDebug = Config.getInstance().isDebug();
        debugLine1.setVisible(enableDebug);
        debugLine2.setVisible(enableDebug);
        debugLine3.setVisible(enableDebug);
        debugLine4.setVisible(enableDebug);

        if (enableDebug) {
            CameraTargetSystem cameraTarget = CoreRegistry.get(CameraTargetSystem.class);
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
            Timer timer = CoreRegistry.get(Timer.class);
            debugLine1.setText(String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f", timer.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            debugLine2.setText(String.format("%s", cameraTarget.toString()));
            debugLine3.setText(String.format("%s", CoreRegistry.get(WorldRenderer.class)));
            debugLine4.setText(String.format("total vus: %s | active threads: %s", ChunkTessellator.getVertexArrayUpdateCount(), CoreRegistry.get(GameEngine.class).getActiveTaskCount()));
        }
    }
    
	private void updateHealthBar(int currentHealth, int maxHealth) {
        float healthRatio = (float) currentHealth / maxHealth;

        // Show/Hide hearts relatively to the available health points of the player
        for (int i = 0; i < 10; i++) {

            if (i < healthRatio * 10f)
                _hearts[i].setVisible(true);
            else
                _hearts[i].setVisible(false);

            //Show Poisoned Status with Green Hearts:
            PoisonedComponent poisoned = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PoisonedComponent.class);
            entityManager = CoreRegistry.get(EntityManager.class);
            for (EntityRef entity : entityManager.iteratorEntities(PoisonedComponent.class)) {
                if (poisoned.poisonDuration >= 1)
                    _hearts[i].setTextureOrigin(new Vector2f(106f, 0.0f));
                else
                	_hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }
            
            for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
                //For fixing the Green > Red hearts when cured:
                CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CuredComponent.class);
                entityManager = CoreRegistry.get(EntityManager.class);
                if (cured.cureDuration >= 1)
                    _hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
                else
                	_hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }
        }
	}
    
    @Override
    public void layout() {
    	super.layout();
    	
    	if (toolbar != null) {
    		toolbar.centerHorizontally();
    		toolbar.getPosition().y = Display.getHeight() - toolbar.getSize().y;
	        crosshair.setPosition(new Vector2f((Display.getWidth() / 2) - (crosshair.getSize().x / 2), (Display.getHeight() / 2) - (crosshair.getSize().y / 2)));
	        
	        for (int i = 0; i < 10; i++) {
	        	_hearts[i].getPosition().x = toolbar.getPosition().x + i * (_hearts[i].getSize().x + 2f);
	        	_hearts[i].getPosition().y = toolbar.getPosition().y - _hearts[i].getSize().y - 2f;
	        }    	
    	}
    }
    
    @Override
    public void open() {
        toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 8);
        layout();
        
    	super.open();
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    	
    }

    @ReceiveEvent(components = {MinionComponent.class})
    public void onMessageReceived(MinionMessageEvent event, EntityRef entityref) {
        messagequeue.addIconToQueue(event.getMinionMessage());
    }
    
    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onSelectedItemChanged(ChangedComponentEvent event, EntityRef entity) {
        for (UIItemCell cell : toolbar.getCells()) {
			cell.setSelectionRectangleEnable(false);
		}
        
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        toolbar.getCells().get(localPlayerComp.selectedTool).setSelectionRectangleEnable(true);
    }
    
	@ReceiveEvent(components = {LocalPlayerComponent.class, HealthComponent.class})
    public void onHealthChange(HealthChangedEvent event, EntityRef entityref) {    	
		updateHealthBar(event.getCurrentHealth(), event.getMaxHealth());
    }
}
