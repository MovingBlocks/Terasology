package org.terasology.mods.miniions.gui.components;

import org.lwjgl.opengl.Display;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.framework.*;

import javax.vecmath.Vector2f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 8/05/12
 * Time: 20:25
 * Should actually be renamed minionBehaviourMenu for consistency
 * used as a dial menu without ungrabbing the mouse
 */
public class UIMinion extends UIDisplayWindow{

    //private UIButton buttonMove;
    private final UIGraphicsElement _background;
    private final UIGraphicsElement _selectionRectangle;

    private MinionComponent.MinionBehaviour _Ohbehave;
    private int _selectedMinion;

    public UIMinion(){

        setSize(new Vector2f(60f,180f));
        _background = new UIGraphicsElement(AssetManager.loadTexture("engine:guiMinion"));
        _background.getTextureSize().set(new Vector2f(60f / 256f, 180f / 256f));
        _background.getTextureOrigin().set(new Vector2f(30.0f / 256f, 20.0f / 256f));
        _background.setSize(getSize());
        addDisplayElement(_background);
        _background.setVisible(true);

        _selectionRectangle = new UIGraphicsElement(AssetManager.loadTexture("engine:guiMinion"));
        _selectionRectangle.getTextureSize().set(new Vector2f(60f / 256f, 20f / 256f));
        _selectionRectangle.getTextureOrigin().set(new Vector2f(30f / 256, 0.0f));
        _selectionRectangle.setSize(new Vector2f(60f, 20f));
        _selectionRectangle.setVisible(true);
        addDisplayElement(_selectionRectangle);

    }

    @Override
    public void update() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if(localPlayer != null){
            LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
            if (localPlayerComp != null) {
                _selectedMinion = localPlayerComp.selectedMinion;
            }

            MinionBarComponent inventory = localPlayer.getEntity().getComponent(MinionBarComponent.class);
            if (inventory == null)
                return;
            EntityRef minion = inventory.MinionSlots.get(_selectedMinion);
            if(minion != null){
                MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
                if(minioncomp != null){
                    int selection = 20 * (minioncomp.minionBehaviour.ordinal());
                    int startpos = (44 *(6-(_selectedMinion+1)));
                    _selectionRectangle.setPosition(new Vector2f(Display.getWidth()-(100),(Display.getHeight()/2) - startpos + selection));
                    //setPosition(new Vector2f(2f, (getSize().y - 8f) * _selectedMinion - 2f));
                    _background.setPosition(new Vector2f(Display.getWidth()-(100),(Display.getHeight()/2) - startpos)); //(25 *(6-(_selectedMinion+1)))
                }
            }
        }
        super.update();
    }

}
