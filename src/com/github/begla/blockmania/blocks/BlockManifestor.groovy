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

//import ...

/**
 * This Groovy class is responsible for keeping the Block Manifest in sync between
 * a set of block definitions and a saved world in Serialized state
 * It is only used on game-startup, leaving on-going activity to BlockManager et al
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class BlockManifestor {

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
            // Load Block manifest IDs from the existing block manifest file
        }

        // We always load the block definitions, the manifest IDs just may already exist if using a saved world
        loadBlockDefinitions()

        // Load block definitions from Block sub-classes
        new PlantBlockManifestor().loadBlockDefinitions()
        // Trees
        // Liquids

        // We do the same check once again - this time to see if we need to write the first-time block manifest
        if (false) {
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
     * @param blockConfig   The ConfigSlurper-produced props from the Groovy definition
     * @return              The finished Block object we'll store in the BlockManager (returned via reference)
     */
    protected prepareBlock(Block b, ConfigObject blockConfig) {
        // Load Block details from Groovy, which may overwrite defaults from Block's Constructor

    }

    /**
     *  After all the block data and such has been loaded into class objects and the assorted Collections,
     *  we save the manifest needed to reload the world in a file in the save dir
     *  We also dynamically create a terrain.png to go with the manifest out of all the block textures in order
     */
    public saveManifest() {
        println "saveManifest says hi"

        // The manifest can be written out purely using plain Block classes, ignoring any potential subtypes
        // All the manifest and terrain.png needs to know is the ID and textures from faces (a Block feature)

        /*String s = Blockmania.getInstance().getActiveWorldProvider().getWorldSavePath() + "/BlockManifest.groovy"
        println "Manifest string is " + s
        File f = new File(s)
        f.withWriter { writer ->
            // loop through everything, look up block images and build terrain.png, save config stuff
        }*/
        // Save the final terrain.png
    }
    
}
