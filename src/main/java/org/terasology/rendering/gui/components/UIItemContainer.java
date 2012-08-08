package org.terasology.rendering.gui.components;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector2f;
import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * A container which is capable of holding multiple item cells.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIItemContainer extends UIDisplayContainer {

    private EntityRef entity = null;
    InventoryComponent entityInventory = null;
    private List<UIItemCell> cells = new ArrayList<UIItemCell>();
    
    private Vector2f cellMargin = new Vector2f(2, 2);
	private Vector2f cellSize = new Vector2f(48, 48);
    
    private int cols;
    private int rows;

    public UIItemContainer(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        
        //CoreRegistry.get(EventSystem.class).registerEventHandler(this);
    }
    
    private void fillInventoryCells() {
        if (entityInventory != null)
        {
        	//remove old cells
        	for (UIItemCell cell : cells) {
				removeDisplayElement(cell);
			}
        	cells.clear();
        	
        	//add new cells
            setVisible(true);
	    	setSize(new Vector2f(cols * (cellSize.x + cellMargin.x), rows * (cellSize.y + cellMargin.y)));

	    	int size = Math.min(entityInventory.itemSlots.size(), cols * rows);
	        for (int i = 0; i < size; ++i)
	        {
	            UIItemCell cell = new UIItemCell(entity, cellSize);
	            cell.setItem(entityInventory.itemSlots.get(i), i);
	            cell.setSize(cellSize);
	            cell.setPosition(new Vector2f((i % cols) * (cellSize.x + cellMargin.x), (i / cols) * (cellSize.y + cellMargin.y)));
	            cell.setVisible(true);
	            
	            cells.add(cell);
	            addDisplayElement(cell);
	        }
        }
	}
    
    public void updateInventoryCells() {
    	if (entityInventory != null) {
	    	for (int i = 0; i < cells.size(); ++i)
	        {
	    		cells.get(i).setItem(entityInventory.itemSlots.get(i), i);
	    		
	        }
    	}
    }
    
    @Override
    public void update() {
    	super.update();
    	
    	updateInventoryCells();
    }
    
    public List<UIItemCell> getCells() {
		return cells;
	}
    
	public EntityRef getEntity() {
		return entity;
	}

	public void setEntity(EntityRef entity) {
        this.entity = entity;
        this.entityInventory = entity.getComponent(InventoryComponent.class);
        
        fillInventoryCells();
    }

    public Vector2f getCellMargin() {
		return cellMargin;
	}

	public void setCellMargin(Vector2f cellMargin) {
		this.cellMargin = cellMargin;
	}

	public Vector2f getCellSize() {
		return cellSize;
	}

	public void setCellSize(Vector2f cellSize) {
		this.cellSize = cellSize;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	
	/*
    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {
    }
    
    @ReceiveEvent(components = InventoryComponent.class)
    public void onReceiveItem(ReceiveItemEvent event, EntityRef entity) {
	    fillInventoryCells();
    }
    */
}
