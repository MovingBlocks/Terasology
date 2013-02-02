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
package org.terasology.model.inventory;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

/**
 * Icon for rendering items in inventory.
 */
public class Icon {
    private static Map<String, Icon> icons;

    private UIImage _element;
    private BlockFamily _blockFamily;
    private int _x;
    private int _y;
    private Texture terrainTex;

    /**
     * Creates Icon for BlockFamily class.
     *
     * @param blockFamily
     */
    public Icon(BlockFamily blockFamily) {
        _element = null;
        _blockFamily = blockFamily;
        setAtlasPosition(0, 0);
        terrainTex = Assets.getTexture("engine:terrain");
    }

    /**
     * Creates an Icon for a non-BlockFamily class
     */
    public Icon(String simpleuri) {
        _element = new UIImage(Assets.getTexture(simpleuri));
        _blockFamily = null;

        _element.setSize(new Vector2f(32, 32));
        _element.setTextureSize(new Vector2f(16, 16));
        _element.setVisible(true);
        _element.setPosition(new Vector2f(-10f, -16f));

        setAtlasPosition(0, 0);
    }
    
    public Icon(String simpleuri, int atlasx, int atlasy) {
        this(simpleuri);
        setAtlasPosition(atlasx, atlasy);
    }

    /**
     * Returns the icon for <code>name</code>.
     *
     * @param name the name of the icon
     * @return the Icon for item
     */
    public static Icon get(String name) {
        if (icons == null) {
            loadIcons();
        }

        return icons.get(name.toLowerCase(Locale.ENGLISH));
    }
    
    public static void set(String name, String simpleuri, int atlasx, int atlasy){
    	if (icons == null) {
            loadIcons();
        }
    	Icon addicon = new Icon(simpleuri, atlasx, atlasy);
    	icons.put(name, addicon);
    }

