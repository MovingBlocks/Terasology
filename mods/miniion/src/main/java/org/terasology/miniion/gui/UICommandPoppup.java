package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIWindow;

public class UICommandPoppup extends UIWindow{
	
	private UIList buttonlist;
	
	private MouseButtonListener buttonlistener = new MouseButtonListener() {
		
		@Override
		public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
			
		}
		
		@Override
		public void up(UIDisplayElement element, int button, boolean intersect) {
			// TODO execute click on mouseup
			
		}
		
		@Override
		public void down(UIDisplayElement element, int button, boolean intersect) {
			
		}
	};
	
	public UICommandPoppup(){
		
		setId("commandpoppup");
		setModal(true);
		
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
		
		buttonlist = new UIList();
		buttonlist.setVisible(true);
		buttonlist.setSize(new Vector2f(150, 300));
	}

}
