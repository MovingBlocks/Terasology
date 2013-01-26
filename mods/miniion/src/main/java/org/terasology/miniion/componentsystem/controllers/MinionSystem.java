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

import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.terasology.asset.Assets;
import org.terasology.components.*;
import org.terasology.components.world.*;

import org.terasology.rendering.gui.events.UIWindowOpenedEvent;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.logic.*;
import org.terasology.world.block.*;
import org.terasology.entityFactory.*;
import org.terasology.entitySystem.*;
import org.terasology.events.*;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.binds.*;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.*;
import org.terasology.miniion.componentsystem.entityfactory.MiniionFactory;
import org.terasology.miniion.events.MinionMessageEvent;
import org.terasology.miniion.events.ToggleMinionModeButton;
import org.terasology.miniion.gui.UICardBook;
import org.terasology.miniion.gui.UIScreenBookOreo;
import org.terasology.miniion.gui.UIZoneBook;
import org.terasology.miniion.minionenum.*;
import org.terasology.miniion.utilities.*;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 17:54
 * Minionsystem gives you some control over the minions.
 * this is the home of the minionbar.
 */
@RegisterComponentSystem
public class MinionSystem implements EventHandlerSystem {

    private static final int PRIORITY_LOCAL_PLAYER_OVERRIDE = 160;
    private static final int POPUP_ENTRIES = 9;
    private static final String BEHAVIOUR_MENU = "minionBehaviour";
    private static final String MENU_TEST = "minionTest";
    private static EntityRef activeminion;
    //TODO : a better way to save / load zones, but it does the trick
    private static EntityRef zonelist;
    
    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    
    private GUIManager guiManager;
    private BlockItemFactory blockItemFactory;
    private DroppedBlockFactory droppedBlockFactory;
    private UIMinionBehaviourMenu minionMenu;
    private UIMinionTestMenu minionTest;
    private MiniionFactory minionFactory;
    private UIMessageQueue messageQueue;

    @Override
    public void initialise() {
    	ModIcons.loadIcons();
        guiManager = CoreRegistry.get(GUIManager.class);
        //minionFactory = new MiniionFactory();
        blockItemFactory = new BlockItemFactory(entityManager);
        droppedBlockFactory = new DroppedBlockFactory(entityManager);
        //minionFactory.setEntityManager(entityManager);
        //minionFactory.setRandom(new FastRandom());
        //guiManager.registerWindow("minionBehaviour", UIMinionBehaviourMenu.class);
        guiManager.registerWindow("minionTest", UIMinionTestMenu.class); // experimental popup menu for the minion command tool
        guiManager.registerWindow("cardbook", UICardBook.class);		// ui to create summonable cards
        guiManager.registerWindow("oreobook", UIScreenBookOreo.class);  // ui to manage summoned minions, selecting one sets it active!
        guiManager.registerWindow("zonebook", UIZoneBook.class);  // ui to manage zones
        createZoneList();
    }
    
    
    /**
     * Ugly way to retrieve a name from a prefab
     * @return a ":" seperated string, with name and flavor text.
     */
    public static String getName(){
    	PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        Prefab prefab = prefMan.getPrefab("miniion:nameslist");
    	EntityRef namelist = CoreRegistry.get(EntityManager.class).create(prefab);
    	namelist.hasComponent(namesComponent.class);
    	namesComponent namecomp = namelist.getComponent(namesComponent.class);
    	Random rand = new Random();
    	return namecomp.namelist.get(rand.nextInt(namecomp.namelist.size()));
    }
    
    
    /**
     * destroys a minion at the end of their dying animation
     * this implies that setting their animation to die will destroy them.
     */
    @ReceiveEvent(components = {SkeletalMeshComponent.class, AnimationComponent.class})
    public void onAnimationEnd(AnimEndEvent event, EntityRef entity){
    	AnimationComponent animcomp = entity.getComponent(AnimationComponent.class);
    	if(animcomp != null && event.getAnimation().equals(animcomp.dieAnim)){
    			entity.destroy();
    	}
    }
    
    /**
     * triggered when a block was destroyed and dropped in the world
     * used to intercept gathering by minions and sending the block to their inventory
     * the droppedblock in the world then gets destroyed, possible duplication exploit
     */
    @ReceiveEvent(components = {BlockPickupComponent.class})
    public void onBlockDropped(BlockDroppedEvent event, EntityRef entity) {
    	if(event.getInstigator().hasComponent(MinionComponent.class)){
    		EntityRef item;
            if (event.getoldBlock().getEntityMode() == BlockEntityMode.PERSISTENT) {
                item = blockItemFactory.newInstance(event.getoldBlock().getBlockFamily(), entity);
            } else {
                item = blockItemFactory.newInstance(event.getoldBlock().getBlockFamily());
            }
            event.getInstigator().send(new ReceiveItemEvent(item));
            event.getDroppedBlock().destroy();
    	}
    }
    
    /**
     * The active minion, to be commanded by the minion command item etc
     * uses a slightly different texture to indicate selection
     * @param minion : the new active minion entity
     */
    public static void setActiveMinion(EntityRef minion){
    	SkeletalMeshComponent skelcomp;
    	if(activeminion != null){
	    	skelcomp= activeminion.getComponent(SkeletalMeshComponent.class);
			if(skelcomp != null){
				skelcomp.material = Assets.getMaterial("OreoMinions:OreonSkin");
				activeminion.saveComponent(skelcomp);
			}
    	}
    	skelcomp= minion.getComponent(SkeletalMeshComponent.class);
		if(skelcomp != null){
			skelcomp.material = Assets.getMaterial("OreoMinions:OreonSkinSelected");
			minion.saveComponent(skelcomp);
		}
    	activeminion = minion;
    }
    
