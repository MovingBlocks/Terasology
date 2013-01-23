package org.terasology.miniion.gui;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UICompositeScrollable;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;

public class UIMinionContainer extends UICompositeScrollable{
	
	private List<UIMinionbarCell> cells = new ArrayList<UIMinionbarCell>();
    private Vector2f cellSize = new Vector2f(48, 48);
    private UIScreenBookOreo minionscreen;
	 
	
	private class UIMinionbarCell extends UIDisplayContainer {

        private final UIImage selectionRectangle;
        private final UILabel label;        

        private boolean selected = false;
        private EntityRef minion;
        
        private ClickListener clickListener = new ClickListener() {
	        @Override
	        public void click(UIDisplayElement element, int button) {
	        	       	
	        	if(minionscreen != null){
	        		for(UIMinionbarCell cell : cells){
	        			cell.setSelected(false);
	        		}
	        		minionscreen.setSelectedMinion(((UIMinionbarCell)element).minion);
	        	}
	        	UIMinionbarCell minion = (UIMinionbarCell) element;	 
	        	minion.selected = true;
	        }
	    };

        public UIMinionbarCell() {
            setSize(new Vector2f(48f, 48f));
            selectionRectangle = new UIImage();
            selectionRectangle.setTexture(Assets.getTexture("engine:gui"));
            selectionRectangle.setTextureSize(new Vector2f(24f, 24f));
            selectionRectangle.setTextureOrigin(new Vector2f(0.0f, 24f));
            selectionRectangle.setSize(new Vector2f(48f, 48f));

            label = new UILabel();
            label.setVisible(true);
            label.setPosition(new Vector2f(30f, 20f));
            
            this.addClickListener(clickListener);
            
            layout();
        }
        
        public void clearMinion(){
        	this.minion = null;
        }
        
        @Override
        public void update() {            
            selectionRectangle.setVisible(selected);
            setPosition(this.getPosition());            
        }
        
    	@Override
    	public void layout() {

    	}

        @Override
        public void render() {        	
        	if(selected){
        		selectionRectangle.renderTransformed();
        	}
        	if(this.minion != null)
        	{
        		if(this.minion.hasComponent(MinionComponent.class))
        		{
		            if (minion.getComponent(MinionComponent.class).icon.isEmpty()) {
		                Icon icon = Icon.get("gelcube");
		                if (icon != null) {
		                    renderIcon(icon);
		                }
		            } else {
		                Icon icon = Icon.get(minion.getComponent(MinionComponent.class).icon);
		                if (icon != null) {
		                    renderIcon(icon);
		                }
		            }
		            label.renderTransformed();
        		}
        	}
        }

        private void renderIcon(Icon icon) {
            glEnable(GL11.GL_DEPTH_TEST);
            glClear(GL11.GL_DEPTH_BUFFER_BIT);
            glPushMatrix();
            glTranslatef(20f, 20f, 0f);
            icon.render();
            glPopMatrix();
            glDisable(GL11.GL_DEPTH_TEST);
        }
        
        public void setMinion(EntityRef minion){
        	this.minion = minion;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean getSelected() {
            return selected;
        }

        public UILabel getLabel() {
            return label;
        }
    }
		
	public void fillInventoryCells()
	{
		//remove old cells
		for (UIMinionbarCell cell : cells) {
            this.removeDisplayElement(cell);
        }
        cells.clear();
        
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.iteratorEntities(MinionComponent.class))
        {

				UIMinionbarCell cell = new UIMinionbarCell();
				cell.setMinion(entity);
		        cell.setVisible(true);                 
		        cells.add(cell);
		        this.addDisplayElement(cell);
        }
	}
	
	public void fillInventoryCells(UIScreenBookOreo minionscreen)
	{
		this.minionscreen = minionscreen;
		fillInventoryCells();
	}
	
	public EntityRef getSelectedMinion(){
		for (UIMinionbarCell cell : cells) {
            if(cell.selected){
            	return cell.minion;
            }
        }
		return null;
	}
}
