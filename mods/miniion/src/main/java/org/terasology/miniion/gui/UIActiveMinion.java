package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.miniion.utilities.MinionRecipe;
import org.terasology.miniion.utilities.Zone;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.*;

public class UIActiveMinion extends UIWindow{
	
	private UILabel lblname, lblflavor, lblzone, lblrecipe;
	private UIImage backgroundmain;
	private UIComposite behaviourlist, actionlist;
	private UIList uizonelist;
	private UIScreenStats uistats;
	private UIModButtonArrow btnLeft, btnRight, btnBehaviour, btnActions, btnStats;
	//behaviour buttons
	private UIModButtonMenu btnStay, btnFollow, btnAttack, btnGather, btnWork;
	//action buttons
	private UIModButtonMenu btnInventory, btnZone, btnRecipe, btnClear, btnBye;
	
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
		setVisible(true);
		
		backgroundmain = new UIImage();
		backgroundmain.setTexture(Assets.getTexture("miniion:activeminionback"));
		backgroundmain.setPosition(new Vector2f(0, 0));
		backgroundmain.setSize(new Vector2f(300, 200));
		backgroundmain.setVisible(true);
		addDisplayElement(backgroundmain);
		
		lblname = new UILabel();
		lblname.setPosition(new Vector2f(30, 45));
		lblname.setSize(new Vector2f(260,15));		
		lblname.setVisible(true);
		backgroundmain.addDisplayElement(lblname);
		
		lblflavor = new UILabel();
		lblflavor.setPosition(new Vector2f(45, 5));
		lblflavor.setWrap(true);
		lblflavor.setSize(new Vector2f(250, 30));
		lblflavor.setVisible(true);
		backgroundmain.addDisplayElement(lblflavor);
		
		lblzone = new UILabel();
		lblzone.setPosition(new Vector2f(10, 65));
		lblzone.setVisible(true);
		backgroundmain.addDisplayElement(lblzone);	
		
		lblrecipe = new UILabel();
		lblrecipe.setPosition(new Vector2f(10, 80));
		lblrecipe.setVisible(true);
		backgroundmain.addDisplayElement(lblrecipe);	
		
