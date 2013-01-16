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
package org.terasology.game.types;

import com.google.common.collect.Lists;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UICompositeScrollable;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FreeStyleType extends GameType {

    public FreeStyleType(){
        setName("Free-style");
    }

    //onCreateInventory
    private List<UIItemCell>      inventoryCells = new ArrayList<UIItemCell>();
    private UIWindow              inventoryParent;
    private UICompositeScrollable inventoryContainer;
    private Vector2f inventoryCellSize = new Vector2f(48, 48);
    private ClickListener         inventoryClickListener = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {

            if( CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).transferSlot.exists() ){
                CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).transferSlot.destroy();
                UIItemContainer inventory = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("inventory").getElementById("inventory");

                for(UIItemCell cell : inventory.getCells()){
                    if(cell.getTransferItemIcon().isVisible()){
                        cell.getTransferItemIcon().setVisible(false);
                        break;
                    }
                }

            }else{
                UIItemCell item = (UIItemCell) element;
                EntityManager entityManager = CoreRegistry.get(EntityManager.class);
                EntityRef player = CoreRegistry.get(LocalPlayer.class).getEntity();
                player.send(new ReceiveItemEvent(entityManager.copy(item.getItemEntity())));
            }
        }
    };

    @Override
    public void onCreateInventoryHook(UIWindow parent) {

        inventoryParent = parent;
        GridLayout layout = new GridLayout(10);
        //layout.setPadding(new Vector4f(0f, 2f, 2f, 2f));

        inventoryContainer = new UICompositeScrollable();
        inventoryContainer.setHorizontalAlign(UIDisplayElement.EHorizontalAlign.CENTER);
        inventoryContainer.setVerticalAlign(UIDisplayElement.EVerticalAlign.CENTER);
        inventoryContainer.setSize(new Vector2f(495, 288));
        inventoryContainer.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));
        inventoryContainer.setEnableScrolling(true);
        inventoryContainer.setEnableScrollbar(true);
        inventoryContainer.setLayout(layout);
        inventoryContainer.setPadding(new Vector4f(0f, 15f, 0f, 0f));
        inventoryContainer.setVisible(true);

        fillInventoryCells();

        inventoryParent.addDisplayElement(inventoryContainer);

    }

    @Override
    public void onPlayerDamageHook(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        if(entity.hasComponent(LocalPlayerComponent.class)){
            return;
        }

        if (health.currentHealth <= 0) return;

        health.timeSinceLastDamage = 0;
        health.currentHealth -= damageAmount;
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent(instigator, health.maxHealth));
        } else {
            entity.send(new HealthChangedEvent(instigator, health.currentHealth, health.maxHealth));
        }
        entity.saveComponent(health);
    }

    private void fillInventoryCells() {
        //remove old cells
        for (UIItemCell cell : inventoryCells) {
            inventoryParent.removeDisplayElement(cell);
        }
        inventoryCells.clear();

        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        EntityRef entity;

        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        Iterator<Prefab> it = prefMan.listPrefabs().iterator();
        ItemComponent itemComp;
        while (it.hasNext()) {
            Prefab prefab = it.next();
            itemComp = prefab.getComponent(ItemComponent.class);
            if (itemComp != null) {
                entity = entityManager.create(prefab.listComponents());
                if (entity.exists() && entity.getComponent(ItemComponent.class) != null) {
                    UIItemCell cell = new UIItemCell(null, inventoryCellSize);
                    cell.setDrag(false);
                    cell.setItemEntity(entity, 0);
                    cell.setDisplayItemCount(false);
                    cell.setVisible(true);
                    cell.addClickListener(inventoryClickListener);

                    inventoryCells.add(cell);
                    inventoryContainer.addDisplayElement(cell);
                }
            }
        }

        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        List<List<BlockUri>> blocks = new ArrayList<List<BlockUri>>();
        blocks.add(sortItems(BlockManager.getInstance().listRegisteredBlockUris()));
        blocks.add(sortItems(BlockManager.getInstance().listAvailableBlockUris()));
        blocks.add(sortItems(BlockManager.getInstance().listShapelessBlockUris()));

        for (List<BlockUri> blockList : blocks) {
            for (BlockUri block : blockList) {
                entity = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(block.getFamilyUri()), 99);
                if (entity.exists()) {
                    UIItemCell cell = new UIItemCell(null, inventoryCellSize);
                    cell.setDrag(false);
                    cell.setItemEntity(entity, 0);
                    cell.setDisplayItemCount(false);
                    cell.setVisible(true);
                    cell.addClickListener(inventoryClickListener);

                    inventoryCells.add(cell);
                    inventoryContainer.addDisplayElement(cell);
                }
            }
        }
    }

    private <T extends Comparable<T>> List<T> sortItems(Iterable<T> items) {
        List<T> result = Lists.newArrayList();
        for (T item : items) {
            result.add(item);
        }
        Collections.sort(result);
        return result;
    }

}
