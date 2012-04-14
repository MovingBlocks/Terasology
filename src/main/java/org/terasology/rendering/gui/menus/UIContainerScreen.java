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
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Displays two inventories, and allows moving items between them
 * @author Immortius <immortius@gmail.com>
 */
public class UIContainerScreen  extends UIDisplayRenderer implements UIInventoryNew.InventorySubscriber{
    private static final int CENTER_BORDER = 100;
    private static final int OUTER_BORDER = 50;
    private final InventorySystem inventorySystem;

    EntityRef container;
    EntityRef creature;

    private final UIInventoryNew playerInventory;
    private final UIInventoryNew containerInventory;
    private final UIGraphicsElement background;

    public UIContainerScreen(EntityRef container, EntityRef creature) {
        this.container = container;
        this.creature = creature;
        inventorySystem = CoreRegistry.get(ComponentSystemManager.class).get(InventorySystem.class);
        background = new UIGraphicsElement("containerWindow");
        background.getTextureSize().set(new Vector2f(256f / 256f, 231f / 256f));
        background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        addDisplayElement(background);

        playerInventory = new UIInventoryNew(creature, 4);
        playerInventory.setVisible(true);
        playerInventory.subscribe(this);
        addDisplayElement(playerInventory);

        containerInventory = new UIInventoryNew(container, 4);
        containerInventory.setVisible(true);
        containerInventory.subscribe(this);
        addDisplayElement(containerInventory);
        setVisible(true);

        background.setVisible(true);

        update();
    }

    @Override
    public void update() {
        super.update();
        playerInventory.setPosition(new Vector2f(0.5f * Display.getWidth() - CENTER_BORDER - playerInventory.getSize().x, 0));
        playerInventory.centerVertically();
        containerInventory.setPosition(new Vector2f(0.5f * Display.getWidth() + CENTER_BORDER, 0));
        containerInventory.centerVertically();
        background.setSize(new Vector2f(2 * (CENTER_BORDER + OUTER_BORDER) + playerInventory.getSize().x + containerInventory.getSize().x, 0.8f * Display.getHeight()));
        background.center();
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