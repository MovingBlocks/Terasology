package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

public class UIActiveMinion extends UIWindow{
	
	private UILabel lblname, lblflavor, lblzone;
	private UIModButtonArrow btnLeft, btnRight;
	
	public UIActiveMinion() {
		setId("activeminiion");
		setModal(true);
		setCloseKeys(new int[] { Keyboard.KEY_ESCAPE });
		
		addMouseButtonListener(new MouseButtonListener() {

			@Override
			public void wheel(UIDisplayElement element, int wheel,
					boolean intersect) {

			}

			@Override
			public void up(UIDisplayElement element, int button,
					boolean intersect) {
				if (button == 1) {
					close();
				}
			}

			@Override
			public void down(UIDisplayElement element, int button,
					boolean intersect) {

			}
		});
		
		setSize(new Vector2f(300,300));
		setVerticalAlign(EVerticalAlign.TOP);
		setHorizontalAlign(EHorizontalAlign.RIGHT);
		setBackgroundImage("miniion:activeminionback");
		setVisible(true);
		
		lblname = new UILabel();
		lblname.setPosition(new Vector2f(20, 45));
		lblname.setSize(new Vector2f(260,15));
		lblname.setBorderSolid(new Vector4f(2f, 2f, 2f, 2f), Color.magenta);
		lblname.setVisible(true);
		addDisplayElement(lblname);
		
		lblflavor = new UILabel();
		lblflavor.setPosition(new Vector2f(45, 5));
		lblflavor.setWrap(true);
		lblflavor.setSize(new Vector2f(250, 30));
		lblflavor.setVisible(true);
		addDisplayElement(lblflavor);
		
		lblzone = new UILabel();
		lblzone.setPosition(new Vector2f(10, 60));
		lblzone.setVisible(true);
		addDisplayElement(lblzone);		
		
		btnLeft = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.LEFT);
		btnLeft.setPosition(new Vector2f(8,41));
		btnLeft.setVisible(true);
		addDisplayElement(btnLeft);
		
		btnRight = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.RIGHT);
		btnRight.setPosition(new Vector2f(260,41));
		btnRight.setVisible(true);
		addDisplayElement(btnRight);
	}
	
	@Override
	public void open() {
		super.open();
		if(MinionSystem.getActiveMinion() == null){
			lblname.setText("No Oeon is obeying you!");			
			lblflavor.setText("Get your Oreominions now!!! 75% off if you bought any other DLC");
			lblzone.setText("");
		}else {
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			lblname.setText(minioncomp.name);
			lblflavor.setText(minioncomp.flavortext);
			if(minioncomp.gatherzone == null){
				lblzone.setText("no zone assigned");
			}else
			{
				lblzone.setText("workzone : " + minioncomp.gatherzone.Name);
			}
		}
	}
}