    private static void loadIcons() {
        icons = new HashMap<String, Icon>();
        String simpleuri = "engine:items";
        // TODO: Hmm, does this mean we have hard coded our tool displays? Should try to move this to ToolManager in that case?
        // TODO: I'ld suggest an icon atlas asset
        Icon questionMarkIcon = new Icon(simpleuri);

        //* TOOLS *//
        Icon pickAxeIcon = new Icon(simpleuri);
        Icon axeIcon = new Icon(simpleuri);
        Icon sickleIcon = new Icon(simpleuri);
        Icon hammerIcon = new Icon(simpleuri);
        Icon knifeIcon = new Icon(simpleuri);
        Icon swordIcon = new Icon(simpleuri);
        Icon bowIcon = new Icon(simpleuri);
        Icon xbowIcon = new Icon(simpleuri);

        //* VIALS & POTIONS *//
        Icon emptyVialIcon = new Icon(simpleuri);
        Icon redVialIcon = new Icon(simpleuri);
        Icon orangeVialIcon = new Icon(simpleuri);
        Icon greenVialIcon = new Icon(simpleuri);
        Icon purpleVialIcon = new Icon(simpleuri);
        Icon ceruleanVialIcon = new Icon(simpleuri);
        Icon blueVialIcon = new Icon(simpleuri);
        Icon blackVialIcon = new Icon(simpleuri);
        //* POWDER REAGENTS *//
        Icon whitePowderIcon = new Icon(simpleuri);
        Icon paleredPowderIcon = new Icon(simpleuri);
        Icon palebluePowderIcon = new Icon(simpleuri);
        Icon greenPowderIcon = new Icon(simpleuri);
        Icon brownPowderIcon = new Icon(simpleuri);
        Icon redPowderIcon = new Icon(simpleuri);
        Icon bluePowderIcon = new Icon(simpleuri);
        Icon purplePowderIcon = new Icon(simpleuri);
        //* PLANTS *//
        Icon mandrakeIcon = new Icon(simpleuri);
        Icon wildRoseIcon = new Icon(simpleuri);
        Icon amanitaIcon = new Icon(simpleuri);
        Icon purpleHazeIcon = new Icon(simpleuri);
        Icon goldBloomIcon = new Icon(simpleuri);
        //* Other Tools *//
        Icon bowlIcon = new Icon(simpleuri);
        Icon heatedflaskIcon = new Icon(simpleuri);
        Icon scissorsIcon = new Icon(simpleuri);
        Icon candleIcon = new Icon(simpleuri);
        Icon dynamiteIcon = new Icon(simpleuri);
        Icon dynamitexlIcon = new Icon(simpleuri);
        //* BOOKs & RECIPEs *//
        Icon recipeIcon = new Icon(simpleuri);
        Icon bookIcon = new Icon(simpleuri);
        Icon redBookIcon = new Icon(simpleuri);
        Icon blueBookIcon = new Icon(simpleuri);
        //* MISC. *//
        Icon appleIcon = new Icon(simpleuri);
        Icon bannanaIcon = new Icon(simpleuri);
        Icon emptyJarIcon = new Icon(simpleuri);
        Icon waterJarIcon = new Icon(simpleuri);
        Icon coal = new Icon(simpleuri);
        Icon stick = new Icon(simpleuri);
        Icon refinementrock = new Icon(simpleuri);

        //* INGOTS *//
        Icon ironingotIcon = new Icon(simpleuri);
        Icon copperingotIcon = new Icon(simpleuri);
        Icon goldingotIcon = new Icon(simpleuri);
        Icon shadowingotIcon = new Icon(simpleuri);
        //* BOWCRAFTING & FLETCHING *//
        Icon woodshaftIcon = new Icon(simpleuri);
        //* Furniture *//
        Icon doorIcon = new Icon(simpleuri);

        questionMarkIcon.setAtlasPosition(4,0);

        //Tool Atlas
        pickAxeIcon.setAtlasPosition(0, 0);
        axeIcon.setAtlasPosition(0, 1);
        sickleIcon.setAtlasPosition(0, 2);
        hammerIcon.setAtlasPosition(0, 3);
        knifeIcon.setAtlasPosition(0, 4);
        swordIcon.setAtlasPosition(0, 5);
        bowIcon.setAtlasPosition(0, 6);
        xbowIcon.setAtlasPosition(0, 7);
        //Potion Atlas
        emptyVialIcon.setAtlasPosition(1, 0);
        redVialIcon.setAtlasPosition(1, 1);
        orangeVialIcon.setAtlasPosition(1, 2);
        greenVialIcon.setAtlasPosition(1, 3);
        purpleVialIcon.setAtlasPosition(1, 4);
        ceruleanVialIcon.setAtlasPosition(1, 5);
        blueVialIcon.setAtlasPosition(1, 6);
        blackVialIcon.setAtlasPosition(1, 7);
        //Reagent Atlas
        whitePowderIcon.setAtlasPosition(2, 0);
        redPowderIcon.setAtlasPosition(2, 1);
        paleredPowderIcon.setAtlasPosition(2, 2);
        greenPowderIcon.setAtlasPosition(2, 3);
        purplePowderIcon.setAtlasPosition(2, 4);
        palebluePowderIcon.setAtlasPosition(2, 5);
        bluePowderIcon.setAtlasPosition(2, 6);
        brownPowderIcon.setAtlasPosition(2, 7);
        //Plant Atlas
        goldBloomIcon.setAtlasPosition(3, 0);
        wildRoseIcon.setAtlasPosition(3, 1);
        amanitaIcon.setAtlasPosition(3, 3);
        purpleHazeIcon.setAtlasPosition(3, 4);
        mandrakeIcon.setAtlasPosition(3, 7);
        //Other Tools Atlas
        bowlIcon.setAtlasPosition(4, 1);
        heatedflaskIcon.setAtlasPosition(4, 2);
        scissorsIcon.setAtlasPosition(4, 3);
        candleIcon.setAtlasPosition(4, 5);
        dynamiteIcon.setAtlasPosition(4, 6);
        dynamitexlIcon.setAtlasPosition(4, 7);
        //Text Atlas
        recipeIcon.setAtlasPosition(5, 0);
        bookIcon.setAtlasPosition(5, 1);
        redBookIcon.setAtlasPosition(5, 2);
        blueBookIcon.setAtlasPosition(5, 3);
        //Misc.
        appleIcon.setAtlasPosition(6, 0);
        bannanaIcon.setAtlasPosition(6, 1);
        emptyJarIcon.setAtlasPosition(7, 0);
        waterJarIcon.setAtlasPosition(7, 1);
        coal.setAtlasPosition(9,0);
        stick.setAtlasPosition(9,2);
        refinementrock.setAtlasPosition(10,0);

        //Ingot Atlas
        ironingotIcon.setAtlasPosition(8, 0);
        copperingotIcon.setAtlasPosition(8, 1);
        goldingotIcon.setAtlasPosition(8, 2);
        shadowingotIcon.setAtlasPosition(8, 3);
        //Resources for Bowcraft-&-Fletching Atlas
        woodshaftIcon.setAtlasPosition(9, 1);
        // Furniture
        doorIcon.setAtlasPosition(6, 2);

        icons.put("questionmark", questionMarkIcon);

        icons.put("pickaxe", pickAxeIcon);
        icons.put("axe", axeIcon);
        icons.put("sickle", sickleIcon);
        icons.put("hammer", hammerIcon);
        icons.put("knife", knifeIcon);
        icons.put("sword", swordIcon);
        icons.put("bow", bowIcon);
        icons.put("xbow", xbowIcon);

        icons.put("emptyvial", emptyVialIcon);
        icons.put("redvial", redVialIcon);
        icons.put("orangevial", orangeVialIcon);
        icons.put("greenvial", greenVialIcon);
        icons.put("purplevial", purpleVialIcon);
        icons.put("ceruleanvial", ceruleanVialIcon);
        icons.put("bluevial", blueVialIcon);
        icons.put("blackvial", blackVialIcon);

        icons.put("whitepwdr", whitePowderIcon);
        icons.put("redpwdr", redPowderIcon);
        icons.put("paleredpwdr", paleredPowderIcon);
        icons.put("greenpwdr", greenPowderIcon);
        icons.put("purplepwdr", purplePowderIcon);
        icons.put("palebluepwdr", palebluePowderIcon);
        icons.put("bluepwdr", bluePowderIcon);
        icons.put("brownpwdr", brownPowderIcon);

        icons.put("goldbloom", goldBloomIcon);
        icons.put("wildrose", wildRoseIcon);
        icons.put("amanita", amanitaIcon);
        icons.put("purplehaze", purpleHazeIcon);
        icons.put("mandrake", mandrakeIcon);

        icons.put("bowl", bowlIcon);
        icons.put("heatedflask", heatedflaskIcon);
        icons.put("scissors", scissorsIcon);
        icons.put("candle", candleIcon);
        icons.put("dynamite", dynamiteIcon);
        icons.put("railgun", dynamitexlIcon);   //no icon for Railgun so use DynamiteXL Icon

        icons.put("recipe", recipeIcon);
        icons.put("book", bookIcon);
        icons.put("redbook", redBookIcon);
        icons.put("bluebook", blueBookIcon);

        icons.put("door", doorIcon);

        icons.put("apple", appleIcon);
        icons.put("bannana", bannanaIcon);

        icons.put("emptyjar", emptyJarIcon);
        icons.put("waterjar", waterJarIcon);

        icons.put("ironingot", ironingotIcon);
        icons.put("copperingot", copperingotIcon);
        icons.put("goldingot", goldingotIcon);
        icons.put("shadowingot", shadowingotIcon);

        icons.put("woodshaft", woodshaftIcon);

        //From old system, no specific icon:
        icons.put("blueprint", palebluePowderIcon);
        icons.put("debug", greenPowderIcon);
        
        icons.put("coal", coal);
        icons.put("stick", stick);
        icons.put("refinementrock", refinementrock);
    }

    /**
     * Draw the icon.
     */
    public void render() {
        if (_blockFamily == null) {
            _element.renderTransformed();
        } else {
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            GL11.glPushMatrix();
            glTranslatef(4f, 0f, 0f);
            GL11.glScalef(20f, 20f, 20f);
            GL11.glRotatef(170f, 1f, 0f, 0f);
            GL11.glRotatef(-16f, 0f, 1f, 0f);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

            Block block = _blockFamily.getArchetypeBlock();
            block.renderWithLightValue(1.0f);

            GL11.glPopMatrix();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
    }

    /**
     * @return x-offset in icon sheet
     */
    public int getX() {
        return _x;
    }

    /**
     * @return y-offset in icon sheet
     */
    public int getY() {
        return _y;
    }
    
    public String getTextureSimpleUri(){
    	return _element.getTexture().getURI().getSimpleString();
    }

    private void setAtlasPosition(int x, int y) {
        _x = x;
        _y = y;

        if (_element == null) {
            return;
        }

        _element.setTextureOrigin(new Vector2f(x * 16, y * 16));
    }
}

