package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.ZoneSelectionComponent;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.*;

public class UIZoneBook extends UIWindow{
	 private final UIImage background;
	 private final UILabel lblzonename, lblheight, lbldepth, lblwidth;
	 private final UIText txtzonename, txtheight, txtdepth, txtwidth;
	 private UIModButton btnSave;

	 public UIZoneBook(){
		 
		setId("zonebook");
        setModal(true);
        maximize();
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
		 
		background = new UIImage();
		background.setTexture(Assets.getTexture("miniion:openbook"));
		background.setHorizontalAlign(EHorizontalAlign.CENTER);
		background.setVerticalAlign(EVerticalAlign.CENTER);
		background.setSize(new Vector2f(500, 300));
		background.setVisible(true);
		addDisplayElement(background);
		
		lblzonename = new UILabel("Zone name :");
		lblzonename.setPosition(new Vector2f(260, 20));
		lblzonename.setColor(Color.black);
		lblzonename.setVisible(true);
		background.addDisplayElement(lblzonename);
		
		txtzonename = new UIText();
		txtzonename.setPosition(new Vector2f(350, 20));
		txtzonename.setColor(Color.black);
		txtzonename.setSize(new Vector2f(80, 20));
		txtzonename.setVisible(true);
		background.addDisplayElement(txtzonename);
        
        lblheight = new UILabel("Height :");
		lblheight.setPosition(new Vector2f(260, 40));
		lblheight.setColor(Color.black);
		lblheight.setVisible(true);
        background.addDisplayElement(lblheight);
        
        txtheight = new UIText();
		txtheight.setPosition(new Vector2f(350, 40));
		txtheight.setColor(Color.black);
		txtheight.setSize(new Vector2f(80, 20));
		txtheight.setVisible(true);
		background.addDisplayElement(txtheight);
        
        lblwidth = new UILabel("Width :");
        lblwidth.setPosition(new Vector2f(260, 60));
        lblwidth.setColor(Color.black);
        lblwidth.setVisible(true);
        background.addDisplayElement(lblwidth);
        
        txtwidth = new UIText();
        txtwidth.setPosition(new Vector2f(350, 60));
        txtwidth.setColor(Color.black);
        txtwidth.setSize(new Vector2f(80, 20));
        txtwidth.setVisible(true);
		background.addDisplayElement(txtwidth);
        
        lbldepth = new UILabel("Depth :");
        lbldepth.setPosition(new Vector2f(260, 80));
        lbldepth.setColor(Color.black);
        lbldepth.setVisible(true);
        background.addDisplayElement(lbldepth);
        
        txtdepth = new UIText();
        txtdepth.setPosition(new Vector2f(350, 80));
        txtdepth.setColor(Color.black);
        txtdepth.setSize(new Vector2f(80, 20));
        txtdepth.setVisible(true);
		background.addDisplayElement(txtdepth);
		
		btnSave = new UIModButton(new Vector2f(50,20), ButtonType.NORMAL);
		btnSave.setPosition(new Vector2f(260, 250));
		btnSave.setLabel("Save");
		btnSave.setId("btnSave");
		btnSave.setVisible(true);
		btnSave.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
            	executeClick(element,button);
            }
        });
		background.addDisplayElement(btnSave);
		
	 }
	 
	 public void executeClick(UIDisplayElement element, int id){
			UIModButton clickedbutton = (UIModButton)element;
			//TODO : save the zone to the zonelist, made a component but that's not very ideal.
	 }
	 
	 @Override
	public void open() {
		super.open();
		EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.iteratorEntities(ZoneSelectionComponent.class))
        {
        	ZoneSelectionComponent selection = entity.getComponent(ZoneSelectionComponent.class);
        	if(selection.blockGrid != null && selection.blockGrid.getGridPositions().size() > 1){
	        	Vector3i minbounds = selection.blockGrid.getMinBounds();
	        	Vector3i maxbounds = selection.blockGrid.getMaxBounds();
	        	txtzonename.setText("comingsoon");
	        	txtwidth.setText("" + (getAbsoluteDiff(maxbounds.x, minbounds.x)));
	        	txtdepth.setText("" + (getAbsoluteDiff(maxbounds.z, minbounds.z)));
	        	txtheight.setText("" + (getAbsoluteDiff(maxbounds.y, minbounds.y)));
        	}
        }
	}
	 
	 private int getAbsoluteDiff(int val1, int val2){
		int width;
     	if(val1 == val2){
     		width = 1;
     	}else if(val1 < 0){
     		if(val2 < 0 && val2 < val1){
     			width = Math.abs(val2) - Math.abs(val1);
     		}else if(val2 < 0 && val2 > val1){
     			width = Math.abs(val1) - Math.abs(val2) ;
     		}else{
     			width = Math.abs(val1) + val2; 
     		}
     		width++;
     	}else{
     		if(val2 > -1 && val2 < val1){
     			width = val1 - val2;
     		}else if(val2 > -1 && val2 > val1){
     			width = val2 - val1;
     		}else{
     			width = Math.abs(val2) + val1;  
     		}
     		width++;
     	}
     	return width;
	 }
}
