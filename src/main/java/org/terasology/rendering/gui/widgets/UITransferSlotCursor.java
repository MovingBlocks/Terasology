package org.terasology.rendering.gui.widgets;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class UITransferSlotCursor extends UIDisplayContainer {
    private UIItemIcon item;
    private LocalPlayer localPlayer;
    private SlotBasedInventoryManager inventoryManager;

    public UITransferSlotCursor() {
        this.localPlayer = CoreRegistry.get(LocalPlayer.class);
        this.inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        item = new UIItemIcon();
        addDisplayElement(item);
    }

    @Override
    public void update() {
        super.update();
        item.setItem(getTransferItem());
        item.setPosition(new Vector2f(Mouse.getX() - getSize().x / 2, Display.getHeight() - Mouse.getY() - getSize().y / 2));
    }

    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return inventoryManager.getItemInSlot(getTransferEntity(), 0);
    }

}