    /**
     * returns the currently active minion
     * @return : the currently active minion
     */
    public static EntityRef getActiveMinion(){
    	return activeminion;
    }
    
    public static void addZone(Zone zone){
    	ZoneListComponent zonelistcomp = zonelist.getComponent(ZoneListComponent.class);    	
    	zonelistcomp.Gatherzones.add(zone);
    	zonelist.saveComponent(zonelistcomp);
    }
    
    public static List<Zone> getGatherZoneList(){
    	if(zonelist == null){
    		return null;
    	}
    	return zonelist.getComponent(ZoneListComponent.class).Gatherzones;
    }
    
    /**
     * bit of a forced way to create an entity
     * I bet Immortius weeps when he sees atrocities like these :D
     */
    private static void createZoneList(){
    	zonelist = CoreRegistry.get(EntityManager.class).create();
		ZoneListComponent zonecomp = new ZoneListComponent();
		zonelist.addComponent(zonecomp);
		zonelist.setPersisted(true);
		zonelist.saveComponent(zonecomp);
    }
    
    @Deprecated
    @ReceiveEvent(components = {WorldComponent.class})
    public void onWindowOpened(UIWindowOpenedEvent event, EntityRef entity) {
        /*if (event.getWindow().getId().equals("hud")) {
            UIMinionbar bar = new UIMinionbar();
            bar.setVisible(true);
            event.getWindow().addDisplayElement(bar);
            messageQueue = new UIMessageQueue();
            messageQueue.setVisible(true);
            event.getWindow().addDisplayElement(messageQueue);
            event.getWindow().update();
            event.getWindow().layout();
        }*/
    }

    @Deprecated
    @ReceiveEvent(components = {MinionComponent.class})
    public void onMessageReceived(MinionMessageEvent event, EntityRef entityref) {
        if (messageQueue != null) {
            messageQueue.addIconToQueue(event.getMinionMessage());
        }
    }

    @Override
    public void shutdown() {
    }

    @Deprecated
    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class})
    public void onToggleMinionMode(ToggleMinionModeButton event, EntityRef entity) {
        /*MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        minionController.minionMode = !minionController.minionMode;
        if (!minionController.minionMode) {
            guiManager.closeWindow(BEHAVIOUR_MENU);
            guiManager.closeWindow(MENU_TEST);
        }
        entity.saveComponent(minionController);
        event.consume();*/
    }

    @Deprecated
    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onNextMinion(ToolbarNextButton event, EntityRef entity) {
        MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            minionController.selectedMinion = (minionController.selectedMinion + 1) % 9;
            entity.saveComponent(minionController);
            event.consume();
        }
    }

    @Deprecated
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

    @Deprecated
    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class})
    public void onMouseWheel(MouseWheelEvent wheelEvent, EntityRef entity) {
        if (minionMenu != null && minionMenu.isVisible()) {
            menuScroll(wheelEvent.getWheelTurns(), entity);
            wheelEvent.consume();
        }
        if (minionTest != null && minionTest.isVisible()) {
	       menuScroll(wheelEvent.getWheelTurns(), entity);
	       wheelEvent.consume();	
	   }
    }

    @Deprecated
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

    //replaced by openuicomponent in items
    @Deprecated
    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onUseItem(UseItemButton event, EntityRef entity) {
        /*MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
        if (minionController.minionMode) {
            switch (event.getState()) {
                case DOWN:
                    minionMenu = (UIMinionBehaviourMenu) guiManager.openWindow(BEHAVIOUR_MENU);
                    minionTest = (UIMinionTestMenu) guiManager.openWindow(MENU_TEST);
                    break;
                case UP:
                    minionMenu.setVisible(false);
                    minionTest.setVisible(false);
                    updateBehaviour(entity);
                    break;
                default:
                    break;
            }
            event.consume();
        }*/
    }
    
    /**
     * overrides the default attack event if the minion command item is the current helditem
     * only adds gather targets for now, minion command needs popuup to set behaviour 
     */
    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class}, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
    public void onAttack(AttackButton event, EntityRef entity) {
        LocalPlayerComponent locplaycomp = entity.getComponent(LocalPlayerComponent.class);
        UIItemContainer toolbar = (UIItemContainer) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");
        int invSlotIndex = localPlayer.getEntity().getComponent(LocalPlayerComponent.class).selectedTool + toolbar.getSlotStart();
        EntityRef heldItem = localPlayer.getEntity().getComponent(InventoryComponent.class).itemSlots.get(invSlotIndex);
        ItemComponent heldItemComp = heldItem.getComponent(ItemComponent.class);

        if (heldItemComp != null && activeminion != null && heldItemComp.name.matches("Minion Command")) {
            SimpleMinionAIComponent aicomp = activeminion.getComponent(SimpleMinionAIComponent.class);
            LocationComponent loccomp = event.getTarget().getComponent(LocationComponent.class);
            aicomp.gatherTargets.add(loccomp.getWorldPosition());
            activeminion.saveComponent(aicomp);
            locplaycomp.handAnimation = 0.5f;
            entity.saveComponent(locplaycomp);
            event.consume();
        }
    }

    @Deprecated
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
                MinionMessage messagetosend = new MinionMessage(MinionMessagePriority.Debug, "test", "testdesc", "testcont", minion, localPlayer.getEntity());
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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
