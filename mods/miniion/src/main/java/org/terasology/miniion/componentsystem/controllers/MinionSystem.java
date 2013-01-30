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
package org.terasology.miniion.componentsystem.controllers;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.players.LocalPlayerComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.miniion.components.UIMessageQueue;
import org.terasology.miniion.components.UIMinionbar;
import org.terasology.rendering.gui.events.UIWindowOpenedEvent;
import org.terasology.world.block.BlockComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.binds.AttackButton;
import org.terasology.input.binds.ToolbarNextButton;
import org.terasology.input.binds.ToolbarPrevButton;
import org.terasology.input.binds.UseItemButton;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.MinionBarComponent;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.MinionControllerComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.componentsystem.entityfactory.MiniionFactory;
import org.terasology.miniion.events.MinionMessageEvent;
import org.terasology.miniion.events.ToggleMinionModeButton;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.miniion.minionenum.MinionMessagePriority;
import org.terasology.miniion.components.UIMinionBehaviourMenu;
import org.terasology.miniion.utilities.MinionMessage;
import org.terasology.utilities.FastRandom;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 17:54
 * Minionsystem gives you some control over the minions.
 * this is the home of the minionbar.
 */
@RegisterSystem
public class MinionSystem implements ComponentSystem {

    private static final int PRIORITY_LOCAL_PLAYER_OVERRIDE = 160;
    private static final int POPUP_ENTRIES = 9;
    private static final String BEHAVIOUR_MENU = "minionBehaviour";

    private GUIManager guiManager;
    private UIMinionBehaviourMenu minionMenu;
    private MiniionFactory minionFactory;
    private UIMessageQueue messageQueue;

    @Override
    public void initialise() {
        guiManager = CoreRegistry.get(GUIManager.class);
        minionFactory = new MiniionFactory();
        minionFactory.setEntityManager(CoreRegistry.get(EntityManager.class));
        minionFactory.setRandom(new FastRandom());
        guiManager.registerWindow("minionBehaviour", UIMinionBehaviourMenu.class);
    }

    @ReceiveEvent(components = {WorldComponent.class})
    public void onWindowOpened(UIWindowOpenedEvent event, EntityRef entity) {
        if (event.getWindow().getId().equals("hud")) {
            UIMinionbar bar = new UIMinionbar();
            bar.setVisible(true);
            event.getWindow().addDisplayElement(bar);
            messageQueue = new UIMessageQueue();
            messageQueue.setVisible(true);
            event.getWindow().addDisplayElement(messageQueue);
            event.getWindow().update();
            event.getWindow().layout();
        }
    }

