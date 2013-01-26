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
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.*;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

public class UIScreenBookOreo extends UIWindow {

	private UIMinionContainer container;
	private UISelectedMinion selected;
	private final UIImage background;
	private final UILabel pagetitle, pagetitle2;

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

		GridLayout layout = new GridLayout(4);
		layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));

		container = new UIMinionContainer();
		container.setPosition(new Vector2f(20, 40));
		container.setSize(new Vector2f(260, 170));
		container.setEnableScrolling(true);
		container.setEnableScrollbar(true);
		container.setLayout(layout);
		container.setPadding(new Vector4f(12f, 12f, 12f, 12f));
		container.setVisible(true);
		addDisplayElement(container);

		selected = new UISelectedMinion(this);
		selected.setPosition(new Vector2f(310, 40));
		selected.setSize(new Vector2f(250, 170));
		selected.setVisible(true);
		addDisplayElement(selected);
	}

	public void setSelectedMinion(EntityRef minion) {
		selected.setMinion(minion);

	}

	@Override
	public void open() {
		super.open();
		refresh();
	}

	public void refresh() {
		container.fillInventoryCells(this);
	}
}
