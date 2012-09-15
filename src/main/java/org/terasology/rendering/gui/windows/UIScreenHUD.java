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
import javax.vecmath.Vector4f;

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
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.widgets.UIBuff;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * 
 * TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class UIScreenHUD extends UIWindow implements EventHandlerSystem {

    protected EntityManager entityManager;

    /* DISPLAY ELEMENTS */
    private final UIImage[] _hearts;
    private final UIImage crosshair;
    private final UILabel debugLine1;
    private final UILabel debugLine2;
    private final UILabel debugLine3;
    private final UILabel debugLine4;

    private final UIItemContainer toolbar;
    private final UIMinionbar minionbar;
    private final UIMessageQueue messagequeue;
    //private final UIHealthBar healthBar;
    private final UIBuff buffBar;

    private final UIImage leftGearWheel;
    private final UIImage rightGearWheel;

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        setId("hud");
        maximize();
        
        _hearts = new UIImage[10];

        // Create hearts
        for (int i = 0; i < 10; i++) {
            _hearts[i] = new UIImage(AssetManager.loadTexture("engine:icons"));
            _hearts[i].setVisible(true);
            _hearts[i].setTextureSize(new Vector2f(9f, 9f));
            _hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f)); //106f for poison
            _hearts[i].setSize(new Vector2f(18f, 18f));
            _hearts[i].setVerticalAlign(EVerticalAlign.BOTTOM);
            _hearts[i].setHorizontalAlign(EHorizontalAlign.CENTER);
            _hearts[i].setPosition(new Vector2f(18f * i - 212f, -52f));

            addDisplayElement(_hearts[i]);
        }
        
        crosshair = new UIImage(AssetManager.loadTexture("engine:gui"));
        crosshair.setId("crosshair");
        crosshair.setTextureSize(new Vector2f(20f, 20f));
        crosshair.setTextureOrigin(new Vector2f(24f, 24f));
        crosshair.setSize(new Vector2f(40f, 40f));
        crosshair.setHorizontalAlign(EHorizontalAlign.CENTER);
        crosshair.setVerticalAlign(EVerticalAlign.CENTER);
        crosshair.setVisible(true);

        debugLine1 = new UILabel();
        debugLine1.setPosition(new Vector2f(4, 4));
        debugLine2 = new UILabel();
        debugLine2.setPosition(new Vector2f(4, 22));
        debugLine3 = new UILabel();
        debugLine3.setPosition(new Vector2f(4, 38));
        debugLine4 = new UILabel();
        debugLine4.setPosition(new Vector2f(4, 54));

        toolbar = new UIItemContainer(9);
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);

        toolbar.setVisible(true);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        minionbar = new UIMinionbar();
        minionbar.setVisible(true);

        messagequeue = new UIMessageQueue();
        messagequeue.setVisible(true);
        
        buffBar = new UIBuff();
        buffBar.setVisible(true);


        addDisplayElement(crosshair);

        leftGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        leftGearWheel.setSize(new Vector2f(36f, 36f));
        leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        leftGearWheel.setVisible(true);
        leftGearWheel.setId("leftGearWheel");

        rightGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        rightGearWheel.setSize(new Vector2f(36f, 36f));
        rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        rightGearWheel.setVisible(true);
        rightGearWheel.setId("rightGearWheel");

        addDisplayElement(rightGearWheel);
        addDisplayElement(leftGearWheel);

        addDisplayElement(debugLine1);
        addDisplayElement(debugLine2);
        addDisplayElement(debugLine3);
        addDisplayElement(debugLine4);

        addDisplayElement(toolbar);
        addDisplayElement(minionbar);
        addDisplayElement(messagequeue);
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

        if(leftGearWheel != null && rightGearWheel != null){
            leftGearWheel.setPosition(new Vector2f(
                    toolbar.getPosition().x - leftGearWheel.getSize().x/2,
                    toolbar.getPosition().y)
            );
            rightGearWheel.setPosition(new Vector2f(
                    toolbar.getPosition().x + toolbar.getSize().x - rightGearWheel.getSize().x/2,
                    toolbar.getPosition().y)
            );
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
    public void open() {
        toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 8);
        leftGearWheel.setVisible(true);
        rightGearWheel.setVisible(true);
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
            cell.setSelection(false);
        }
        
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        toolbar.getCells().get(localPlayerComp.selectedTool).setSelection(true);
    }
    
    @ReceiveEvent(components = {LocalPlayerComponent.class, HealthComponent.class})
    public void onHealthChange(HealthChangedEvent event, EntityRef entityref) {        
        updateHealthBar(event.getCurrentHealth(), event.getMaxHealth());
    }
}
