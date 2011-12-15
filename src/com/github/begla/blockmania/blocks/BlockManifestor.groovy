package com.github.begla.blockmania.blocks

/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Graphics
import org.newdawn.slick.util.ResourceLoader
import com.github.begla.blockmania.game.Blockmania
import javax.vecmath.Vector2f;

/**
 * This Groovy class is responsible for keeping the Block Manifest in sync between
 * a set of block definitions and a saved world in Serialized state
 * It is only used on game-startup, leaving on-going activity to BlockManager et al
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class BlockManifestor {

    /** Holds BufferedImages during the loading process */
    private Map<String,BufferedImage> _images = [:]

    /** Holds image index values during the loading process. These values are persisted in the Manifest */
    protected static Map<String,Integer> _imageIndex = [:]

    /** The path this Manifestor loads from */
    protected getPath() {
        return "com/github/begla/blockmania/data/blocks"
    }

    /**
     * On game startup we need to load Block configuration regardless. Block IDs depend on existing or new world
     * Later on this class could also review an existing world's version level and make any needed upgrades
     * (the "version" prop is in all the files as an example, but a complete system would take some work)
     */
    public loadConfig() {

        // See if we have an existing block manifest in a saved world
        if (false) {
            // Load values for _imageIndex from the Manifest
            // Load stored BlockID byte values from the Manifest
        } else {
            // If we don't have a saved world we'll need to load raw block textures
            _images = getInternalImages("com/github/begla/blockmania/data/textures/blocks")

            //println "Loaded fresh images - here's some logging!"
            _images.eachWithIndex { key, value, index ->
                //println "Image " + index + " is for " + key + " and looks like: " + value
                _imageIndex.put(key, index)
            }

            println "The image index now looks like this: " + _imageIndex
        }

        // We always load the block definitions, the manifest IDs just may already exist if using a saved world
        loadBlockDefinitions()

        // Load block definitions from Block sub-classes
        new PlantBlockManifestor().loadBlockDefinitions()
        // Trees
        // Liquids

        // We do the same check once again - this time to see if we need to write the first-time block manifest
        if (true) {
            // Saving a manifest includes splicing all available Block textures together into a new images
            saveManifest()
        }

        // Hacky hacky hack hack!
        System.exit(0)
    }

    /**
     * Loads block definitions from available internal Groovy classes and external addon Groovy scripts
     * Populates the stuff that groovy/blocks/Default.groovy used to load, with dynamic IDs
     * Is also used by sub-classes where BLOCK_PATH must be separately defined along with instantiateBlock
     */
    public loadBlockDefinitions() {
        // First identify what plain Block definitions we've got at the appropriate path and loop over what we get
        getClassesAt(getPath()).each { c ->
            //println ("Got back the following class: " + c)

            // Prepare to load properties from the Groovy definition via ConfigSlurper
            ConfigObject blockConfig = new ConfigSlurper().parse((Class) c)
            blockConfig.put("name", c.getSimpleName())
            println "Loaded block config for Class " + c + ": " + blockConfig

            // Prepare a Block from the stuff we load from the Groovy definition
            Block b = instantiateBlock(blockConfig)

            // Optionally use the Class object we loaded to execute any custom Groovy scripting (rare?)
            // An example would be if the Block wants to be registered as a specific type (dirt, plant, mineral..)

            // Add the finished Block to BlockManager (mockups below)
            // BlockManager.addBlock(b) // This adds the instantiated class itself with all values set for game usage
            // if (!BlockManager.hasManifested(b)) {    // Check if we already loaded a manifest ID for the Block
                // BlockManager.addBlockManifest(b, BlockManager.nextID)    // If not then create an ID for it
        }
    }

    /**
     * Helper method that takes a path to a resource directory and figures out the Block (or child) classes there
     * This relies on the directory only containing Block-derived classes, closure stubs, and sub dirs
     * TODO: Need a separate loader for external addon blocks - it needs override priority for user content
     *      The add-on loader will be grabbing stuff out of plain text Groovy scripts with textures in the same dir
     * @param path  target path to load stuff from
     * @return      instanced Groovy classes
     */
    protected getClassesAt(String path) {
        def allClasses = []

        URL u = getClass().getClassLoader().getResource(path);
        path = path.replace('/', '.')
        println "*** Going to get Blocks from classpath: " + path

        new File(u.toURI()).list().each { i ->
            //println "Checking filename/dir: " + i
            // Ignore directories and compiled inner classes (closures)
            if (!i.contains('$') && i.endsWith(".class")) {
                def className = i[0..-7]
                println ("Useful class: " + className)
                allClasses << getClass().getClassLoader().loadClass(path + "." + className)
            }
        }
        return allClasses
    }

    /**
     * The Block class this Manifestor loads & prepares (so BlockManifestor.loadBlockDefinitions can be generic)
     * @return the instantiated and prepared class
     */
    private instantiateBlock(ConfigObject blockConfig) {
        // Construct the class - this loads the Block-level defaults
        Block b = new Block()

        // Now apply Block-level details from Groovy (which may overwrite constructor defaults)
        prepareBlock(b, blockConfig)

        // Return the prepared class
        return b
    }

    /**
     * This method prepares an instantiated Block (or child class) with values loaded from its Groovy definition
     * This allows sub-type Manifestors to call super() to fill values relevant to whatever is one level up
     * @param b             The Block we're preparing with values loaded at this level
     * @param c             ConfigObject holding the ConfigSlurper-produced props from the Groovy definition
     * @return              The finished Block object we'll store in the BlockManager (returned via reference)
     */
    protected prepareBlock(Block b, ConfigObject c) {
        // Load Block details from Groovy, which may overwrite defaults from Block's Constructor
        println "Preparing block with name " + c.name

        // Faces - note how these are _not_ persisted in the Manifest, instead the texture name index values are
        // In theory this allows Blocks to change their faces without impacting the saved state of a world
        // First just set all 6 faces to the default for that block (its name for a png file)
        // This can return null if there's no default texture for a block, is ok if everything is set below
        // TODO: Might want to add some validation that all six sides have valid assignments at the end?
        //println "Default image returns: " + _imageIndex.get(c.name)
        b.withTextureAtlasPos(new Vector2f(_imageIndex.get(c.name), 0))

        // Then look for each more specific assignment and overwrite defaults where needed
        if (c.block.faces.sides != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.sides + " for sides"
            b.withTextureAtlasPosMantle(new Vector2f(_imageIndex.get(c.block.faces.sides), 0))
        }
        if (c.block.faces.topbottom != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.topbottom + " for topbottom"
            b.withTextureAtlasPosTopBottom(new Vector2f(_imageIndex.get(c.block.faces.topbottom), 0))
        }
        // Top, Bottom, Left, Right, Front, Back - probably a way to do that in a loop...
        if (c.block.faces.top != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.top + " for top"
            b.withTextureAtlasPos(Block.SIDE.TOP, new Vector2f(_imageIndex.get(c.block.faces.top), 0))
        }
        if (c.block.faces.bottom != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.bottom + " for bottom"
            b.withTextureAtlasPos(Block.SIDE.BOTTOM, new Vector2f(_imageIndex.get(c.block.faces.bottom), 0))
        }
        if (c.block.faces.left != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.left + " for left"
            b.withTextureAtlasPos(Block.SIDE.LEFT, new Vector2f(_imageIndex.get(c.block.faces.left), 0))
        }
        if (c.block.faces.right != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.right + " for right"
            b.withTextureAtlasPos(Block.SIDE.RIGHT, new Vector2f(_imageIndex.get(c.block.faces.right), 0))
        }
        if (c.block.faces.front != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.front + " for front"
            b.withTextureAtlasPos(Block.SIDE.FRONT, new Vector2f(_imageIndex.get(c.block.faces.front), 0))
        }
        if (c.block.faces.back != [:]) {
            //println "Setting Block " + c.name + " to " + c.block.faces.back + " for back"
            b.withTextureAtlasPos(Block.SIDE.BACK, new Vector2f(_imageIndex.get(c.block.faces.back), 0))
        }

        println "Faces are (L, R, T, B, F, B): " + b.getTextureAtlasPos()
    }

    /**
     * Looks for Block image files recursively starting from a given path and adds them to a map
     * @param path  the path to start looking from
     * @return      a map containing loaded BufferedImages tied to their filename minus .png
     */
    private getInternalImages(String path) {
        def images = [:]

        URL u = getClass().getClassLoader().getResource(path);
        println "*** Going to get Images from classpath: " + path

        new File(u.toURI()).list().each { i ->
            println "Checking filename/dir: " + i
            // Expecting either png images or subdirs with more png images (and potentially more subdirs)
            // TODO: We might need some error handling here (hopefully solid convention is enough)
            if (i.endsWith(".png")) {
                println "Useful image: " + i
                // Load a BufferedImage and put it in the map tied to its name short the ".png"
                images.put(i[0..-5], ImageIO.read(ResourceLoader.getResource(path + "/" + i).openStream()))
            }
            else {
                // Recursively go through subdirs and add all we find there
                images.putAll(getInternalImages(path + "/" + i))
            }
        }
        // Return the final map
        return images
    }

    /**
     *  After all the block data and such has been loaded into class objects and the assorted Collections,
     *  we save the manifest needed to reload the world in a file in the save dir
     *  We also dynamically create a terrain.png to go with the manifest out of all the block textures in order
     */
    public saveManifest() {

        // The manifest can be written out purely using plain Block classes, ignoring any potential subtypes
        // All the manifest and terrain.png needs to know is the ID and textures from faces (a Block feature)

        /*String s = Blockmania.getInstance().getActiveWorldProvider().getWorldSavePath() + "/BlockManifest.groovy"
        println "Manifest string is " + s
        File f = new File(s)
        f.withWriter { writer ->
            // loop through everything, look up block images and build terrain.png, save config stuff
        }*/
        // Save the final terrain.png

        // Loop through loaded classes and figure out width + height (or just use width only)

        int width = _images.size() * 16
        println "We've got " + _images.size() + " images total, so the width of the final image will be " + width
        int x = 0

        BufferedImage result = new BufferedImage(width, 16, BufferedImage.TYPE_INT_RGB)
        Graphics g = result.getGraphics()

        _images.each {
            g.drawImage(it.value, x, 0, null)
            x += 16
        }

        //String pngSave = Blockmania.getInstance().getActiveWorldProvider().getWorldSavePath() + "/terrain.png"
        File pngSave = new File("terrain.png")
        println "Saving merged Block texture file to " + pngSave.absolutePath
        ImageIO.write(result,"png",pngSave)

    }
    
}
