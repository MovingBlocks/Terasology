package org.terasology.mods.miniions.components.componentsystem.controllers;

import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.GroovyHelpManager;
import org.terasology.math.Vector3i;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.SimpleMinionAIComponent;
import org.terasology.mods.miniions.rendering.gui.components.UIMinion;

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

    private final int popupentries = 9;
    private final String behaviourmenu = "minionbehaviour";
    private UIMinion minionbehaviourmenu;
    public enum Messages{
        NoPath,
        Idle
    }

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
        minion.saveComponent(minioncomp);
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
        if(minioncomp == null) return MinionComponent.MinionBehaviour.Stay;
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
        switch (getSelectedBehaviour()){
            case Clear:{
                SimpleMinionAIComponent minionai = getSelectedMinion().getComponent(SimpleMinionAIComponent.class);
                minionai.ClearCommands();
                getSelectedMinion().saveComponent(minionai);
                MinionComponent minioncomp = getSelectedMinion().getComponent(MinionComponent.class);
                minioncomp.minionBehaviour = MinionComponent.MinionBehaviour.Stay;
                getSelectedMinion().saveComponent(minioncomp);
                break;
            }
            case Inventory:{
                LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
                if(localPlayer == null) return;
                getSelectedMinion().send(new ActivateEvent(getSelectedMinion(), localPlayer.getEntity()));
                MinionComponent minioncomp = getSelectedMinion().getComponent(MinionComponent.class);
                minioncomp.minionBehaviour = MinionComponent.MinionBehaviour.Stay;
                getSelectedMinion().saveComponent(minioncomp);
                break;
            }
            case Test:{
                break;
            }
            case Disappear:{
                DestroyActiveMinion();
                break;
            }

        }
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
                    Vector3f centerPosf = new Vector3f(centerPos.x ,centerPos.y + 0.5f,centerPos.z);
                    minionai.followingPlayer = false;
                    switch (getSelectedBehaviour())
                    {
                        case Follow: {
                            minionai.movementTarget = centerPosf;
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Move: {
                            minionai.movementTargets.add(centerPosf);
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Gather: {
                            minionai.gatherTargets.add(centerPosf);
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Patrol: {
                            minionai.patrolTargets.add(centerPosf);
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Test: {
                            minionai.movementTargets.add(centerPosf);
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                    }
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

    public String[] getMessage(Messages messages){
        switch (messages){
            case NoPath:{
                String[] content = new String[3];
                content[0] = "Command not executed"; //description
                content[1] = "No Path found"; //Title
                content[2] = "The minion couldn't find a path to destination"; //body
                return content;
            }
            case Idle:{
                String[] content = new String[3];
                content[0] = "No more commands"; //description
                content[1] = "iddle minion"; //Title
                content[2] = "The minion has no more commands to execute and is now iddle"; //body
                return content;
            }
        }
        return null;
    }
}