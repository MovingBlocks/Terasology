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

import org.terasology.asset.Assets;
import org.terasology.components.DrowningComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.config.Config;
import org.terasology.config.SystemConfig;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.events.BreathMeterUpdateEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.SwimEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.VisibilityListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 *         <p/>
 *         TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class UIScreenHUD extends UIWindow implements EventHandlerSystem {

    protected EntityManager entityManager;
    private Timer timer;

    /* DISPLAY ELEMENTS */
    private final UIImage[] hearts;
    private final int NUM_HEART_ICONS = 10;
    private final UIImage[] breathBubbles;
    private final int NUM_BUBBLE_ICONS = 10;
    private final UIImage crosshair;
    private final UILabel debugLine1;
    private final UILabel debugLine2;
    private final UILabel debugLine3;
    private final UILabel debugLine4;

    private final UIItemContainer toolbar;

    private final UIImage leftGearWheel;
    private final UIImage rightGearWheel;

    private final Config config = CoreRegistry.get(Config.class);

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        timer = CoreRegistry.get(Timer.class);

        setId("hud");
        maximize();

        addVisibilityListener(new VisibilityListener() {
            @Override
            public void changed(UIDisplayElement element, boolean visibility) {
                if (visibility) {
                    toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 9);
                }
            }
        });

        hearts = new UIImage[NUM_HEART_ICONS];

        // Create hearts
        for (int i = 0; i < NUM_HEART_ICONS; i++) {
            hearts[i] = new UIImage(Assets.getTexture("engine:icons"));
            hearts[i].setVisible(true);
            hearts[i].setTextureSize(new Vector2f(9f, 9f));
            hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f)); //106f for poison
            hearts[i].setSize(new Vector2f(18f, 18f));
            hearts[i].setVerticalAlign(EVerticalAlign.BOTTOM);
            hearts[i].setHorizontalAlign(EHorizontalAlign.CENTER);
            hearts[i].setPosition(new Vector2f(18f * i - 212f, -52f));

            addDisplayElement(hearts[i]);
        }

        breathBubbles = new UIImage[NUM_BUBBLE_ICONS];

        // Create breath meter
        for (int i = 0; i < NUM_BUBBLE_ICONS; i++){
            breathBubbles[i] = new UIImage(Assets.getTexture("engine:icons"));
            breathBubbles[i].setVisible(true);
            breathBubbles[i].setTextureSize(new Vector2f(9f, 9f));
            breathBubbles[i].setTextureOrigin(new Vector2f(16f, 18f)); // Bubble icon
            breathBubbles[i].setSize(new Vector2f(18f, 18f));
            breathBubbles[i].setVerticalAlign(EVerticalAlign.BOTTOM);
            breathBubbles[i].setHorizontalAlign(EHorizontalAlign.CENTER);
            breathBubbles[i].setPosition(new Vector2f(-18f * i + 210f, -52f));

            addDisplayElement(breathBubbles[i]);
        }

        crosshair = new UIImage(Assets.getTexture("engine:gui"));
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

        toolbar = new UIItemContainer(10);
        toolbar.setId("toolbar");
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);

        toolbar.setVisible(true);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        leftGearWheel = new UIImage(Assets.getTexture("engine:inventory"));

        leftGearWheel.setSize(new Vector2f(36f, 36f));
        leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        leftGearWheel.setId("leftGearWheel");
        leftGearWheel.setVisible(true);

        leftGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        leftGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        leftGearWheel.setPosition(new Vector2f(
                leftGearWheel.getPosition().x - 240f,
                leftGearWheel.getPosition().y - 4f)
        );

        rightGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
        rightGearWheel.setSize(new Vector2f(36f, 36f));
        rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        rightGearWheel.setId("rightGearWheel");
        rightGearWheel.setVisible(true);

        rightGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        rightGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        rightGearWheel.setPosition(new Vector2f(
                rightGearWheel.getPosition().x + 240f,
                rightGearWheel.getPosition().y - 4f)
        );

        addDisplayElement(crosshair);
        addDisplayElement(rightGearWheel);
        addDisplayElement(leftGearWheel);
        addDisplayElement(debugLine1);
        addDisplayElement(debugLine2);
        addDisplayElement(debugLine3);
        addDisplayElement(debugLine4);

        addDisplayElement(toolbar);

        CoreRegistry.get(EventSystem.class).registerEventHandler(this);

        update();
        layout();
    }

    @Override
    public void update() {
        super.update();

        boolean enableDebug = config.getSystem().isDebugEnabled();
        debugLine1.setVisible(enableDebug);
        debugLine2.setVisible(enableDebug);
        debugLine3.setVisible(enableDebug);
        debugLine4.setVisible(enableDebug);

        if (enableDebug) {
            CameraTargetSystem cameraTarget = CoreRegistry.get(CameraTargetSystem.class);
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
            Timer timer = CoreRegistry.get(Timer.class);
            debugLine1.setText(String.format("fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f", timer.getFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            if (entityManager != null) {
                debugLine2.setText(String.format("Active Entities: %s, Current Target: %s", entityManager.getActiveEntities(), cameraTarget.toString()));
            }
            debugLine3.setText(String.format("%s", CoreRegistry.get(WorldRenderer.class)));
            debugLine4.setText(String.format("total vus: %s | active threads: %s | debug rendering mode: %s",
                    ChunkTessellator.getVertexArrayUpdateCount(), CoreRegistry.get(GameEngine.class).getActiveTaskCount(),
                    SystemConfig.DebugRenderingStages.values()[config.getSystem().getDebugRenderingStage()].toString()));
        }

    }

    private void updateHealthBar(int currentHealth, int maxHealth) {
        float healthRatio = (float) currentHealth / maxHealth;

        // Show/Hide hearts relatively to the available health points of the player
        for (int i = 0; i < NUM_HEART_ICONS; i++) {

            if (i < healthRatio * 10f)
                hearts[i].setVisible(true);
            else
                hearts[i].setVisible(false);

            // TODO: Need to reimplement this in some way, maybe expose a method to change the health icon
            //Show Poisoned Status with Green Hearts:
            /*PoisonedComponent poisoned = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PoisonedComponent.class);
            entityManager = CoreRegistry.get(EntityManager.class);
            for (EntityRef entity : entityManager.iteratorEntities(PoisonedComponent.class)) {
                if (poisoned.poisonDuration >= 1)
                    hearts[i].setTextureOrigin(new Vector2f(106f, 0.0f));
                else
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }
            
            for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
                //For fixing the Green > Red hearts when cured:
                CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CuredComponent.class);
                entityManager = CoreRegistry.get(EntityManager.class);
                if (cured.cureDuration >= 1)
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
                else
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }*/
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {

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

    @ReceiveEvent(components = {LocalPlayerComponent.class, DrowningComponent.class})
    public void onBreathUpdate(BreathMeterUpdateEvent event, EntityRef entity){
        DrowningComponent drownComp = entity.getComponent(DrowningComponent.class);

        if(drownComp.underWater){
            // Calculate ratios
            int currentBreath = (int)(timer.getTimeInMs() - drownComp.timeEnteredLiquid);
            int maxBreath = (int)drownComp.timeBeforeDrown;
            float remainingBreathRatio = 0f;
            if(maxBreath != 0){
                remainingBreathRatio = 1 - ((float)currentBreath / maxBreath);
            }

            // Show or hide bubble icons depending on the amount of remaining breath
            for (int i = 0; i < NUM_BUBBLE_ICONS; i++){
                if(remainingBreathRatio > 0){
                    breathBubbles[i].setVisible(i < remainingBreathRatio * NUM_BUBBLE_ICONS);

                    // Decide the correct icon to use
                    if((remainingBreathRatio * NUM_BUBBLE_ICONS) - i < 0.5f){
                        breathBubbles[i].setTextureOrigin(new Vector2f(25f, 18f)); // Popped bubble icon
                    }else{
                        breathBubbles[i].setTextureOrigin(new Vector2f(16f, 18f)); // Regular bubble icon
                    }
                }else{
                    breathBubbles[i].setVisible(false);
                }
            }
        }else{
            // Hide bubbles when not underwater
            for (int i = 0; i < NUM_BUBBLE_ICONS; i++){
                breathBubbles[i].setVisible(false);
            }
        }
    }
}
