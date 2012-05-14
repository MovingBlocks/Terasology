package org.terasology.componentSystem.controllers;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.GroovyHelpManager;
import org.terasology.math.Vector3i;
import org.terasology.rendering.gui.components.UIMinion;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 17:54
 * Minionsystem gives you some control over the minions.
 * this is the home of the minionbar.
 */
public class MinionSystem implements EventHandlerSystem {

    private final int popupentries = 5;
    private final String behaviourmenu = "minionbehaviour";
    private UIMinion minionbehaviourmenu;

    public void initialise() {}

    public MinionBarComponent getMinionBar(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return null;
        EntityRef entity = localPlayer.getEntity();
        if(entity == null) return null;
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if(localPlayerComp == null) return null;
        MinionBarComponent minionbar = localPlayer.getEntity().getComponent(MinionBarComponent.class);
        return minionbar;
    }

    public EntityRef getSelectedMinion(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return null;
        EntityRef entity = localPlayer.getEntity();
        if(entity == null) return null;
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if(localPlayerComp == null) return null;
        MinionBarComponent minionbar = localPlayer.getEntity().getComponent(MinionBarComponent.class);
        if(minionbar == null) return null;
        EntityRef minion = minionbar.MinionSlots.get(localPlayerComp.selectedMinion);
        return minion;
    }

    public boolean DestroyActiveMinion(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return false;
        EntityRef entity = localPlayer.getEntity();
        if(entity == null) return false;
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if(localPlayerComp == null) return false;
        MinionBarComponent minionbar = localPlayer.getEntity().getComponent(MinionBarComponent.class);
        if(minionbar == null) return false;
        EntityRef minion = minionbar.MinionSlots.get(localPlayerComp.selectedMinion);
        if(minion != null) minion.destroy();
        minionbar.MinionSlots.set(localPlayerComp.selectedMinion,EntityRef.NULL);
        return true;
    }

    public void menuScroll(int wheelMoved){
        EntityRef minion = getSelectedMinion();
        MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
        int ordinal = ((minioncomp.minionBehaviour.ordinal() - wheelMoved / 120) % popupentries);
        while (ordinal < 0) ordinal+= popupentries;
        minioncomp.minionBehaviour = MinionComponent.MinionBehaviour.values()[ordinal];
    }

    public void barScroll(int wheelMoved){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        EntityRef entity = localPlayer.getEntity();
        if(entity == null) return;
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if(localPlayerComp == null) return;
        localPlayerComp.selectedMinion = (localPlayerComp.selectedMinion - wheelMoved / 120) % 9;
        while (localPlayerComp.selectedMinion < 0) {
            localPlayerComp.selectedMinion = 9 + localPlayerComp.selectedMinion;
        }
        localPlayer.getEntity().saveComponent(localPlayerComp);
    }

    public MinionComponent.MinionBehaviour getSelectedBehaviour(){
        EntityRef minion = getSelectedMinion();
        MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
        if(minioncomp == null) return MinionComponent.MinionBehaviour.Move;
        return minioncomp.minionBehaviour;
    }

    public void RightMouseDown(){
        minionbehaviourmenu = (UIMinion)GUIManager.getInstance().getWindowById(behaviourmenu);
        if(minionbehaviourmenu == null) {
            minionbehaviourmenu = new UIMinion();
            GUIManager.getInstance().addWindow(minionbehaviourmenu,behaviourmenu);
        }
        minionbehaviourmenu.setVisible(true);
    }

    public void RightMouseReleased(){
        UIMinion minionbehaviourmenu = (UIMinion)GUIManager.getInstance().getWindowById(behaviourmenu);
        if(minionbehaviourmenu != null){
            GUIManager.getInstance().removeWindow(minionbehaviourmenu);
            if(GUIManager.getInstance().getWindowById("container") != null){
                GUIManager.getInstance().setFocusedWindow("container");
            }
        }
        setMinionSelectMode(false);
        if(getSelectedBehaviour() == MinionComponent.MinionBehaviour.Disappear){
            //UIConfirm confirm = new UIConfirm("Confirm minion dismissal", "Are you sure you want to dispose of your cure little cube? You will lose it's inventory content if you click yes.");
            //GUIManager.getInstance().addWindow(confirm,"confirm");
            DestroyActiveMinion();
        }
        else if (getSelectedBehaviour() == MinionComponent.MinionBehaviour.Inventory){
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            if(localPlayer == null) return;
            getSelectedMinion().send(new ActivateEvent(getSelectedMinion(), localPlayer.getEntity()));
            MinionComponent minioncomp = getSelectedMinion().getComponent(MinionComponent.class);
            minioncomp.minionBehaviour = MinionComponent.MinionBehaviour.Move;
        }
        MinionComponent minioncomp = getSelectedMinion().getComponent(MinionComponent.class);
        getSelectedMinion().saveComponent(minioncomp);

    }

    public void setTarget(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        GroovyHelpManager helpMan = new GroovyHelpManager();
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        if(!isMinionSelected())
        {
            helpMan.spawnCube(localPlayerComponent.selectedMinion);
        }
        else
        {
            SimpleMinionAIComponent minionai = getSelectedMinion().getComponent(SimpleMinionAIComponent.class);
            if(minionai != null){
                if(helpMan.calcSelectedBlock() != null)
                {
                    Vector3i centerPos = helpMan.calcSelectedBlock().getBlockPosition();
                    minionai.followingPlayer = false;
                    minionai.movementTarget = new Vector3f(centerPos.x, centerPos.y, centerPos.z);
                }
            }
        }
    }

    public boolean MinionMode(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return false;
        return localPlayer.getEntity().getComponent(LocalPlayerComponent.class).minionMode;
    }

    public boolean MinionSelect(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return false;
        return localPlayer.getEntity().getComponent(LocalPlayerComponent.class).minionSelect;
    }

    public boolean isMinionSelected(){
        return getSelectedMinion() != EntityRef.NULL;
    }

    public void switchMinionMode(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComponent.minionMode = !localPlayerComponent.minionMode;
        localPlayer.getEntity().saveComponent(localPlayerComponent);
    }

    public void setMinionSelectMode(boolean mode){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComponent.minionSelect = mode;
        localPlayer.getEntity().saveComponent(localPlayerComponent);
    }
}