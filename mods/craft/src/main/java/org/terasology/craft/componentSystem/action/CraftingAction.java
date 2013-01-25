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
package org.terasology.craft.componentSystem.action;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.craft.components.actions.CraftingActionComponent;
import org.terasology.craft.components.utility.CraftRecipeComponent;
import org.terasology.craft.rendering.CraftingGrid;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.craft.events.crafting.AddItemEvent;
import org.terasology.craft.events.crafting.ChangeLevelEvent;
import org.terasology.craft.events.crafting.CheckRefinementEvent;
import org.terasology.craft.events.crafting.DeleteItemEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.AABB;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.*;
import org.terasology.world.block.management.BlockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Small-Jeeper
 */
@RegisterComponentSystem
public class CraftingAction implements EventHandlerSystem {
    private WorldProvider worldProvider;
    private EntityManager entityManager;
    private Map<String, ArrayList<Prefab>> entitesWithRecipes = Maps.newHashMap();
    private Map<String, ArrayList<RefinementData>> entitesWithRefinement = Maps.newHashMap();
    private static final String EMPTY_ROW = " ";
    private static final Logger logger = LoggerFactory.getLogger(CraftingAction.class);

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        entityManager = CoreRegistry.get(EntityManager.class);

        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        //prefMan.getPrefab()
        for ( Prefab prefab : prefMan.listPrefabs(CraftRecipeComponent.class)){
            CraftRecipeComponent recipe = prefab.getComponent(CraftRecipeComponent.class);

            if ( recipe.refinement.size() > 0 ){
                for ( Map<String, String> refinement : recipe.refinement.values() ){
                    if ( refinement.containsKey("instigator") && refinement.containsKey("target") ){

                        RefinementData refinementData = new RefinementData();
                        refinementData.instigator = refinement.get("instigator").toLowerCase();
                        refinementData.target     = refinement.get("target").toLowerCase();

                        if ( refinement.containsKey("resultCount") ){
                            try{
                                refinementData.resultCount = Byte.parseByte( refinement.get("resultCount") );
                            }catch(NumberFormatException exception){
                                logger.warn("Refinement: {}. The resultCount must be a byte!", prefab.getName());
                            }
                        }

                        refinementData.resultPrefab = prefab;

                        if ( !entitesWithRefinement.containsKey( refinementData.target ) ){
                            entitesWithRefinement.put(refinementData.target, new ArrayList<RefinementData>());
                        }
                        logger.info("Found refinement: {}", prefab.getName());
                        entitesWithRefinement.get( refinementData.target ).add(refinementData);
                    }
                }
            }

            if ( recipe.recipe.size() > 0){
                String key = getRecipeKey(recipe.recipe);
                if ( !entitesWithRecipes.containsKey(key)){
                    entitesWithRecipes.put(key, new ArrayList<Prefab>());
                }

                logger.info("Found recipe: {}", prefab.getName());
                entitesWithRecipes.get(key).add(prefab);
            }

        }
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CraftingActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        CraftingActionComponent craftingComponent = entity.getComponent(CraftingActionComponent.class);

