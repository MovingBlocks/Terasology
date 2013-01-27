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
package org.terasology.miniion.gui;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.*;

import javax.vecmath.Vector2f;

public class UIScreenBookOreo extends UIWindow {

	private UISelectedMinion selected;
	private UIList uiminionlist;
	private final UIImage background;
	private final UILabel pagetitle, pagetitle2;
	private UIModButton btnshowminion;
	
	private ClickListener minionistener = new ClickListener() {		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIListItem listitem = (UIListItem)element;
			selected.setMinion((EntityRef)listitem.getValue());
			//MinionSystem.setActiveMinion((EntityRef)listitem.getValue());
		}
	};

	public UIScreenBookOreo() {
		setId("oreobook");
		setHorizontalAlign(EHorizontalAlign.LEFT);
		setVerticalAlign(EVerticalAlign.CENTER);
		setModal(true);
		// setCloseBinds(new String[]{"engine:useHeldItem"});
		setCloseKeys(new int[] { Keyboard.KEY_ESCAPE });
		setSize(new Vector2f(800, 400));

		background = new UIImage();
		background.setTexture(Assets.getTexture("miniion:openbook"));
		background.setPosition(new Vector2f(0, 0));
		background.setSize(new Vector2f(600, 400));
		background.setVisible(true);
		addDisplayElement(background);

		pagetitle = new UILabel("Summoned minions");
		pagetitle.setPosition(new Vector2f(50, 20));
		pagetitle.setColor(org.newdawn.slick.Color.black);
		pagetitle.setVisible(true);
		addDisplayElement(pagetitle);

		pagetitle2 = new UILabel("Active minion");
		pagetitle2.setPosition(new Vector2f(340, 20));
		pagetitle2.setColor(org.newdawn.slick.Color.black);
		pagetitle2.setVisible(true);
		addDisplayElement(pagetitle2);
		
		uiminionlist = new UIList();
		uiminionlist.setSize(new Vector2f(250, 350));
		uiminionlist.setPosition(new Vector2f(45, 40));
		uiminionlist.setVisible(true);
		addDisplayElement(uiminionlist);	

		selected = new UISelectedMinion(this);
		selected.setPosition(new Vector2f(310, 40));
		selected.setSize(new Vector2f(250, 170));
		selected.setVisible(true);
		addDisplayElement(selected);
		
		btnshowminion = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		btnshowminion.setLabel("Show minion window");
		btnshowminion.setColorOffset(120);
		btnshowminion.setVisible(true);
		btnshowminion.setPosition(new Vector2f(375, 260));
		btnshowminion.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				if(btnshowminion.getLabel().getText() == "Show minion window"){
					btnshowminion.setLabel("Hide minion minion");
				}else {
					btnshowminion.setLabel("Show minion window");
				}
			}
		});
		this.addDisplayElement(btnshowminion);
		
	}

	@Override
	public void open() {
		super.open();
		refresh();
	}

	public void refresh() {
		//container.fillInventoryCells(this);
		uiminionlist.removeAll();
		EntityManager entMan = CoreRegistry.get(EntityManager.class);
		for(EntityRef minion : entMan.iteratorEntities(MinionComponent.class)){
			UIListItem listitem = new UIListItem(minion.getComponent(MinionComponent.class).name, minion);
			listitem.setTextColor(Color.black);
			listitem.addClickListener(minionistener);
			uiminionlist.addItem(listitem);
		}
	}
	
	public void removeMinionFromList(EntityRef minion){
		for(UIListItem item : uiminionlist.getItems()){
			EntityRef listminion = (EntityRef)item.getValue();
			if(listminion.getId() == minion.getId()){
				uiminionlist.removeItem(item);
				//doesn't seem to work
				/*if(uiminionlist.getItemCount() > 0){
					uiminionlist.select(0);
					selected.setMinion((EntityRef)uiminionlist.getSelection().getValue());
				}*/
			}
		}
		
	}
}