		btnLeft = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.LEFT);
		btnLeft.setPosition(new Vector2f(8,41));
		btnLeft.setId("previousminion");
		btnLeft.addClickListener(executeArrowButton);
		btnLeft.setVisible(true);
		backgroundmain.addDisplayElement(btnLeft);
		
		btnRight = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.RIGHT);
		btnRight.setPosition(new Vector2f(260,41));
		btnRight.setId("nextminion");
		btnRight.addClickListener(executeArrowButton);
		btnRight.setVisible(true);
		backgroundmain.addDisplayElement(btnRight);						
		
		GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(0f, 0f, 0f, 0f));
		behaviourlist = new UIComposite();
		behaviourlist.setSize(new Vector2f(100,100));
		behaviourlist.setPosition(new Vector2f(200,200));
		behaviourlist.setBackgroundImage("miniion:modularback");
		behaviourlist.setLayout(layout);
		behaviourlist.setVisible(false);
		backgroundmain.addDisplayElement(behaviourlist);
		
		btnBehaviour  = new UIModButtonArrow(new Vector2f(46,12), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.DOWN, "set behaviour");
		btnBehaviour.setPosition(new Vector2f(237,186));
		btnBehaviour.setVisible(true);
		btnBehaviour.setId("showbehaviour");
		btnBehaviour.addClickListener(executeArrowButton);		
		backgroundmain.addDisplayElement(btnBehaviour);
		
		btnStay = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnStay.setLabel("STAY");
		btnStay.setId("stay");
		btnStay.addClickListener(behaviourToggleListener);
		btnStay.setVisible(true);
		behaviourlist.addDisplayElement(btnStay);
		
		btnFollow = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnFollow.setLabel("FOLLOW");
		btnFollow.setId("foll");
		btnFollow.addClickListener(behaviourToggleListener);
		btnFollow.setVisible(true);
		behaviourlist.addDisplayElement(btnFollow);
		
		btnAttack = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnAttack.setLabel("ATTACK");
		btnAttack.setId("atta");
		btnAttack.addClickListener(behaviourToggleListener);
		btnAttack.setVisible(true);
		behaviourlist.addDisplayElement(btnAttack);
		
		btnGather = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnGather.setLabel("GATHER");
		btnGather.setId("gath");
		btnGather.addClickListener(behaviourToggleListener);
		btnGather.setVisible(true);
		behaviourlist.addDisplayElement(btnGather);
		
		btnWork = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnWork.setLabel("WORK");
		btnWork.setId("work");
		btnWork.addClickListener(behaviourToggleListener);
		btnWork.setVisible(true);
		behaviourlist.addDisplayElement(btnWork);
		
		btnActions  = new UIModButtonArrow(new Vector2f(46,12), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.DOWN, "select action");
		btnActions.setPosition(new Vector2f(137,186));
		btnActions.setVisible(true);
		btnActions.setId("showactions");
		btnActions.addClickListener(executeArrowButton);		
		backgroundmain.addDisplayElement(btnActions);
		
		actionlist = new UIComposite();
		actionlist.setSize(new Vector2f(100,100));
		actionlist.setPosition(new Vector2f(100,200));
		actionlist.setBackgroundImage("miniion:modularback");
		actionlist.setLayout(layout);
		actionlist.setVisible(false);
		backgroundmain.addDisplayElement(actionlist);
		
		btnInventory = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnInventory.setLabel("inventory");
		btnInventory.setId("inve");
		btnInventory.addClickListener(actionListener);
		btnInventory.setVisible(true);
		actionlist.addDisplayElement(btnInventory);
		
		btnZone = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnZone.setLabel("zone");
		btnZone.setId("zone");
		btnZone.addClickListener(actionListener);
		btnZone.setVisible(true);
		actionlist.addDisplayElement(btnZone);
		
		btnRecipe = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnRecipe.setLabel("recipe");
		btnRecipe.setId("reci");
		btnRecipe.addClickListener(actionListener);
		btnRecipe.setVisible(true);
		actionlist.addDisplayElement(btnRecipe);
		
		btnClear = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnClear.setLabel("clear orders");
		btnClear.setId("clea");
		btnClear.addClickListener(actionListener);
		btnClear.setVisible(true);
		actionlist.addDisplayElement(btnClear);
		
		btnBye = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnBye.setLabel("bye bye");
		btnBye.setId("byeb");
		btnBye.addClickListener(actionListener);
		btnBye.setVisible(true);
		actionlist.addDisplayElement(btnBye);
		
		uizonelist = new UIList();
		uizonelist.setSize(new Vector2f(100, 300));
		uizonelist.setPosition(new Vector2f(0, 200));
		uizonelist.setBackgroundImage("miniion:modularback");
		uizonelist.setVisible(false);
		this.addDisplayElement(uizonelist);		
		
		btnStats = new UIModButtonArrow(new Vector2f(12,46), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.LEFT);
		btnStats.setPosition(new Vector2f(8,135));
		btnStats.setId("showstats");
		btnStats.addClickListener(executeArrowButton);
		btnStats.setVisible(true);
		backgroundmain.addDisplayElement(btnStats);				
		
		uistats = new UIScreenStats();
		uistats.setSize(new Vector2f(300,600));
		//edit this to minus the width of the window if you wonna change the size
		uistats.setPosition(new Vector2f(-300,0));
		//we'll make a new background for the stats, but for now this will do
		uistats.setBackgroundImage("miniion:modularback");
		uistats.setVisible(false);
		backgroundmain.addDisplayElement(uistats);
		
	}
	
	@Override
	public void open() {
		super.open();
		setModal(true);
		refreshScreen();
	}
	
	/**
	 * execute clicks on arrowbuttons
	 */
	private ClickListener executeArrowButton = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIModButtonArrow arrow = (UIModButtonArrow)element;
			if(arrow.getId() == "showbehaviour"){
				if(MinionSystem.getActiveMinion() != null){
					behaviourlist.setVisible(!behaviourlist.isVisible());
				}
			}else
			if(arrow.getId() == "showactions"){
				if(MinionSystem.getActiveMinion() != null){
					actionlist.setVisible(!actionlist.isVisible());
				}
			}else
			if(arrow.getId() == "showstats"){
				if(MinionSystem.getActiveMinion() != null){
					uistats.setVisible(!uistats.isVisible());
				}
			}else
			if(arrow.getId() == "previousminion"){
				MinionSystem.getNextMinion(false);
				refreshScreen();
			}else
			if(arrow.getId() == "nextminion"){
				MinionSystem.getNextMinion(false);
				refreshScreen();
			}
		}
	};
	
	private void closeAllFoldouts(){
		behaviourlist.setVisible(false);
		actionlist.setVisible(false);
		uistats.setVisible(false);
		uizonelist.setVisible(false);
	}
			
	/**
	 * sets the clicked toggle button and matching behaviour
	 */
	private ClickListener behaviourToggleListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIModButtonMenu clickedbutton = (UIModButtonMenu) element;
			toggleBehaviour(clickedbutton);									
			if(MinionSystem.getActiveMinion() != null){
				MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
				if (clickedbutton.getId() == "stay") {
					minioncomp.minionBehaviour = MinionBehaviour.Stay;
				}else
				if (clickedbutton.getId() == "foll") {
					minioncomp.minionBehaviour = MinionBehaviour.Follow;
				}else
				if (clickedbutton.getId() == "atta") {
					minioncomp.minionBehaviour = MinionBehaviour.Attack;
				}else
				if (clickedbutton.getId() == "gath") {
					minioncomp.minionBehaviour = MinionBehaviour.Gather;
				}else
				if (clickedbutton.getId() == "work") {
					minioncomp.minionBehaviour = MinionBehaviour.Work;
				}
				MinionSystem.getActiveMinion().saveComponent(minioncomp);
			}
			behaviourlist.setVisible(false);
		}
	};
	
	/**
	 * sets the clicked toggle button and matching behaviour
	 */
	private ClickListener actionListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIModButtonMenu clickedbutton = (UIModButtonMenu) element;
			if(MinionSystem.getActiveMinion() != null){
				MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
				if (clickedbutton.getId() == "inve") {
					MinionSystem.getActiveMinion().send(new ActivateEvent(MinionSystem.getActiveMinion(), CoreRegistry.get(LocalPlayer.class).getEntity()));
				}else
				if (clickedbutton.getId() == "zone") {
					uizonelist.removeAll();
					if(uizonelist.isVisible()){
						uizonelist.setVisible(false);
					}
					else{
						for (Zone zone : MinionSystem.getGatherZoneList()) {
							UIListItem listitem = new UIListItem(zone.Name, zone);
							listitem.addClickListener(zoneItemListener);
							uizonelist.addItem(listitem);
						}
						for (Zone zone : MinionSystem.getWorkZoneList()) {
							UIListItem listitem = new UIListItem(zone.Name, zone);
							listitem.addClickListener(zoneItemListener);
							uizonelist.addItem(listitem);
						}
						uizonelist.setVisible(true);
					}
				}else
				if (clickedbutton.getId() == "reci") {
					uizonelist.removeAll();
					if(uizonelist.isVisible()){
						uizonelist.setVisible(false);
					}
					else{
						for (MinionRecipe recipe : MinionSystem.getRecipesList()) {
							UIListItem listitem = new UIListItem(recipe.Name, recipe);
							listitem.addClickListener(recipeItemListener);
							uizonelist.addItem(listitem);
						}
						uizonelist.setVisible(true);
					}
				}else
				if (clickedbutton.getId() == "clea") {
					SimpleMinionAIComponent aicomp = MinionSystem.getActiveMinion().getComponent(SimpleMinionAIComponent.class);
					aicomp.ClearCommands();
					MinionSystem.getActiveMinion().saveComponent(aicomp);
					minioncomp.assignedzone = null;
					minioncomp.assignedrecipe = null;
					MinionSystem.getActiveMinion().saveComponent(minioncomp);
					refreshScreen();
				}else
				if (clickedbutton.getId() == "byeb") {
					//WARNING!!!! execute getprevious before setting the component to dying, 
					//else getprevious will have trouble determining what minion needs to become active!
					EntityRef dyingminion = MinionSystem.getActiveMinion();
					MinionSystem.getPreviousMinion(true);
					minioncomp.minionBehaviour = MinionBehaviour.Die;
					minioncomp.dying = true;
					dyingminion.saveComponent(minioncomp);
					closeAllFoldouts();
					refreshScreen();
				}				
			}
		}
	};
	
	private ClickListener zoneItemListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			Zone selectedzone = (Zone) ((UIListItem)element).getValue();
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			for (Zone zone : MinionSystem.getGatherZoneList()) {
				if (zone.Name.matches(selectedzone.Name)) {
					minioncomp.assignedzone = zone;
				}
			}
			for (Zone zone : MinionSystem.getWorkZoneList()) {
				if (zone.Name.matches(selectedzone.Name)) {
					minioncomp.assignedzone = zone;
				}
			}
			MinionSystem.getActiveMinion().saveComponent(minioncomp);
			uizonelist.setVisible(false);
			refreshScreen();
		}
	};
	
	private ClickListener recipeItemListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			MinionRecipe selectedrecipe = (MinionRecipe) ((UIListItem)element).getValue();
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			for (MinionRecipe recipe : MinionSystem.getRecipesList()) {
				if (recipe.Name.matches(selectedrecipe.Name)) {
					minioncomp.assignedrecipe = recipe;
				}
			}
			MinionSystem.getActiveMinion().saveComponent(minioncomp);
			uizonelist.setVisible(false);
			refreshScreen();
		}
	};
	
	/**
	 * make sure only 1 toggle is active in this displaycontainer
	 * @param button
	 * 				the selected button
	 */
	private void toggleBehaviour(UIModButtonMenu button){
		for(UIDisplayElement modbutton : behaviourlist.getDisplayElements()){
			if(modbutton.equals(button)){
				((UIModButtonMenu)modbutton).setToggleState(true);
			}else{
				((UIModButtonMenu)modbutton).setToggleState(false);
			}
		}
	}
	
	private void refreshScreen(){
		if(MinionSystem.getActiveMinion() == null){
			// remove and add border for resize
			// would be nice if I could lock the size to default size
			lblname.removeBorderSolid();		
			lblname.setText("No Oeon is obeying you!");
			lblname.setBorderSolid(new Vector4f(2f, 2f, 2f, 2f), Color.magenta);
			lblflavor.setText("Get your Oreominions now!!! 75% off if you bought any other DLC");
			lblzone.setText("");
			lblrecipe.setText("");            
		}else {
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			// remove and add border for resize
			// would be nice if I could lock the size to default size
			lblname.removeBorderSolid();
			if(minioncomp == null){
				lblname.setText("missing component");
				lblflavor.setText("something went wrong, contact your system administrator! Quickly!");
			}else{
				lblname.setText(minioncomp.name);
				lblflavor.setText(minioncomp.flavortext);				
				if(minioncomp.assignedzone == null){
					lblzone.setText("no zone assigned");
				}else
				{
					lblzone.setText("workzone : " + minioncomp.assignedzone.Name);
				}
				if(minioncomp.assignedrecipe == null){
					lblrecipe.setText("");
				}else
				{
					String tmpstr = " requires : ";
					for(String resource : minioncomp.assignedrecipe.craftRes){
						tmpstr = tmpstr.concat(resource + ", ");
					}					
					lblrecipe.setText("recipe : " + minioncomp.assignedrecipe.Name + tmpstr.substring(0, tmpstr.lastIndexOf(",")));
				}
				if (minioncomp.minionBehaviour == MinionBehaviour.Follow) {
					toggleBehaviour(btnFollow);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Stay) {
					toggleBehaviour(btnStay);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Attack) {
					toggleBehaviour(btnAttack);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Gather) {
					toggleBehaviour(btnGather);
				} else {
					toggleBehaviour(null);
				}
			}			
			lblname.setBorderSolid(new Vector4f(2f, 2f, 2f, 2f), Color.magenta);						
		}
		//refresh the stats screen whenever the main window refreshes.
		uistats.refreshScreen();
	}	
}
	