        if ( !craftingComponent.possibleItem.equals(EntityRef.NULL) ){
            EntityRef player = CoreRegistry.get(LocalPlayer.class).getEntity();
            player.send(new ReceiveItemEvent(craftingComponent.possibleItem));
            decreaseItems(entity, player);

            checkEmptyCraftBlock(entity);

            if ( entity.exists() ){
                EntityRef possibleCraft = tryCraft(entity);

                craftingComponent.possibleItem = possibleCraft;
                entity.saveComponent(craftingComponent);
            }

        }else{
            entity.send(new AddItemEvent(event.getTarget(), event.getInstigator()));
        }
    }

    @ReceiveEvent(components = {CraftingActionComponent.class})
    public void onAddItem(AddItemEvent event, EntityRef entity) {
        CraftingActionComponent craftingComponent = entity.getComponent(CraftingActionComponent.class);

        int selectedCell = getSelectedItemFromCraftBlock(entity, craftingComponent.getCurrentLevel());
        EntityRef entityFromPlayer   = event.getInstigator();
        EntityRef selectedEntity = craftingComponent.getCurrentLevelElements().get(selectedCell);

        float percent = event.getPercent();
        byte sendingCount  = 1;
        byte returnedCount = 0;

        /*if( craftingComponent.lastSelected.equals(selectedEntity) ){
            return;
        }

        craftingComponent.lastSelected = selectedEntity;  */

        ItemComponent playerItem = entityFromPlayer.getComponent(ItemComponent.class);

        if ( playerItem == null){
            return;
        }


        if ( percent > 0 && playerItem.stackCount > 1 ) {
            sendingCount = (byte)Math.round( percent * playerItem.stackCount );

            playerItem.stackCount -= sendingCount;

            if(playerItem.stackCount < 0) {
                playerItem.stackCount = 0;
                sendingCount--;
            }
        } else {
            playerItem.stackCount--;
        }

        ItemComponent craftItem  = selectedEntity.getComponent(ItemComponent.class);

        if ( craftItem != null &&  craftItem.name.toLowerCase().equals(playerItem.name.toLowerCase())){

            if ( craftItem.stackCount >= InventorySystem.MAX_STACK ){
                return;
            }

            if ( (craftItem.stackCount + sendingCount ) > InventorySystem.MAX_STACK ){
                returnedCount = (byte)( (craftItem.stackCount + sendingCount) - InventorySystem.MAX_STACK);
                craftItem.stackCount = InventorySystem.MAX_STACK;

            }else{
                craftItem.stackCount += sendingCount;
            }

            selectedEntity.saveComponent(craftItem);

        }else if ( selectedEntity.equals(EntityRef.NULL) ){

            if (entityFromPlayer.getComponent(ItemComponent.class) == null){
                return;
            }

            EntityRef entityToCraftBlock =  entityManager.copy(entityFromPlayer);
            craftItem  = entityToCraftBlock.getComponent(ItemComponent.class);

            craftItem.stackCount = sendingCount;
            entityToCraftBlock.saveComponent(craftItem);
            craftingComponent.getCurrentLevelElements().set( selectedCell, entityToCraftBlock);

        }else{
            EntityRef entityToCraftBlock =  entityManager.copy(entityFromPlayer);
            craftItem  = entityToCraftBlock.getComponent(ItemComponent.class);

            craftItem.stackCount = sendingCount;
            entityToCraftBlock.saveComponent(craftItem);

            EntityRef player = CoreRegistry.get(LocalPlayer.class).getEntity();
            ItemComponent tItem  = selectedEntity.getComponent(ItemComponent.class);
            tItem.container = EntityRef.NULL;
            selectedEntity.saveComponent(tItem);
            player.send(new ReceiveItemEvent(entityManager.copy(selectedEntity)));
            craftingComponent.getCurrentLevelElements().set( selectedCell, entityToCraftBlock);
        }

        playerItem.stackCount += returnedCount;

        if (playerItem.stackCount == 0) {
            entityFromPlayer.destroy();
        } else {
            entityFromPlayer.saveComponent(playerItem);
        }

        
        entity.saveComponent(craftingComponent);

        craftingComponent.possibleItem = tryCraft(entity);
        entity.saveComponent(craftingComponent);

    }

    /*
     * If Changed "Y position" of craft grid
     */
    @ReceiveEvent(components = {CraftingActionComponent.class})
    public void onChangeLevel(ChangeLevelEvent event, EntityRef entity) {
        if (event.isDecreaseEvent()){
            entity.getComponent(CraftingActionComponent.class).decreaseLevel();
        }else{
            entity.getComponent(CraftingActionComponent.class).increaseLevel();
        }
    }

    @ReceiveEvent(components = {CraftingActionComponent.class})
    public void onDeleteItem(DeleteItemEvent event, EntityRef entity){
        CraftingActionComponent craftingComponent = entity.getComponent(CraftingActionComponent.class);

        int selectedCell = getSelectedItemFromCraftBlock(entity, craftingComponent.getCurrentLevel());
        EntityRef selectedEntity = craftingComponent.getCurrentLevelElements().get(selectedCell);

        if (selectedEntity.equals(EntityRef.NULL)){
            return;
        }

        float percent = event.getPercent();
        byte sendingCount  = 1;
        ItemComponent craftItem  = selectedEntity.getComponent(ItemComponent.class);

        if ( percent > 0 && craftItem.stackCount > 1 ){
            sendingCount = (byte)Math.round( percent * craftItem.stackCount );
            craftItem.stackCount -= sendingCount;

            if (craftItem.stackCount < 0) {
                craftItem.stackCount = 0;
                sendingCount--;
            }
        }else{
            craftItem.stackCount--;
        }

        //Send item to player
        EntityRef entityForPlayer = entityManager.copy(selectedEntity);
        ItemComponent entityForPlayerItem  = entityForPlayer.getComponent(ItemComponent.class);
        entityForPlayerItem.container = EntityRef.NULL;
        entityForPlayerItem.stackCount = sendingCount;
        entityForPlayer.saveComponent(entityForPlayerItem);

        EntityRef player = CoreRegistry.get(LocalPlayer.class).getEntity();
        player.send(new ReceiveItemEvent(entityForPlayer));


        if (craftItem.stackCount == 0) {
            craftingComponent.deleteItem( getSelectedItemFromCraftBlock(entity, craftingComponent.getCurrentLevel()) );
            entity.saveComponent(craftingComponent);
        } else {
            selectedEntity.saveComponent(craftItem);
        }

        checkEmptyCraftBlock(entity);

        if ( entity.exists() ){

            EntityRef possibleCraft = tryCraft(entity);

            if ( !possibleCraft.equals(EntityRef.NULL) ){
                craftingComponent.possibleItem = possibleCraft;
                entity.saveComponent(craftingComponent);
            }else if ( !craftingComponent.possibleItem.equals(EntityRef.NULL) ){
                craftingComponent.possibleItem = EntityRef.NULL;
                entity.saveComponent(craftingComponent);
            }
        }
    }

    @ReceiveEvent(components = {CraftingActionComponent.class})
    public void checkRefinement(CheckRefinementEvent event, EntityRef entity){
        CraftingActionComponent craftingComponent = entity.getComponent(CraftingActionComponent.class);

        if ( event.getInstigator().equals(EntityRef.NULL ) || entity.equals(EntityRef.NULL ) ){
            disablePossibleItem(craftingComponent);
            return;
        }

        LocalPlayerComponent localPlayer = event.getInstigator().getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = event.getInstigator().getComponent(InventoryComponent.class);

        if ( localPlayer == null || inventory == null ){
            disablePossibleItem(craftingComponent);
            return;
        }

        if ( craftingComponent.getAllElements().size() > 1 ){
            return;
        }else{
            int countNotNulledElements = 0;
            for(EntityRef element : craftingComponent.getCurrentLevelElements() ){
                if ( !element.equals(EntityRef.NULL) ){
                    countNotNulledElements++;
                }

                if (countNotNulledElements > 1){
                    disablePossibleItem(craftingComponent);
                    return;
                }
            }
        }

        UIItemContainer toolbar = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");
        ItemComponent instigatorItem = inventory.itemSlots.get(toolbar.getSlotStart() + localPlayer.selectedTool).getComponent(ItemComponent.class);

        int selectedCell = getSelectedItemFromCraftBlock(entity, craftingComponent.getCurrentLevel());

        EntityRef selectedEntity = craftingComponent.getCurrentLevelElements().get(selectedCell);

        ItemComponent targetItem = selectedEntity.getComponent(ItemComponent.class);

        if ( instigatorItem == null || targetItem == null){
            disablePossibleItem(craftingComponent);
            return;
        }

        String instigatorName = instigatorItem.name.toLowerCase();
        String targetName     = targetItem.name.toLowerCase();

        if ( entitesWithRefinement.containsKey(targetName) && !entitesWithRefinement.get(targetName).isEmpty() ){

            for (RefinementData refinementData : entitesWithRefinement.get(targetName)){
                if( refinementData.instigator.equals( instigatorName ) ){

                    CraftRecipeComponent craftRecipe = refinementData.resultPrefab.getComponent(CraftRecipeComponent.class);
                    craftRecipe.resultCount = refinementData.resultCount;
                    refinementData.resultPrefab.setComponent(craftRecipe);

                    EntityRef refinementElement = createNewElement( refinementData.resultPrefab );

                    if (!refinementElement.equals(EntityRef.NULL)){
                        craftingComponent.possibleItem = refinementElement;
                        craftingComponent.isRefinement = true;
                        entity.saveComponent(craftingComponent);
                    }

                    break;
                }
            }
        }else{

            if (craftingComponent.isRefinement){

                if ( !craftingComponent.possibleItem.equals(EntityRef.NULL) ){
                    craftingComponent.possibleItem = EntityRef.NULL;
                }

                craftingComponent.isRefinement = false;
                entity.saveComponent(craftingComponent);
            }

        }
    }

    private void disablePossibleItem(CraftingActionComponent craftingComponent){
        if (craftingComponent.isRefinement){
            craftingComponent.isRefinement = false;

            if ( !craftingComponent.possibleItem.equals(EntityRef.NULL) ){
                craftingComponent.possibleItem = EntityRef.NULL;
            }

        }
    }

    /*
     * Check current craft block for the recipe
     */
    private EntityRef tryCraft(EntityRef entity){

        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        CraftingActionComponent craftingComponent = entity.getComponent(CraftingActionComponent.class);
        Map<String, List<String>> possibleRecipe = Maps.newHashMap();

        //Converting entites from craft block to the string recipe

        for(String level: CraftingActionComponent.levels){

            int countNotEmptyElements = 0;
            ArrayList<EntityRef> craftLevel = craftingComponent.getLevelElements(level);
            ArrayList<String> translatedLevel = new ArrayList<String>();

            if ( craftLevel!= null ){
                for(EntityRef craftElement : craftLevel){
                    ItemComponent item = craftElement.getComponent(ItemComponent.class);

                    if (item != null){
                        translatedLevel.add(item.name.toLowerCase());
                        countNotEmptyElements++;
                    }else{
                        translatedLevel.add(EMPTY_ROW);
                    }

                }
                possibleRecipe.put(level, translatedLevel);
            }
        }

        String searchingKey = getRecipeKey(possibleRecipe);

        if (entitesWithRecipes.containsKey(searchingKey)){
            boolean isRecipe = false;
            for(Prefab prefabWithRecipe : entitesWithRecipes.get(searchingKey)){

                CraftRecipeComponent craftRecipe = prefabWithRecipe.getComponent(CraftRecipeComponent.class);

                RecipeMatrix possibleRecipeMatrix = new RecipeMatrix(possibleRecipe, 3, 3);
                RecipeMatrix craftMatrix          = new RecipeMatrix(craftRecipe.recipe, 3, 3);

                isRecipe = !craftRecipe.fullMatch ?
                        possibleRecipeMatrix.trim().equals(craftMatrix.trim()) :
                        possibleRecipeMatrix.equals(craftMatrix);

                //Recipe founded. Return result Entity!
                if (isRecipe){
                    return createNewElement(prefabWithRecipe);
                }
            }
        }

        return EntityRef.NULL;
    }

    private EntityRef createNewElement(Prefab prefab){

        PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
        Prefab resultPrefab = prefab;
        CraftRecipeComponent craftRecipe = prefab.getComponent(CraftRecipeComponent.class);

        String name = "";

        if (craftRecipe.type != CraftRecipeComponent.CraftRecipeType.SELF &&  craftRecipe.result.isEmpty() ){
            logger.warn("The recipe does not have result name");
            return EntityRef.NULL;
        }

        if ( craftRecipe.type != CraftRecipeComponent.CraftRecipeType.SELF ){
            resultPrefab =  prefMan.getPrefab(craftRecipe.result);
            name = craftRecipe.result;
        }else{
            resultPrefab = prefab;
            name = resultPrefab.getName();
        }

        EntityRef recipe = EntityRef.NULL;
        EntityRef result = EntityRef.NULL;

        if ( resultPrefab != null ){
            recipe = entityManager.create(resultPrefab.listComponents());
        }


        if ( recipe.equals(EntityRef.NULL) || recipe.getComponent(ItemComponent.class) == null ){
            Block recipeBlock = null;
            BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
            if ( craftRecipe.type != CraftRecipeComponent.CraftRecipeType.SELF ){
                result = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(craftRecipe.result));
            }else{
                recipeBlock = BlockManager.getInstance().getBlock(resultPrefab);

                if ( recipeBlock != null ){
                    result = blockFactory.newInstance(recipeBlock.getBlockFamily(), recipe);
                }
            }


        }else{
            result = recipe;
            ItemComponent oldItem = result.getComponent(ItemComponent.class);

            ItemComponent newItem = new ItemComponent();

            newItem.stackCount    = oldItem.stackCount;
            newItem.container     = oldItem.container;
            newItem.name          = oldItem.name;
            newItem.baseDamage    = oldItem.baseDamage;
            newItem.consumedOnUse = oldItem.consumedOnUse;
            newItem.icon          = oldItem.icon;
            newItem.stackId       = oldItem.stackId;
            newItem.renderWithIcon = oldItem.renderWithIcon;
            newItem.usage = oldItem.usage;

            result.saveComponent(newItem);
        }


        ItemComponent item = result.getComponent(ItemComponent.class);

        if ( item != null ){
            CraftRecipeComponent recipeComponent = prefab.getComponent(CraftRecipeComponent.class);
            item.stackCount = recipeComponent.resultCount;
            result.saveComponent(item);
        }else{
            logger.warn("Failed to create entity with name {}", name);
            result = EntityRef.NULL;
        }



        return result;
    }

    /*
     * Decrease stackCount of the item
     */

    private void decreaseItems(EntityRef craftBlockEntity, EntityRef playerEntity){
        CraftingActionComponent craftingComponent = craftBlockEntity.getComponent(CraftingActionComponent.class);

        if ( craftingComponent == null ){
            return;
        }

        int countLevels = craftingComponent.getAllElements().size();

        for( int i=0; i<countLevels; i++){
            for(int j=0; j<CraftingActionComponent.MAX_SLOTS; j++){
                ArrayList<EntityRef> list = craftingComponent.getLevelElements(CraftingActionComponent.levels[i]);

                if ( list == null ){
                    continue;
                }

                EntityRef itemEntity = list.get(j);

                if ( itemEntity != null ){
                    ItemComponent item  = itemEntity.getComponent(ItemComponent.class);
                    if ( item == null ){
                        continue;
                    }

                    item.stackCount--;

                    if ( item.stackCount <= 0 ){
                        craftingComponent.deleteItem(i,j);
                    }else{
                        itemEntity.saveComponent(item);
                    }
                }

            }
        }

        if( craftingComponent.isRefinement ){

            LocalPlayerComponent localPlayer = playerEntity.getComponent(LocalPlayerComponent.class);
            InventoryComponent inventory = playerEntity.getComponent(InventoryComponent.class);

            ItemComponent instigatorItem = inventory.itemSlots.get(localPlayer.selectedTool).getComponent(ItemComponent.class);

            instigatorItem.stackCount--;

            if( instigatorItem.stackCount < 1 ){
                inventory.itemSlots.get(localPlayer.selectedTool).destroy();
            }

        }


    }

    /*
     * Check craft block for the emptiness
     */
    private void checkEmptyCraftBlock(EntityRef craftBlockEntity){
        CraftingActionComponent craftingComponent = craftBlockEntity.getComponent(CraftingActionComponent.class);

        if ( craftingComponent.getAllElements().size() == 0 ){

            BlockComponent blockComp = craftBlockEntity.getComponent(BlockComponent.class);
            Block currentBlock = worldProvider.getBlock(blockComp.getPosition());
            worldProvider.setBlock(blockComp.getPosition(), BlockManager.getInstance().getAir(), currentBlock);

            craftBlockEntity.destroy();
            return;
        }
    }

    private int getSelectedItemFromCraftBlock(EntityRef entity, int level){
        AABB aabb = null;
        int blockSelected = 0;
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);

        if (blockComp != null) {
            Block newBlock = worldProvider.getBlock(blockComp.getPosition());
            if (newBlock.isTargetable()) {
                aabb = newBlock.getBounds(blockComp.getPosition());
                CraftingGrid craftingGridRenderer = new CraftingGrid();
                craftingGridRenderer.setAABB(aabb, level);
                blockSelected = craftingGridRenderer.getSelectedBlock();
            }
        }

        return blockSelected;
    }

    private String getRecipeKey(Map<String, List<String>> map){
        String key = "" + map.size();

        for(List<String> currentLevel : map.values()){
            int countNotEmptyElements = 0;
            for(String element: currentLevel){
                if ( !element.equals(EMPTY_ROW) ){
                    countNotEmptyElements++;
                }
            }
            key += "-" + countNotEmptyElements;
        }

        return key;
    }

    private static class RefinementData{
        public byte resultCount = 1;
        public String instigator = "";
        public String target = "";
        public Prefab resultPrefab = null;
    }

    private static class RecipeMatrix{
        public int width  = 3;
        public int height = 3;
        public Map<String, List<String>> recipe = null;
        public static String EMPTY_ROW = " ";

        public RecipeMatrix(Map<String, List<String>> recipe){
            this(recipe, 3, 3);
        }

        public RecipeMatrix(Map<String, List<String>> recipe, int width, int height){
            this.recipe = recipe;
            this.width  = width;
            this.height = height;
        }

        /*
         * Deleted empty columns and rows
         *
         * For example, we have some matrix like this:
         *
         * 1 0 1
         * 1 0 1
         * 0 0 0
         *
         */
        public RecipeMatrix trim(){
            HashMap<String, List<String>> matrix = new HashMap<String, List<String>>();

            ArrayList<Integer> counterLines = new ArrayList<Integer>(width);
            ArrayList<Integer> counterColumns = new ArrayList<Integer>(height);
            int countLevels = recipe.size();

            //calculate count for empty rows
            for(int i = 0; i < height; i++){
                if ( counterLines.size() < (i + 1) ){
                    counterLines.add(0);
                }
                for(int j = 0; j < width; j++){
                    for(List<String> currentLevel : recipe.values() ){
                        if ( currentLevel.get(i*width + j).equals(EMPTY_ROW) ){
                            counterLines.set(i, counterLines.get(i) + 1);
                        }
                    }
                }
            }

            /*
             * Now we know that our matrix has one line
             * But we cant delete the line if it is between two non-empty lines
             */
            if ( counterLines.size() == 3 &&
                    counterLines.get(1) == countLevels*width &&
                    counterLines.get(0) < countLevels*width  &&
                    counterLines.get(2) < countLevels*width){
                counterLines.set(1, 0);
            }

            //calculate count for empty columns
            for(int i = 0; i < width; i++){
                if ( counterColumns.size() < (i + 1) ){
                    counterColumns.add(0);
                }
                for(int j = 0; j < height; j++){
                    for(List<String> currentLevel : recipe.values() ){
                        if ( currentLevel.get(j*width + i).equals(EMPTY_ROW)){
                            counterColumns.set(i, counterColumns.get(i) + 1);
                        }
                    }
                }
            }


            if ( counterColumns.size() == 3 &&
                    counterColumns.get(1) == countLevels*height &&
                    counterColumns.get(0) < countLevels*height  &&
                    counterColumns.get(2) < countLevels*height){
                counterColumns.set(1, 0);
            }


            int countLines = 0;

            if (counterLines.isEmpty() && counterColumns.isEmpty()){
                return this;
            }

            //create new matrix without empty lines
            for(int i=0; i<height; i++){
                if ( counterLines.get(i) < countLevels*width ){
                    for(String key : recipe.keySet()){
                        if (!matrix.containsKey(key)){
                            matrix.put(key, new ArrayList<String>());
                        }

                        for(int j=0; j<height; j++){
                            matrix.get(key).add( recipe.get(key).get(i*width + j));
                        }
                    }
                    countLines++;
                }
            }

            /*
             * Now we have this result:
             *
             * 1 0 1
             * 1 0 1
             *
             */

            int countColumns = width;


            //delete from the new matrix empty columns
            for(int i=0, tCounter=0; i<countColumns; i++, tCounter++){
                if ( counterColumns.get(tCounter) == countLevels*height ){
                    for(String key : recipe.keySet()){
                        if (!matrix.containsKey(key)){
                            matrix.put(key, new ArrayList<String>());
                        }

                        for(int j=0; j<countLines; j++){
                            int t = j*(countColumns - 1) + i;
                            matrix.get(key).remove(t);
                        }
                    }
                    countColumns--;
                    //counterColumns.set( i, counterColumns.get(i) - 1 );
                    i--;
                }
            }


            return new RecipeMatrix(matrix, countColumns, countLines);
        }

        /*
         * Rotate matrix
         *
         * input:     output:
         *
         * 1 0 2      1 3 1
         * 3 4 8      8 4 0
         * 1 8 7      7 8 2
         *
         */

        public RecipeMatrix rotate(){

            RecipeMatrix rotatedMatrix = new RecipeMatrix(new HashMap<String, List<String>>());

            for(String key : recipe.keySet()){
                ArrayList<String> buff = new ArrayList<String>();

                rotatedMatrix.recipe.put(key, buff);

                for(int i=0; i < height; i++){
                    for(int j=0; j < width; j++){
                        int index = height*(width - j - 1) + i;
                        buff.add( recipe.get(key).get(index<0?0:index) );
                    }
                }


            }

            rotatedMatrix.width  = height;
            rotatedMatrix.height = width;

            return rotatedMatrix;
        }

        public boolean equals(RecipeMatrix matrix){

            if ( recipe.size() != matrix.recipe.size() ){
                return false;
            }

            for(int i = 0; i < 4; i++){
                if (matrix.width != width && matrix.height != height){
                    matrix = matrix.rotate();
                    continue;
                }

                boolean found = true;

                for( String key : recipe.keySet() ){
                    int trace1 = getTrace(key);
                    int trace2 = matrix.getTrace(key);

                    if ( getTrace(key) != matrix.getTrace(key) || !recipe.get(key).equals( matrix.recipe.get(key) ) ){
                        found = false;
                        break;
                    }
                }

                if ( found ){
                    return true;
                }

                matrix = matrix.rotate();
            }

            for(String key : recipe.keySet()){
                if ( !matrix.recipe.containsKey(key) ){
                    return false;
                }

            }

            return false;
        }

        public int getTrace(String level){
            String trace = "";

            int min = Math.min(width, height);

            for(int i=0; i < min; i++){
                trace += recipe.get(level).get( i * min + i );
            }

            return trace.hashCode();
        }

        public RecipeMatrix clone(){
            return new RecipeMatrix(this.recipe, this.width, this.height);
        }
    }
}
