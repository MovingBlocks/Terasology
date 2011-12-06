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
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
class BlockManifestor {

    // This may or may not make sne
    //def _config

    public BlockManifestor() {

    }

    /**
     * On world startup this should be called to either load an existing config file or generate a new one
     * Later on this class could also review an existing world's version level and make any needed upgrades
     * (the "version" prop is in all the files as an example, but a complete system would take some work)
     */
    public loadConfig() {
        println "Yay!"
        // IF a manifest exists, load it, else call generate. Can't load just yet, need to save something first!
        if (false) {
            // Load
        } else {
            generateManifest()
        }

        // Hacky hacky hack hack!
        System.exit(0)
    }

    /**
     * Generates a brand-new block manifest from available internal Groovy classes and external addon Groovy scripts
     * Populates the stuff that groovy/blocks/Default.groovy used to load, with dynamic IDs
     */
    public generateManifest() {
        // this is specific to vanilla blocks, no subtypes here
        getClassesAt("com/github/begla/blockmania/data/blocks/plant").each { c ->
            //println ("Got back the following class: " + c)
            def blockConfig = new ConfigSlurper().parse((Class) c)
            println "Loaded block config for Class " + c + ": " + blockConfig
            // Create a Block class here and call another utility method to overwrite any defaults specified
            // Put the final class and any related info (ID look-ups) in the right spots in BlockManager
        }

        // Do the same thing here for Block subtypes - Plants, Trees, Liquids, etc...

        // saveManifest
    }

    /**
     * Helper method that takes a path to a resource directory and figures out the Block (or child) classes there
     * This relies on the directory only containing Block-derived classes, closure stubs, and sub dirs
     * TODO: Need a separate loader for external addon blocks - it needs override priority for user content
     *      The add-on loader will be grabbing stuff out of plain text Groovy scripts with textures in the same dir
     * @param path  target path to load stuff from
     * @return      instanced Groovy classes
     */
    private getClassesAt(String path) {
        def allClasses = []

        URL u = getClass().getClassLoader().getResource(path);
        path = path.replace('/', '.')
        println "Going to use class base path: " + path

        new File(u.toURI()).list().each { i ->
            println "Checking filename/dir: " + i
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
     *  After all the block data and such has been loaded into class objects and the assorted Collections,
     *  we save the manifest needed to reload the world in a file in the save dir
     *  We also dynamically create a terrain.png to go with the manifest out of all the block textures in order
     */
    public saveManifest() {
        /*String s = Blockmania.getInstance().getActiveWorldProvider().getWorldSavePath() + "/BlockManifest.groovy"
        println "Manifest string is " + s
        File f = new File(s)
        f.withWriter { writer ->
            // loop through everything, look up block images and build terrain.png, save config stuff
        }*/
        // Save the final terrain.png
    }
    
}
