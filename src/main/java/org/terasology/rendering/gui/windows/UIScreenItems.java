package org.terasology.rendering.gui.windows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.components.ItemComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UICompositeScrollable;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.management.BlockManager;

import com.google.common.collect.Lists;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIScreenItems extends UIWindow {
    
    private UICompositeScrollable container;
    private List<UIItemCell> cells = new ArrayList<UIItemCell>();
    private Vector2f cellSize = new Vector2f(48, 48);
    
    private ClickListener clickListener = new ClickListener() {
        @Override
        public void click(UIDisplayElement element, int button) {
            UIItemCell item = (UIItemCell) element;
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            EntityRef player = CoreRegistry.get(LocalPlayer.class).getEntity();
            player.send(new ReceiveItemEvent(entityManager.copy(item.getItemEntity())));
        }
    };
    
    public UIScreenItems() {
        setId("itemList");
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE, Keyboard.KEY_F5});
        setModal(true);
        maximize();
        
        GridLayout layout = new GridLayout(11);
        layout.setPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        container = new UICompositeScrollable();
        container.setHorizontalAlign(EHorizontalAlign.CENTER);
        container.setVerticalAlign(EVerticalAlign.CENTER);
        container.setSize(new Vector2f(608, 600));
        container.setBackgroundColor(new Color(120, 116, 91, 200));
        container.setBorderSolid(new Vector4f(1f, 1f, 1f, 1f), new Color(0, 0, 0));
        container.setEnableScrolling(true);
        container.setEnableScrollbar(true);
        container.setLayout(layout);
        container.setPadding(new Vector4f(12f, 12f, 12f, 12f));
        container.setVisible(true);
        
        addWindowListener(new WindowListener() {
            @Override
            public void shutdown(UIDisplayElement element) {
                EntityRef item;
                for (UIItemCell cell : cells) {
                    item = cell.getItemEntity();
                    if (item.exists()) {
                        item.destroy();
                    }
                }
            }
            
            @Override
            public void initialise(UIDisplayElement element) {
                
            }
        });
        
        fillInventoryCells();
        
        addDisplayElement(container);
    }
    
    private void fillInventoryCells() {
        //remove old cells
        for (UIItemCell cell : cells) {
            removeDisplayElement(cell);
        }
        cells.clear();
        
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
                    UIItemCell cell = new UIItemCell(null, cellSize);
                    cell.setDrag(false);
                    cell.setItemEntity(entity, 0);
                    cell.setDisplayItemCount(false);
                    cell.setVisible(true);
                    cell.addClickListener(clickListener);
                    
                    cells.add(cell);
                    container.addDisplayElement(cell);
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
                    UIItemCell cell = new UIItemCell(null, cellSize);
                    cell.setDrag(false);
                    cell.setItemEntity(entity, 0);
                    cell.setDisplayItemCount(false);
                    cell.setVisible(true);
                    cell.addClickListener(clickListener);
                    
                    cells.add(cell);
                    container.addDisplayElement(cell);
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
