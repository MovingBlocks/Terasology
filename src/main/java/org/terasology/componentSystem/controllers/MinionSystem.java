package org.terasology.componentSystem.controllers;

import com.sun.xml.internal.bind.v2.TODO;
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
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 17:54
 * Minionsystem gives you some control over the minions.
 * this is the home of the minionbar.
 */
public class MinionSystem implements EventHandlerSystem {

    // Number of dials in the menu
    private final int popupentries = 5;
    // to be used for naming
    private HashSet<String> Names = new HashSet<String>();
    // links dials in the menu to actions / AI behaviour in SimpleMinionAISystem
    private final String behaviourmenu = "minionbehaviour";
    // the right click dial menu
    private UIMinion minionbehaviourmenu;

    public void initialise() {
        Names.add("Begla");
    }

    // returns the inventory component that represents the minion toolbar
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

    // return Entityref for the currently selected minion in the toolbar
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

    //should be clear
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
        // make sure to remove the ref here, alse you can't resummon
        minionbar.MinionSlots.set(localPlayerComp.selectedMinion,EntityRef.NULL);
        return true;
    }

    // scrolls up down in the behaviour dial when in minion mode, might trigger change in SimpleMinionAI
    // TODO add events and parameters
    public void menuScroll(int wheelMoved){
        EntityRef minion = getSelectedMinion();
        MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
        int ordinal = ((minioncomp.minionBehaviour.ordinal() - wheelMoved / 120) % popupentries);
        while (ordinal < 0) ordinal+= popupentries;
        // this sets the behaviour atm
        minioncomp.minionBehaviour =  MinionComponent.MinionBehaviour.values()[ordinal];
    }

    // moves the selection in the minion toolbar when in minion mode
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

    // returns behaviour of the selected minion
    public MinionComponent.MinionBehaviour getSelectedBehaviour(){
        EntityRef minion = getSelectedMinion();
        MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
        if(minioncomp == null) return MinionComponent.MinionBehaviour.Move;
        return minioncomp.minionBehaviour;
    }

    // opens the minion dial menu while right mouse is being pressed down
    public void RightMouseDown(){
        minionbehaviourmenu = (UIMinion)GUIManager.getInstance().getWindowById(behaviourmenu);
        if(minionbehaviourmenu == null) {
            minionbehaviourmenu = new UIMinion();
            GUIManager.getInstance().addWindow(minionbehaviourmenu,behaviourmenu);
        }
        minionbehaviourmenu.setVisible(true);
    }

    // right mouse button released
    // TODO : linked to previous one, add events
    public void RightMouseReleased(){
        UIMinion minionbehaviourmenu = (UIMinion)GUIManager.getInstance().getWindowById(behaviourmenu);
        if(minionbehaviourmenu != null){
            GUIManager.getInstance().removeWindow(minionbehaviourmenu);
            //this check should be obsolete TODO test / remove
            if(GUIManager.getInstance().getWindowById("container") != null){
                GUIManager.getInstance().setFocusedWindow("container");
            }
        }
        setMinionSelectMode(false);
        // launches the 2 events that didn't belong in the AI system
        // TODO : group all behaviour in 1 place, add events
        if(getSelectedBehaviour() == MinionComponent.MinionBehaviour.Disappear){
            //UIConfirm confirm = new UIConfirm("Confirm minion dismissal", "Are you sure you want to dispose of your cure little cube? You will lose it's inventory content if you click yes.");
            //GUIManager.getInstance().addWindow(confirm,"confirm");
            DestroyActiveMinion();
        }
        else if (getSelectedBehaviour() == MinionComponent.MinionBehaviour.Inventory){
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            if(localPlayer == null) return;
            getSelectedMinion().send(new ActivateEvent(getSelectedMinion(), localPlayer.getEntity()));
        }
        // make sure to check, minion could be destroyed by now
        if(getSelectedMinion() != null){
            MinionComponent minioncomp = getSelectedMinion().getComponent(MinionComponent.class);
            getSelectedMinion().saveComponent(minioncomp);
        }
    }

    // sets the target of the selected minion when clicked,
    public void setTarget(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        //TODO : move code from helpman to minionsys
        GroovyHelpManager helpMan = new GroovyHelpManager();
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        if(!isMinionSelected())
        {
            helpMan.spawnCube(localPlayerComponent.selectedMinion);
            EntityRef minion = getSelectedMinion();
            MeshComponent meshComponent = minion.getComponent(MeshComponent.class);
            meshComponent.Name = "Begla";
            meshComponent.ID = localPlayerComponent.selectedMinion;
            minion.saveComponent(meshComponent);
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

    // check to see if minionmode is active
    public boolean MinionMode(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return false;
        return localPlayer.getEntity().getComponent(LocalPlayerComponent.class).minionMode;
    }

    // check to see if we have the behaviour menu open
    public boolean MinionSelect(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return false;
        return localPlayer.getEntity().getComponent(LocalPlayerComponent.class).minionSelect;
    }

    // check to see if the current slot has a minion or not
    public boolean isMinionSelected(){
        return getSelectedMinion() != EntityRef.NULL;
    }

    // switched by pressing X, changes between normal and minion mode
    public void switchMinionMode(){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComponent.minionMode = !localPlayerComponent.minionMode;
        localPlayer.getEntity().saveComponent(localPlayerComponent);
    }

    // set parameter to indicate behaviour menu is open
    public void setMinionSelectMode(boolean mode){
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer == null) return;
        LocalPlayerComponent localPlayerComponent = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComponent.minionSelect = mode;
        localPlayer.getEntity().saveComponent(localPlayerComponent);
    }
}
