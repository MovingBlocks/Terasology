package org.terasology.rendering.gui.menus;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.rendering.gui.components.UIInventoryNew;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;

import javax.vecmath.Vector2f;

/**
 * Displays two inventories, and allows moving items between them
 * @author Immortius <immortius@gmail.com>
 */
public class UIContainerScreen  extends UIDisplayRenderer implements UIInventoryNew.InventorySubscriber{
    private final InventorySystem inventorySystem;

    EntityRef container;
    EntityRef creature;

    private final UIInventoryNew playerInventory;
    private final UIInventoryNew containerInventory;

    public UIContainerScreen(EntityRef container, EntityRef creature) {
        this.container = container;
        this.creature = creature;
        inventorySystem = CoreRegistry.get(ComponentSystemManager.class).get(InventorySystem.class);

        playerInventory = new UIInventoryNew(creature, 4);
        playerInventory.setPosition(new Vector2f(0.45f * Display.getWidth() - playerInventory.getSize().x, 0));
        playerInventory.setVisible(true);
        playerInventory.subscribe(this);
        addDisplayElement(playerInventory);

        containerInventory = new UIInventoryNew(container, 4);
        containerInventory.setPosition(new Vector2f(0.55f * Display.getWidth(), 0));
        containerInventory.setVisible(true);
        containerInventory.subscribe(this);
        addDisplayElement(containerInventory);
        setVisible(true);

        update();
    }

    @Override
    public void update() {
        super.update();
        playerInventory.centerVertically();
        containerInventory.centerVertically();
    }

    @Override
    public void processKeyboardInput(int key) {
        if (!isVisible())
            return;

        super.processKeyboardInput(key);
        if (key == Keyboard.KEY_E) {
            Terasology.getInstance().getCurrentGameState().closeScreen();
        }
    }

    public void itemClicked(UIInventoryNew inventoryNew, int slot) {
        EntityRef fromEntity = inventoryNew.getEntity();
        EntityRef toEntity = (fromEntity.equals(container)) ? creature : container;
        InventoryComponent fromInventory = fromEntity.getComponent(InventoryComponent.class);
        if (fromInventory == null)
            return;
        EntityRef itemEntity = fromInventory.itemSlots.get(slot);
        if (inventorySystem.addItem(toEntity, itemEntity)) {
            fromInventory.itemSlots.set(slot, EntityRef.NULL);
        }
    }
}