    @ReceiveEvent(components = {MinionComponent.class})
    public void onMessageReceived(MinionMessageEvent event, EntityRef entityref) {
        if (messageQueue != null) {
            messageQueue.addIconToQueue(event.getMinionMessage());
        }
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class})
    public void onToggleMinionMode(ToggleMinionModeButton event, EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        minionController.minionMode = !minionController.minionMode;
        if (!minionController.minionMode) {
            guiManager.closeWindow(BEHAVIOUR_MENU);
        }
        entity.saveComponent(minionController);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onNextMinion(ToolbarNextButton event, EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            minionController.selectedMinion = (minionController.selectedMinion + 1) % 9;
            entity.saveComponent(minionController);
            event.consume();
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onPrevMinion(ToolbarPrevButton event, EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            minionController.selectedMinion = (minionController.selectedMinion - 1) % 9;
            if (minionController.selectedMinion < 0) {
                minionController.selectedMinion += 9;
            }
            entity.saveComponent(minionController);
            event.consume();
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class})
    public void onMouseWheel(MouseWheelEvent wheelEvent, EntityRef entity) {
        if (minionMenu != null && minionMenu.isVisible()) {
            menuScroll(wheelEvent.getWheelTurns(), entity);
            wheelEvent.consume();
        }
    }

    private void menuScroll(int wheelMoved, EntityRef playerEntity) {
        EntityRef minion = getSelectedMinion(playerEntity);
        MinionComponent minionComp = minion.getComponent(MinionComponent.class);
        if (minionComp == null) {
            return;
        }
        int ordinal = ((minionComp.minionBehaviour.ordinal() - wheelMoved) % POPUP_ENTRIES);
        while (ordinal < 0) {
            ordinal += POPUP_ENTRIES;
        }
        minionComp.minionBehaviour = MinionBehaviour.values()[ordinal];
        minion.saveComponent(minionComp);
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onAttack(AttackButton event, EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            switch (event.getState()) {
                case DOWN:
                    minionMenu = (UIMinionBehaviourMenu) guiManager.openWindow(BEHAVIOUR_MENU);
                    break;
                case UP:
                    minionMenu.setVisible(false);
                    updateBehaviour(entity);
                    break;
                default:
                    break;
            }
            event.consume();
        }
    }

    public void updateBehaviour(EntityRef player) {
        EntityRef minion = getSelectedMinion(player);
        MinionComponent minionComp = minion.getComponent(MinionComponent.class);
        if (minionComp == null) {
            return;
        }
        switch (minionComp.minionBehaviour) {
            case Clear: {
                SimpleMinionAIComponent minionai = minion.getComponent(SimpleMinionAIComponent.class);
                if (minionai == null) {
                    return;
                }
                minionai.ClearCommands();
                minionComp.minionBehaviour = MinionBehaviour.Stay;
                minion.saveComponent(minionai);
                minion.saveComponent(minionComp);
                break;
            }
            case Inventory: {
                minion.send(new ActivateEvent(minion, player));
                minionComp.minionBehaviour = MinionBehaviour.Stay;
                minion.saveComponent(minionComp);
                break;
            }
            case Test: {
                LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
                if (localPlayer == null) return;
                MinionMessage messagetosend = new MinionMessage(MinionMessagePriority.Debug, "test", "testdesc", "testcont", minion, localPlayer.getCharacterEntity());
                minion.send(new MinionMessageEvent(messagetosend));
                break;
            }
            case Disappear: {
                minion.destroy();
                break;
            }
            default:
                break;
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onUseMinion(UseItemButton event, EntityRef playerEntity) {
        MinionControllerComponent minionController = playerEntity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            EntityRef minion = getSelectedMinion(playerEntity);
            if (minion.exists()) {
                setTarget(minion, event.getTarget(), playerEntity);
            } else {
                createMinion(playerEntity, event.getTarget());
            }
            event.consume();
        }
    }

    private EntityRef getSelectedMinion(EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController == null) {
            return EntityRef.NULL;
        }
        MinionBarComponent minionbar = entity.getComponent(MinionBarComponent.class);
        if (minionbar == null) {
            return EntityRef.NULL;
        }
        EntityRef minion = minionbar.minionSlots.get(minionController.selectedMinion);
        return minion;
    }

    private void createMinion(EntityRef player, EntityRef target) {
        MinionControllerComponent minionController = player.getComponent(MinionControllerComponent.class);
        MinionBarComponent inventory = player.getComponent(MinionBarComponent.class);
        BlockComponent blockComp = target.getComponent(BlockComponent.class);
        if (blockComp == null || minionController == null || inventory == null) {
            return;
        }
        Vector3i centerPos = blockComp.getPosition();
        inventory.minionSlots.set(minionController.selectedMinion, minionFactory.generateMiniion(new Vector3f(centerPos.x, centerPos.y + 1, centerPos.z), minionController.selectedMinion));
    }


    private void setTarget(EntityRef minion, EntityRef target, EntityRef player) {
        BlockComponent blockComp = target.getComponent(BlockComponent.class);
        if (blockComp == null) {
            return;
        }
        SimpleMinionAIComponent minionai = minion.getComponent(SimpleMinionAIComponent.class);
        MinionComponent minionComp = minion.getComponent(MinionComponent.class);
        if (minionai == null || minionComp == null) {
            return;
        }

        Vector3i centerPos = blockComp.getPosition();
        Vector3f centerPosf = new Vector3f(centerPos.x, centerPos.y + 0.5f, centerPos.z);
        minionai.followingPlayer = false;
        switch (minionComp.minionBehaviour) {
            case Follow: {
                minionai.movementTarget = centerPosf;
                minion.saveComponent(minionai);
                break;
            }
            case Move: {
                minionai.movementTargets.add(centerPosf);
                minion.saveComponent(minionai);
                break;
            }
            case Gather: {
                minionai.gatherTargets.add(centerPosf);
                minion.saveComponent(minionai);
                break;
            }
            case Patrol: {
                minionai.patrolTargets.add(centerPosf);
                minion.saveComponent(minionai);
                break;
            }
            case Test: {
                minionai.movementTargets.add(centerPosf);
                minion.saveComponent(minionai);
                break;
            }
            default:
                break;
        }
    }

}
