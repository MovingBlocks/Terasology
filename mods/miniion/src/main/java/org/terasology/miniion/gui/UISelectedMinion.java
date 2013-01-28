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

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.gui.framework.*;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.miniion.gui.UIModButton;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.miniion.utilities.Zone;
import org.terasology.rendering.gui.widgets.*;

public class UISelectedMinion extends UICompositeScrollable {

	private class UIMinionbarCell extends UIDisplayContainer {

		private EntityRef minion;

		public UIMinionbarCell() {
			setSize(new Vector2f(48f, 48f));
			layout();
		}

		/**
		 * clears the selected minion and hides the buttons again
		 */
		public void clearMinion() {
			this.minion = null;
			lblBehaviour.setVisible(false);
			butfollow.setVisible(false);
			butSetZone.setVisible(false);
			butInventory.setVisible(false);
			butClearComm.setVisible(false);
			butBye.setVisible(false);
			butStay.setVisible(false);
			butAttack.setVisible(false);
			butGather.setVisible(false);
			lblname.setVisible(false);
			lblflavor.setVisible(false);
			setBehaviourToggle(null);
		}

		@Override
		public void update() {
		}

		@Override
		public void layout() {

		}

		/**
		 * render minion icons //TODO icons should get replaces with names list
		 */
		@Override
		public void render() {
			if (minion != null) {
				if (this.minion.hasComponent(MinionComponent.class)) {
					if (minion.getComponent(MinionComponent.class).icon
							.isEmpty()) {
						Icon icon = Icon.get("minionskull");
						if (icon != null) {
							renderIcon(icon);
						}
					} else {
						Icon icon = Icon.get(minion
								.getComponent(MinionComponent.class).icon);
						if (icon != null) {
							renderIcon(icon);
						}
					}
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

		public void setMinion(EntityRef minion) {
			this.minion = minion;
		}
	}

	private UIMinionbarCell cell = new UIMinionbarCell();
	private UIScreenBookOreo minionscreen;
	private UIModButton butfollow, butStay, butInventory, butBye, butAttack,
			butGather, butSetZone, butClearComm;
	private UILabel lblBehaviour, lblname, lblflavor;
	private UIList uizonelist;

	public UISelectedMinion(UIScreenBookOreo minionscreen) {

		this.minionscreen = minionscreen;

		cell.setPosition(new Vector2f(0, 30));
		cell.setVisible(true);
		this.addDisplayElement(cell);

		lblname = new UILabel("");
		lblname.setPosition(new Vector2f(50, 30));
		lblname.setColor(Color.black);
		lblname.setVisible(false);
		this.addDisplayElement(lblname);

		lblflavor = new UILabel("");
		lblflavor.setPosition(new Vector2f(50, 50));
		lblflavor.setColor(Color.black);
		lblflavor.setVisible(false);
		this.addDisplayElement(lblflavor);

		lblBehaviour = new UILabel("Behaviour");
		lblBehaviour.setPosition(new Vector2f(20, 80));
		lblBehaviour.setColor(Color.black);
		lblBehaviour.setVisible(false);
		this.addDisplayElement(lblBehaviour);

		butfollow = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butfollow.setLabel("Follow");
		butfollow.setColorOffset(180);
		butfollow.setVisible(false);
		butfollow.setPosition(new Vector2f(20, 110));
		butfollow.setId("btnFollow");
		butfollow.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				executeClick(element, button);
			}
		});
		this.addDisplayElement(butfollow);

