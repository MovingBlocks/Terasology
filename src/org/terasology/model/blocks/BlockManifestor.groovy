package org.terasology.model.blocks

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

import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.imageio.ImageIO
import javax.vecmath.Vector2f
import javax.vecmath.Vector4f
import org.newdawn.slick.util.ResourceLoader
import org.terasology.logic.manager.TextureManager
import org.terasology.utilities.ClasspathResourceLoader
import groovy.util.logging.Log

/**
 * This Groovy class is responsible for keeping the Block Manifest in sync between
 * a set of block definitions and a saved world in Serialized state
 * It is only used on game-startup, leaving on-going activity to BlockManager et al
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@Log
class BlockManifestor {

    private static BlockManager _bm;
    protected ClasspathResourceLoader _resourceLoader;

    // TODO: Usage of this is fairly brute force, maybe there's a more efficient way, with sorting or so?

    /** Holds BufferedImages during the loading process (not persisted) */
    private Map<String, BufferedImage> _images = [:]

    /** Holds image index values during the loading process. These values are persisted in the Manifest */
    protected static Map<String, Integer> _imageIndex = [:]

    /** Holds Block ID index values during the loading process - also persisted in the Manifest */
    protected static Map<Byte, Block> _blockIndex = [:]

    /** Smaller version of _blockIndex only used for loading IDs */
    protected static Map<String, Byte> _blockStringIndex = [:]

    /** Holds the Byte value for the next Block ID - starts at 1 since Air is always 0 */
    protected static byte _nextByte = (byte) 1

    /** Temp Manifest references - need to tie these to each saved world instead (or fail to find any, then create) */
    File _blockManifest = new File('SAVED_WORLDS/BlockManifest.groovy')
    File _imageManifest = new File('SAVED_WORLDS/ImageManifest.png')
    File _imageManifestMipMap1 = new File('SAVED_WORLDS/ImageManifest1.png')
    File _imageManifestMipMap2 = new File('SAVED_WORLDS/ImageManifest2.png')
    File _imageManifestMipMap3 = new File('SAVED_WORLDS/ImageManifest3.png')

    // Empty default constructor for child classes
    public BlockManifestor() {}

    public BlockManifestor(BlockManager bm) {
        _bm = bm
        fixSavePaths()
    }
    
    // Temp helper methods until we can correctly use WorldProvider.getWorldSavePath - tries to detect and fix screwy applet paths
    protected fixSavePaths() {
        _blockManifest = fixSavePath(_blockManifest)
        _imageManifest = fixSavePath(_imageManifest)
        _imageManifestMipMap1 = fixSavePath(_imageManifestMipMap1)
        _imageManifestMipMap2 = fixSavePath(_imageManifestMipMap2)
        _imageManifestMipMap3 = fixSavePath(_imageManifestMipMap3)
    }

    private File fixSavePath(File f) {
        log.info "Suggested absolute save path is: " + f.getAbsolutePath()
        if (!f.getAbsolutePath().contains("Terasology")) {
            f = new File(System.getProperty("java.io.tmpdir"), f.path)
            log.info "Going to use absolute TEMP save path instead: " + f.getAbsolutePath()

            return f
        }
        return f
    }

    /**
     * On game startup we need to load Block configuration. Exact Block IDs depend on existing or new world
     * Later on this class could also review an existing world's version level and make any needed upgrades
     * (the "version" prop is in all the files as an example, but a complete system would take some work)
     */
    public loadConfig() throws Exception {
        // First of all we need to know whether we're running from inside a jar or not - this will store a local ref if so
        // The path used here can be tricky as it may catch something unexpected if too vague (like a lib jar with a matching fragment)
        _resourceLoader = new ClasspathResourceLoader("org/terasology/data/blocks")

        boolean worldExists = _blockManifest.exists() && _imageManifest.exists()

        // Check if we've got a manifest - later this would base on / trigger when user selects world load / create (GUI)
        if (worldExists) {
            loadManifest()
            // Load the ImageManifest into TextureManager as the "terrain" reference of old
            // TODO: Could actually both load existing textures and added ones and update the manifest files fairly easily
        } else {
            // If we don't have a saved world we'll need to build a new ImageManifest from raw block textures
            String path = "images"
            println "*** Going to scan for images from classpath: " + _resourceLoader.getPackagePath() + '/' + path
            _images = _resourceLoader.getImages(path)

            println "Loaded fresh images - here's some logging!"
            _images.eachWithIndex { key, value, index ->
                println "Image " + index + " is for " + key + " and looks like: " + value
                _imageIndex.put(key, index)
            }

            println "The image index (ImageManifest not yet saved) now looks like this: " + _imageIndex
        }

        // We always load the block definitions, the block IDs just may already exist in the manifest if using a saved world
        loadBlockDefinitions("definitions")

        // Load block definitions from Block sub-classes
        new PlantBlockManifestor(_resourceLoader).loadBlockDefinitions("definitions/plant")
        new TreeBlockManifestor(_resourceLoader).loadBlockDefinitions("definitions/plant/tree")
        new LiquidBlockManifestor(_resourceLoader).loadBlockDefinitions("definitions/liquid")

        // We can also re-use manifestors for sub dirs if we just put stuff there for a "human-friendly" grouping
        loadBlockDefinitions("definitions/furniture")
        loadBlockDefinitions("definitions/mineral")
        new PlantBlockManifestor(_resourceLoader).loadBlockDefinitions("definitions/plant/leaf")

        // _nextByte may not make sense if we're loading a world - until it is possible to upgrade / add stuff anyway
        println "Done loading blocks - _nextByte made it to " + _nextByte
        println "Final map that'll be passed to BlockManager is: " + _blockIndex

        // We do the same check once again - this time to see if we need to write the first-time manifest
        if (!worldExists) {
            // Saving a manifest includes splicing all available Block textures together into a new images
            saveManifest()
        }

        _bm.addAllBlocks(_blockIndex)
        log.info "_imageManifest file: " + _imageManifest.getAbsolutePath()
        TextureManager.getInstance().addTexture("terrain", _imageManifest.getAbsolutePath(), [_imageManifestMipMap1.getAbsolutePath(), _imageManifestMipMap2.getAbsolutePath(), _imageManifestMipMap3.getAbsolutePath()].toArray(new String[0]))
        // Hacky hacky hack hack!
        //System.exit(0)
    }

    /**
     * Loads block definitions from available internal Groovy classes and external addon Groovy scripts
     * Populates the stuff that groovy/blocks/Default.groovy used to load, with dynamic IDs
     * Is also used by sub-classes where BLOCK_PATH must be separately defined along with instantiateBlock
     */
    public loadBlockDefinitions(String path) {
        // First identify what plain Block definitions we've got at the appropriate path and loop over what we get
        _resourceLoader.getClassesAt(path).each { c ->
            println("Got back the following class: " + c)

            // Prepare to load properties from the Groovy definition via ConfigSlurper
            ConfigObject blockConfig = new ConfigSlurper().parse((Class) c)
            blockConfig.put("name", c.getSimpleName())
            println "Loaded block config for Class " + c + ": " + blockConfig

            // Prepare a Block from the stuff we load from the Groovy definition
            Block b = instantiateBlock(blockConfig)

            // Optionally use the Class object we loaded to execute any custom Groovy scripting (rare?)
            // An example would be if the Block wants to be registered as a specific type (dirt, plant, mineral..)

            // Make up or load a dynamic ID and add the finished Block to our local block index (prep for BlockManager)
            b.withTitle(c.getSimpleName())
            // See if have an ID for this block already (we should if we loaded an existing manifest)
            if (_blockStringIndex.containsKey(b.getTitle())) {
                println "Found an existing block ID value, assigning it to Block " + b.getTitle()
                b.withId((byte) _blockStringIndex.get(b.getTitle()))
            } else {
                // We have a single special case - the Air block (aka "empty") is ALWAYS id 0
                if (b.getTitle() == "Air") {
                    println "Hit the Air block - assigning this one the magic zero value a.k.a. 'empty'"
                    b.withId((byte) 0)
                    _blockStringIndex.put(b.getTitle(), (byte) 0)
                } else {
                    println "We don't have an existing ID for " + b.getTitle() + " so assigning _nextByte " + _nextByte
                    b.withId(_nextByte)
                    _blockStringIndex.put(b.getTitle(), _nextByte)
                    _nextByte++
                }
            }

            _blockIndex.put(b.getId(), b)

            // BlockManager.addBlock(b) // This adds the instantiated class itself with all values set for game usage
            // if (!BlockManager.hasManifested(b)) {    // Check if we already loaded a manifest ID for the Block
            // BlockManager.addBlockManifest(b, BlockManager.nextID)    // If not then create an ID for it
        }
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
     * @param b The Block we're preparing with values loaded at this level
     * @param c ConfigObject holding the ConfigSlurper-produced props from the Groovy definition
     * @return The finished Block object we'll store in the BlockManager (returned via reference)
     */
    protected prepareBlock(Block b, ConfigObject c) {
        // Load Block details from Groovy, which may overwrite defaults from Block's Constructor
        println "Preparing block with name " + c.name

        // *** FACES - note how these are _not_ persisted in the Manifest, instead the texture name index values are
        // In theory this allows Blocks to change their faces without impacting the saved state of a world
        // First just set all 6 faces to the default for that block (its name for a png file)
        // This can return null if there's no default texture for a block, is ok if everything is set below
        // TODO: Might want to add some validation that all six sides have valid assignments at the end? Air gets all 0?
        println "Default image returns: " + _imageIndex.get(c.name)

        def textureId = _imageIndex.get(c.name)

        if (textureId != null)
            b.withTextureAtlasPos(calcAtlasPositionForId(textureId))

        // Then look for each more specific assignment and overwrite defaults where needed
        if (c.block.faces.all != [:]) {
            println "Setting Block " + c.name + " to texture " + c.block.faces.all + " for all"
            b.withTextureAtlasPos(calcAtlasPositionForId(_imageIndex.get(c.block.faces.all)))
        }
        if (c.block.faces.sides != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.sides + " for sides"
            b.withTextureAtlasPosMantle(calcAtlasPositionForId(_imageIndex.get(c.block.faces.sides)))
        }
        if (c.block.faces.topbottom != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.topbottom + " for topbottom"
            b.withTextureAtlasPosTopBottom(calcAtlasPositionForId(_imageIndex.get(c.block.faces.topbottom)))
        }
        // Top, Bottom, Left, Right, Front, Back - probably a way to do that in a loop...
        if (c.block.faces.top != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.top + " for top"
            b.withTextureAtlasPos(Block.SIDE.TOP, calcAtlasPositionForId(_imageIndex.get(c.block.faces.top)))
        }
        if (c.block.faces.bottom != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.bottom + " for bottom"
            b.withTextureAtlasPos(Block.SIDE.BOTTOM, calcAtlasPositionForId(_imageIndex.get(c.block.faces.bottom)))
        }
        if (c.block.faces.left != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.left + " for left"
            b.withTextureAtlasPos(Block.SIDE.LEFT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.left)))
        }
        if (c.block.faces.right != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.right + " for right"
            b.withTextureAtlasPos(Block.SIDE.RIGHT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.right)))
        }
        if (c.block.faces.front != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.front + " for front"
            b.withTextureAtlasPos(Block.SIDE.FRONT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.front)))
        }
        if (c.block.faces.back != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.back + " for back"
            b.withTextureAtlasPos(Block.SIDE.BACK, calcAtlasPositionForId(_imageIndex.get(c.block.faces.back)))
        }
        println "Faces are (L, R, T, B, F, B): " + b.getTextureAtlasPos()

        // *** BLOCK_FORM and COLOR_SOURCE enums (defined explicitly in block definition, not needed here)
        if (c.block.blockform != [:]) {
            println "Setting BLOCK_FORM enum to: " + c.block.blockform
            b.withBlockForm(c.block.blockform)
        }
        if (c.block.colorsource != [:]) {
            println "Setting COLOR_SOURCE enum to: " + c.block.colorsource
            b.withColorSource(c.block.colorsource)
        }
        println "Block has form " + b.getBlockForm() + ", and color source " + b.getColorSource()

        // *** BOOLEANS - IntelliJ may warn about "null" about here but it works alright
        // Casting to (boolean) removes the warning but is functionally unnecessary
        if (c.block.translucent != [:]) {
            println "Setting translucent boolean to: " + c.block.translucent
            b.withTranslucent((boolean) c.block.translucent)
        }
        if (c.block.invisible != [:]) {
            println "Setting invisible boolean to: " + c.block.invisible
            b.withInvisible((boolean) c.block.invisible)
        }
        // TODO: Check what's up with waving/invisible together? Not fully updated println ?
        if (c.block.waving != [:]) {
            println "Setting invisible boolean to: " + c.block.invisible
            b.withWaving((boolean) c.block.waving)
        }
        if (c.block.penetrable != [:]) {
            println "Setting penetrable boolean to: " + c.block.penetrable
            b.withPenetrable((boolean) c.block.penetrable)
        }
        if (c.block.castsShadows != [:]) {
            println "Setting castsShadows boolean to: " + c.block.castsShadows
            b.withCastsShadows((boolean) c.block.castsShadows)
        }
        if (c.block.disableTessellation != [:]) {
            println "Setting disableTessellation boolean to: " + c.block.disableTessellation
            b.withDisableTessellation((boolean) c.block.disableTessellation)
        }
        if (c.block.renderBoundingBox != [:]) {
            println "Setting renderBoundingBox boolean to: " + c.block.renderBoundingBox
            b.withRenderBoundingBox((boolean) c.block.renderBoundingBox)
        }
        if (c.block.allowBlockAttachment != [:]) {
            println "Setting allowBlockAttachment boolean to: " + c.block.allowBlockAttachment
            b.withAllowBlockAttachment((boolean) c.block.allowBlockAttachment)
        }
        if (c.block.bypassSelectionRay != [:]) {
            println "Setting bypassSelectionRay boolean to: " + c.block.bypassSelectionRay
            b.withBypassSelectionRay((boolean) c.block.bypassSelectionRay)
        }
        //TODO: Move liquid to LiquidBlock rather than a Block boolean? Tho might be nice to have all basics in Block
        if (c.block.liquid != [:]) {
            println "Setting liquid boolean to: " + c.block.liquid
            b.withLiquid((boolean) c.block.liquid)
        }

        // *** MISC
        if (c.block.luminance != [:]) {
            println "Setting luminance to: " + c.block.luminance
            b.withLuminance((byte) c.block.luminance)
        }
        if (c.block.hardness != [:]) {
            println "Setting hardness to: " + c.block.hardness
            b.withHardness((byte) c.block.hardness)
        }

        // *** COLOR OFFSET (4 values) - this might need error handling
        if (c.block.colorOffset != [:]) {
            println "Setting colorOffset to: " + c.block.colorOffset + " (after making it a Vector4f)"
            b.withColorOffset(new Vector4f((float) c.block.colorOffset[0], (float) c.block.colorOffset[1], (float) c.block.colorOffset[2], (float) c.block.colorOffset[3]))
            println "The Vector4f instantiated is" + b.getColorOffset()
        }

    }

    public BufferedImage generateImage(int mipMapLevel) {
        int size = (int) (Block.ATLAS_SIZE_IN_PX / (2 ** mipMapLevel));
        int textureSize = (Block.TEXTURE_SIZE_IN_PX / (2 ** mipMapLevel));

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        Graphics g = result.getGraphics()

        int counter = 0
        _images.each {
            Vector2f position = calcAtlasPositionForId(counter);
            g.drawImage(it.value.getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), (int) position.x * textureSize, (int) position.y * textureSize, null)
            counter++
            // TODO: Throw an exception if the size of the atlas is exceeded
        }

        return result;
    }

    /**
     * For a new world we'll need to store block and image information in "Manifest" files to go with the world save
     * At some point maybe it will be possible to update a stored manifest as well (world update / new blocks)
     * If an existing world is being loaded instead we shouldn't need to save anything
     */
    public saveManifest() {

        //String s = Terasology.getInstance().getActiveWorldProvider().getWorldSavePath() + "/BlockManifest.groovy"

        // Later need to use Terasology.getInstance().getActiveWorldProvider().getWorldSavePath() or something
        println "Saving merged Block texture file to " + _imageManifest.absolutePath
        _imageManifest.mkdirs()
        ImageIO.write(generateImage(0), "png", _imageManifest)
        ImageIO.write(generateImage(1), "png", _imageManifestMipMap1)
        ImageIO.write(generateImage(2), "png", _imageManifestMipMap2)
        ImageIO.write(generateImage(3), "png", _imageManifestMipMap3)

        // Save the BlockManifest - again we use the spiffy power of ConfigObject / ConfigSlurper
        def manifest = new ConfigObject()
        manifest.blockIndex = _blockStringIndex
        manifest.imageIndex = _imageIndex
        manifest.nextByte = _nextByte

        println "Saving block IDs and image atlas (index) positions to " + _blockManifest.absolutePath
        _blockManifest.withWriter { writer ->
            writer << '// Warning: Editing this file may do crazy things to your saved world!\r\n'
            manifest.writeTo(writer)
        }
    }

    /**
     * Loads an existing World Manifest from a save directory into instance variables
     */
    private loadManifest() {
        def manifest = new ConfigSlurper().parse(_blockManifest.toURL())

        _imageIndex = manifest.imageIndex
        _blockStringIndex = manifest.blockIndex
        _nextByte = manifest.nextByte

        println "LOADED imageIndex: " + _imageIndex
        println "LOADED blockIndex: " + _blockStringIndex
        println "LOADED nextByte: " + _nextByte
    }


    private Vector2f calcAtlasPositionForId(int id) {
        return new Vector2f(((int) id % (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN), ((int) id / (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN))
    }
}
