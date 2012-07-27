package org.terasology.mods.miniions.rendering.gui.components;

import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.asset.AssetManager;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 8/05/12
 * Time: 20:25
 * Should actually be renamed minionBehaviourMenu for consistency
 * used as a dial menu without ungrabbing the mouse
 */
public class UIMinion extends UIDisplayWindow {

    //private UIButton buttonMove;
    private final UIGraphicsElement background;
    private final UIGraphicsElement selectionrectangle;

    private int selectedMinion;

    public UIMinion() {

        setSize(new Vector2f(60f, 180f));
        background = new UIGraphicsElement(AssetManager.loadTexture("engine:guiMinion"));
        background.getTextureSize().set(new Vector2f(60f / 256f, 180f / 256f));
        background.getTextureOrigin().set(new Vector2f(30.0f / 256f, 20.0f / 256f));
        background.setSize(getSize());
        addDisplayElement(background);
        background.setVisible(true);

        selectionrectangle = new UIGraphicsElement(AssetManager.loadTexture("engine:guiMinion"));
        selectionrectangle.getTextureSize().set(new Vector2f(60f / 256f, 20f / 256f));
        selectionrectangle.getTextureOrigin().set(new Vector2f(30f / 256, 0.0f));
        selectionrectangle.setSize(new Vector2f(60f, 20f));
        selectionrectangle.setVisible(true);
        addDisplayElement(selectionrectangle);

    }

    @Override
    public void update() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if (localPlayer != null) {
            MinionControllerComponent minionController = localPlayer.getEntity().getComponent(MinionControllerComponent.class);
            if (minionController != null) {
                selectedMinion = minionController.selectedMinion;
            }

            MinionBarComponent inventory = localPlayer.getEntity().getComponent(MinionBarComponent.class);
            if (inventory == null) {
                return;
            }
            EntityRef minion = inventory.minionSlots.get(selectedMinion);
            if (minion != null) {
                MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
                if (minioncomp != null) {
                    int selection = 20 * (minioncomp.minionBehaviour.ordinal());
                    int startpos = (44 * (6 - (selectedMinion + 1)));
                    selectionrectangle.setPosition(new Vector2f(Display.getWidth() - (100), (Display.getHeight() / 2) - startpos + selection));
                    //setPosition(new Vector2f(2f, (getSize().y - 8f) * selectedMinion - 2f));
                    background.setPosition(new Vector2f(Display.getWidth() - (100), (Display.getHeight() / 2) - startpos)); //(25 *(6-(selectedMinion+1)))
                }
            }
        }
        super.update();
    }

}
