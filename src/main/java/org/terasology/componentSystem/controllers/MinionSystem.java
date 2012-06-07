package org.terasology.componentSystem.controllers;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.input.binds.ToggleMinionModeButton;
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

    private final int popupentries = 9;
    private final String behaviourmenu = "minionbehaviour";
    private UIMinion minionbehaviourmenu;

    public void initialise() {
        minionbehaviourmenu = new UIMinion();
        GUIManager.getInstance().addWindow(minionbehaviourmenu,behaviourmenu);
    }

    /*@ReceiveEvent(components = LocalPlayerComponent.class)
    public void onToggleMinionMode(ToggleMinionModeButton event, EntityRef entity) {

    }

    public void processKeyboardInput(int key, boolean state, boolean repeatEvent) {
        switch (key) {
            case Keyboard.KEY_X:
                if (!repeatEvent && state) {
                    MinionSystem minionSystem = new MinionSystem();
                    minionSystem.switchMinionMode();
                }
                break;
        }
    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {
        // needed for the minion toolbar
        MinionSystem minionsys = new MinionSystem();
        if (wheelMoved != 0) {
            //check mode, act according TODO? use events?
            if (minionsys.MinionMode())
                if (minionsys.MinionSelect()) minionsys.menuScroll(wheelMoved);
                else minionsys.barScroll(wheelMoved);

        } else if (button == 1 && !state) {
            // triggers the selected behaviour of a minion
            minionsys.RightMouseReleased();

        } else if (state && (button == 0 || button == 1)) {
            processInteractions(button);
        }
    }

    private void processInteractions(int button) {
        MinionSystem minionsys = new MinionSystem();
        // Throttle interactions
        if (timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }

        EntityRef entity = localPlayer.getEntity();
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

        if (localPlayerComp.isDead) return;

        if (minionsys.MinionMode()) {
            if (button == 1) {
                if (minionsys.isMinionSelected()) {
                    // opens the minion behaviour menu
                    lastInteraction = timer.getTimeInMs();
                    minionsys.RightMouseDown();
                    minionsys.setMinionSelectMode(true);
                }
            } else {
                if (Mouse.isButtonDown(0) || button == 0) {
                    // used to set targets for the minion
                    lastInteraction = timer.getTimeInMs();
                    minionsys.setTarget();
                }
            }
        }

    } */

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
        GUIManager.getInstance().setFocusedWindow(minionbehaviourmenu);
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
                    minionai.followingPlayer = false;
                    switch (getSelectedBehaviour())
                    {
                        case Follow: {
                            minionai.movementTarget = new Vector3f(centerPos.x, centerPos.y, centerPos.z);
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Move: {
                            minionai.movementTargets.add(new Vector3f(centerPos.x, centerPos.y, centerPos.z));
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Gather: {
                            minionai.gatherTargets.add(new Vector3f(centerPos.x, centerPos.y, centerPos.z));
                            getSelectedMinion().saveComponent(minionai);
                            break;
                        }
                        case Patrol: {
                            minionai.patrolTargets.add(new Vector3f(centerPos.x, centerPos.y, centerPos.z));
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
}