		butStay = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butStay.setLabel("Stay");
		butStay.setColorOffset(180);
		butStay.setVisible(false);
		butStay.setPosition(new Vector2f(80, 110));
		butStay.setId("btnStay");
		butStay.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				executeClick(element, button);
			}
		});
		this.addDisplayElement(butStay);

		butAttack = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butAttack.setLabel("Attack");
		butAttack.setColorOffset(180);
		butAttack.setVisible(false);
		butAttack.setPosition(new Vector2f(140, 110));
		butAttack.setId("btnAttack");
		butAttack.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				executeClick(element, button);
			}
		});
		this.addDisplayElement(butAttack);

		butGather = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butGather.setLabel("Gather");
		butGather.setColorOffset(180);
		butGather.setVisible(false);
		butGather.setPosition(new Vector2f(200, 110));
		butGather.setId("btnGather");
		butGather.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				executeClick(element, button);
			}
		});
		this.addDisplayElement(butGather);

		butSetZone = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butSetZone.setLabel("select zone");
		butSetZone.setVisible(false);
		butSetZone.setPosition(new Vector2f(140, 170));
		butSetZone.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				if (uizonelist.isVisible()) {
					getZone();
					butSetZone.setLabel("select zone");
				} else {
					setZone();
					butSetZone.setLabel("save zone");
				}
			}
		});
		this.addDisplayElement(butSetZone);

		butInventory = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butInventory.setLabel("Inventory");
		butInventory.setVisible(false);
		butInventory.setPosition(new Vector2f(20, 170));
		butInventory.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				openInventory();
			}
		});
		this.addDisplayElement(butInventory);
		
		butClearComm = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butClearComm.setLabel("clear orders");
		butClearComm.setColorOffset(120);
		butClearComm.setVisible(false);
		butClearComm.setPosition(new Vector2f(20, 200));
		butClearComm.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				SimpleMinionAIComponent aicomp = cell.minion.getComponent(SimpleMinionAIComponent.class);
				aicomp.ClearCommands();
				cell.minion.saveComponent(aicomp);
				MinionComponent minioncomp = cell.minion.getComponent(MinionComponent.class);
				minioncomp.assignedzone = null;
				cell.minion.saveComponent(minioncomp);
			}
		});
		this.addDisplayElement(butClearComm);

		butBye = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butBye.setLabel("bye bye");
		butBye.setColorOffset(120);
		butBye.setVisible(false);
		butBye.setPosition(new Vector2f(140, 200));
		butBye.addClickListener(new ClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				destroyMinion();
			}
		});
		this.addDisplayElement(butBye);

		uizonelist = new UIList();
		uizonelist.setSize(new Vector2f(250, 300));
		uizonelist.setPosition(new Vector2f(300, 0));
		uizonelist.setBackgroundImage("miniion:pageback");
		uizonelist.setBackgroundColor(Color.black);
		uizonelist.setVisible(false);
		this.addDisplayElement(uizonelist);

	}

	/**
	 * sets the currently active minion and shows it's setting page
	 * 
	 * @param minion
	 *            : the minion selected in the list of summoned minions
	 */
	public void setMinion(EntityRef minion) {
		MinionSystem.setActiveMinion(minion); // new way of defining the active
												// minion, //TODO book ui should
												// reflect this (selection
												// rectangle, minor
												// inconsistence)
		cell.setMinion(minion);
		MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
		lblname.setText(minioncomp.name);
		lblflavor.setText(minioncomp.flavortext);
		lblname.setVisible(true);
		lblflavor.setVisible(true);
		lblBehaviour.setVisible(true);
		butfollow.setVisible(true);
		butSetZone.setVisible(true);
		butInventory.setVisible(true);
		butClearComm.setVisible(true);
		butBye.setVisible(true);
		butStay.setVisible(true);
		butAttack.setVisible(true);
		butGather.setVisible(true);
		lblname.setVisible(true);
		lblflavor.setVisible(true);
		if (minioncomp.minionBehaviour == MinionBehaviour.Follow) {
			setBehaviourToggle(butfollow);
		} else if (minioncomp.minionBehaviour == MinionBehaviour.Stay) {
			setBehaviourToggle(butStay);
		} else if (minioncomp.minionBehaviour == MinionBehaviour.Attack) {
			setBehaviourToggle(butAttack);
		} else if (minioncomp.minionBehaviour == MinionBehaviour.Gather) {
			setBehaviourToggle(butGather);
		} else {
			setBehaviourToggle(null);
		}
	}

	/**
	 * set behaviour toggles and matching behaviour all behaviour toggles should
	 * be added here
	 */
	public void executeClick(UIDisplayElement element, int id) {
		UIModButton clickedbutton = (UIModButton) element;
		MinionComponent minioncomp = this.cell.minion
				.getComponent(MinionComponent.class);
		if (clickedbutton.getId() == "btnStay") {
			minioncomp.minionBehaviour = MinionBehaviour.Stay;
			setBehaviourToggle(butStay);
		}
		if (clickedbutton.getId() == "btnFollow") {
			minioncomp.minionBehaviour = MinionBehaviour.Follow;
			setBehaviourToggle(butfollow);
		}
		if (clickedbutton.getId() == "btnAttack") {
			minioncomp.minionBehaviour = MinionBehaviour.Attack;
			setBehaviourToggle(butAttack);
		}
		if (clickedbutton.getId() == "btnGather") {
			minioncomp.minionBehaviour = MinionBehaviour.Gather;
			setBehaviourToggle(butGather);
		}
		this.cell.minion.saveComponent(minioncomp);
	}

	/**
	 * make sure only 1 toggle is active
	 * 
	 * @param button
	 *            : the active toggle
	 */
	private void setBehaviourToggle(UIModButton button) {
		butStay.setToggleState(false);
		butfollow.setToggleState(false);
		butAttack.setToggleState(false);
		butGather.setToggleState(false);
		if (button != null) {
			button.setToggleState(true);
		}
	}

	/**
	 * no comment.
	 */
	private void destroyMinion() {
		if (this.cell.minion != null) {
			MinionComponent minioncomp = this.cell.minion
					.getComponent(MinionComponent.class);
			minioncomp.minionBehaviour = MinionBehaviour.Die;
			this.cell.minion.saveComponent(minioncomp);
			if (minionscreen != null) {
				minionscreen.removeMinionFromList(this.cell.minion);
			}
			this.cell.clearMinion();			
		}
	}

	/**
	 * changes the minions behaviour to iddle, opens it's inventory, probably
	 * needs a distance check closes the settings screen and opens the inventory
	 */
	private void openInventory() {
		if (this.cell.minion != null) {
			this.cell.minion.send(new ActivateEvent(this.cell.minion,
					CoreRegistry.get(LocalPlayer.class).getEntity()));
			this.cell.minion.getComponent(MinionComponent.class).minionBehaviour = MinionBehaviour.Stay;
			setBehaviourToggle(butStay);
			this.cell.minion.saveComponent(this.cell.minion
					.getComponent(MinionComponent.class));
			UIDisplayElement.getGUIManager().closeWindow(minionscreen);
		}
	}

	/**
	 * dirty trick to select zone
	 */
	private void setZone() {
		uizonelist.removeAll();
		for (Zone zone : MinionSystem.getGatherZoneList()) {
			UIListItem listitem = new UIListItem(zone.Name, zone);
			listitem.setTextColor(Color.black);
			uizonelist.addItem(listitem);
		}
		uizonelist.setVisible(true);
	}

	private void getZone() {
		if (uizonelist.getSelection() != null) {
			MinionComponent minioncomp = cell.minion
					.getComponent(MinionComponent.class);
			for (Zone zone : MinionSystem.getGatherZoneList()) {
				if (zone.Name.matches(uizonelist.getSelection().getText())) {
					minioncomp.assignedzone = zone;
				}
			}
			cell.minion.saveComponent(minioncomp);
		}
		uizonelist.setVisible(false);
	}
}
