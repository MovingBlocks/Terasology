package org.terasology.model.blocks.management

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

import groovy.util.logging.Log
import org.terasology.logic.manager.TextureManager
import org.terasology.math.Rotation
import org.terasology.math.Side
import org.terasology.model.shapes.BlockShape
import org.terasology.model.shapes.BlockShapeManager
import org.terasology.model.structures.AABB
import org.terasology.utilities.ClasspathResourceLoader
import org.terasology.utilities.Helper

import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.vecmath.Vector2f

import org.terasology.model.blocks.*

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

    /** Not persisted */
    Map<String, BufferedImage> _images = [:]

    /** Holds image index values during the loading process. These values are persisted in the Manifest */
    protected static Map<String, Integer> _imageIndex = [:]

    /** Holds Block ID index values during the loading process - also persisted in the Manifest */
    protected static Map<Byte, Block> _blockIndex = [:]

    /** Smaller version of _blockIndex only used for loading IDs */
    protected static Map<String, Byte> _blockStringIndex = [:]

    protected static List<BlockFamily> _blockFamilies = []

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
        _blockManifest = Helper.fixSavePath(_blockManifest)
        _imageManifest = Helper.fixSavePath(_imageManifest)
        _imageManifestMipMap1 = Helper.fixSavePath(_imageManifestMipMap1)
        _imageManifestMipMap2 = Helper.fixSavePath(_imageManifestMipMap2)
        _imageManifestMipMap3 = Helper.fixSavePath(_imageManifestMipMap3)
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
            log.fine "*** Going to scan for images from classpath: " + _resourceLoader.getPackagePath() + '/' + path
            _images = _resourceLoader.getImages(path)

            //println "Loaded fresh images - here's some logging!"
            _images.eachWithIndex { key, value, index ->
                //println "Image " + index + " is for " + key + " and looks like: " + value
                // TODO: Using Locale.ENGLISH _seems_ sensible given that the names should be consistent between different
                // users (is mostly used internally), but need to review
                _imageIndex.put(key.toLowerCase(Locale.ENGLISH), index)
            }

            log.info "The image index (ImageManifest not yet saved) now looks like this: " + _imageIndex
        }

        SimpleBlockLoader blockLoader = new SimpleBlockLoader(_imageIndex);

        // We always load the block definitions, the block IDs just may already exist in the manifest if using a saved world
        loadBlockDefinitions("definitions", blockLoader)

        // Load block definitions from Block sub-classes
        loadBlockDefinitions("definitions/plant", new PlantBlockLoader(_imageIndex))
        loadBlockDefinitions("definitions/plant/tree", new TreeBlockLoader(_imageIndex))
        loadBlockDefinitions("definitions/liquid", new LiquidBlockLoader(_imageIndex))

        // We can also re-use manifestors for sub dirs if we just put stuff there for a "human-friendly" grouping
        loadBlockDefinitions("definitions/furniture", blockLoader)
        loadBlockDefinitions("definitions/mineral", blockLoader)
        loadBlockDefinitions("definitions/soil", blockLoader)
        loadBlockDefinitions("definitions/plant/leaf", new PlantBlockLoader(_imageIndex))

        // _nextByte may not make sense if we're loading a world - until it is possible to upgrade / add stuff anyway
        //println "Done loading blocks - _nextByte made it to " + _nextByte
        //println "Final map that'll be passed to BlockManager is: " + _blockIndex

        // We do the same check once again - this time to see if we need to write the first-time manifest
        if (!worldExists) {
            // Saving a manifest includes splicing all available Block textures together into a new images
            saveManifest()
        }

        _bm.addAllBlocks(_blockIndex)
        _bm.addAllBlockFamilies(_blockFamilies);
        TextureManager.getInstance().addTexture("terrain", _imageManifest.getAbsolutePath(), [_imageManifestMipMap1.getAbsolutePath(), _imageManifestMipMap2.getAbsolutePath(), _imageManifestMipMap3.getAbsolutePath()].toArray(new String[0]))
    }

    /**
     * Loads block definitions from available internal Groovy classes and external addon Groovy scripts
     * Populates the stuff that groovy/blocks/Default.groovy used to load, with dynamic IDs
     * Is also used by sub-classes where BLOCK_PATH must be separately defined along with instantiateBlock
     */
    public loadBlockDefinitions(String path, BlockLoader loader) {
        // First identify what plain Block definitions we've got at the appropriate path and loop over what we get
        _resourceLoader.getClassesAt(path).each { c ->
            //println("Got back the following class: " + c)

            // Prepare to load properties from the Groovy definition via ConfigSlurper
            ConfigObject blockConfig = new ConfigSlurper().parse((Class) c)
            blockConfig.put("name", c.getSimpleName())
            //println "Loaded block config for Class " + c + ": " + blockConfig

            if (blockConfig.block.alignment instanceof String) {
                String alignment = blockConfig.block.alignment;
                if (alignment.equalsIgnoreCase("HorizontalDirection")) {
                    loadHorizontalBlock(blockConfig, loader)
                } else if (alignment.equalsIgnoreCase("SurfaceAligned")) {
                    loadSurfaceAligned(blockConfig, loader)
                } else {
                    loadSymmetricBlock(blockConfig, loader)
                }
            } else {
                loadSymmetricBlock(blockConfig, loader)
            }
        }
    }

    public loadSurfaceAligned(ConfigObject blockConfig, BlockLoader loader) {
        def loaded = false
        EnumMap<Side, Block> blockMap = new EnumMap<Side, Block>(Side.class)

        // TODO: Other sides
        // TODO: Top-Bottom flip
        if (blockConfig.block.bottom != [:]) {
            ConfigObject combined = new ConfigObject();
            combined.merge(blockConfig.block)
            combined.merge(blockConfig.block.bottom)

            Block b = loader.loadBlock(combined)
            BlockShape shape = loadBlockShape(combined);
            if (shape != null) {
                FacesInfo facesInfo = loadFaces(combined.faces, blockConfig.name)
                applyBlockShape(b, shape, facesInfo)
            }
            registerBlock(blockConfig.name + Side.BOTTOM, b)
            blockMap.put(Side.BOTTOM, b)
        }
        if (blockConfig.block.sides != [:]) {
            ConfigObject combined = new ConfigObject();
            combined.merge(blockConfig.block)
            combined.merge(blockConfig.block.sides)

            BlockShape shape = loadBlockShape(combined);
            if (shape != null) {
                FacesInfo facesInfo = loadFaces(combined.faces, blockConfig.name)
                for (Rotation rot : Rotation.horizontalRotations()) {
                    Block block = loader.loadBlock(combined, rot)
                    applyBlockShape(block, shape, facesInfo, rot)

                    registerBlock(blockConfig.name + rot.rotate(Side.FRONT), block)
                    blockMap.put(rot.rotate(Side.FRONT), block)
                }
            }
        }

        if (!loaded) {
            // TODO: default, load shape and rotate to all sides
        }

        _blockFamilies.add(new AlignToSurfaceFamily(blockConfig.name, blockMap))
    }

    public loadHorizontalBlock(ConfigObject blockConfig, BlockLoader loader) {
        BlockShape shape = loadBlockShape(blockConfig.block);

        EnumMap<Side, Block> blockMap = new EnumMap<Side, Block>(Side.class)
        
        FacesInfo facesInfo = loadFaces(blockConfig.block.faces, blockConfig.name)
        for (Rotation rot : Rotation.horizontalRotations()) {
            Block block = loader.loadBlock(blockConfig.block, rot)
            applyBlockShape(block, shape, facesInfo, rot)

            registerBlock(blockConfig.name + rot.rotate(Side.FRONT), block)
            blockMap.put(rot.rotate(Side.FRONT), block)
        }
        _blockFamilies.add(new HorizontalBlockFamily(blockConfig.name, blockMap))
    }

    public loadSymmetricBlock(ConfigObject blockConfig, BlockLoader loader) {
        Block b = loader.loadBlock(blockConfig.block);
        
        BlockShape shape = loadBlockShape(blockConfig.block);

        if (shape != null) {
            FacesInfo facesInfo = loadFaces(blockConfig.block.faces, blockConfig.name)
            applyBlockShape(b, shape, facesInfo)
            if (blockConfig.block.loweredShape != [:]) {
                applyLoweredShape(b, BlockShapeManager.getInstance().getBlockShape(blockConfig.block.loweredShape), facesInfo)
            }
        }

        registerBlock(blockConfig.name, b)
        _blockFamilies.add(new SymmetricFamily(b))
    }

    private BlockShape loadBlockShape(ConfigObject config) {
        // TODO: No singletons!
        if (config.shape instanceof String) {
            return BlockShapeManager.getInstance().getBlockShape(config.shape)
        } else if (!config.invisible) {
            return BlockShapeManager.getInstance().getBlockShape("cube")
        }
        return null;
    }

    private void applyLoweredShape(Block b, BlockShape shape, FacesInfo faces) {
        if (shape != null) {
            for (Side side : Side.values()) {
                if (shape.getSideMesh(side) != null) {
                    b.withLoweredSideMesh(side, shape.getSideMesh(side).mapTexCoords(calcTextureOffsetFor(faces.sides.get(side)), Block.TEXTURE_OFFSET_WIDTH))
                }
            }
        }
    }
    
    private void applyBlockShape(Block b, BlockShape shape, FacesInfo faces) {
        applyBlockShape(b, shape, faces, Rotation.None)
    }

    private void applyBlockShape(Block b, BlockShape shape, FacesInfo faces, Rotation rotation) {
        if (shape.getCenterMesh() != null) {
            b.withCenterMesh(shape.getCenterMesh().rotate(rotation.getQuat4f()).mapTexCoords(calcTextureOffsetFor(faces.center), Block.TEXTURE_OFFSET_WIDTH))
        }
        
        for (Side side : Side.values()) {
            Side targetSide = rotation.rotate(side);
            if (shape.getSideMesh(side) != null) {
                b.withSideMesh(targetSide, shape.getSideMesh(side).rotate(rotation.getQuat4f()).mapTexCoords(calcTextureOffsetFor(faces.sides.get(side)), Block.TEXTURE_OFFSET_WIDTH))
            }
            b.withFullSide(targetSide, shape.isBlockingSide(side));
            b.withTextureAtlasPos(targetSide, faces.sides.get(side))
        }
        
        List<AABB> colliders = []
        for (AABB col : shape.colliders) {
            colliders.add(rotation.rotate(col));
        }
        b.setColliders(colliders);
        
    }

    private Vector2f calcTextureOffsetFor(Vector2f face) {
        return new Vector2f((float) face.x * Block.TEXTURE_OFFSET, (float) face.y * Block.TEXTURE_OFFSET);
    }

    private FacesInfo loadFaces(ConfigObject faces, String defaultTextureName) {
        FacesInfo result = new FacesInfo();
        
        def textureId = getImageIdUnchecked(defaultTextureName)
        if (faces.all != [:]) {
            textureId = getImageId(faces.all);
        }
        if (textureId == null && faces.center != [:]) {
            textureId = getImageId(faces.center);
        }
        if (textureId != null) {
            Vector2f texPos = calcAtlasPositionForId(textureId);
            result.center = texPos;
            for (Side side : Side.values()) {
                result.sides.put(side, texPos);
            }
        } else {
            for (Side side : Side.values()) {
                result.sides.put(side, calcAtlasPositionForId(0));
            }
        }

        if (faces.center != [:]) {
            result.center = calcAtlasPositionForId(getImageId(faces.center))
        }
        if (faces.sides != [:]) {
            Vector2f texPos = calcAtlasPositionForId(getImageId(faces.sides))
            for (Side side : Side.horizontalSides()) {
                result.sides.put(side, texPos);
            }
        }
        if (faces.topbottom != [:]) {
            Vector2f texPos = calcAtlasPositionForId(getImageId(faces.topbottom))
            result.sides.put(Side.TOP, texPos);
            result.sides.put(Side.BOTTOM, texPos);
        }
        // Top, Bottom, Left, Right, Front, Back - probably a way to do that in a loop...
        if (faces.top != [:]) {
            result.sides.put(Side.TOP, calcAtlasPositionForId(getImageId(faces.top)))
        }
        if (faces.bottom != [:]) {
            result.sides.put(Side.BOTTOM, calcAtlasPositionForId(getImageId(faces.bottom)))
        }
        if (faces.left != [:]) {
            result.sides.put(Side.LEFT, calcAtlasPositionForId(getImageId(faces.left)))
        }
        if (faces.right != [:]) {
            result.sides.put(Side.RIGHT, calcAtlasPositionForId(getImageId(faces.right)))
        }
        if (faces.front != [:]) {
            result.sides.put(Side.FRONT, calcAtlasPositionForId(getImageId(faces.front)))
        }
        if (faces.back != [:]) {
            result.sides.put(Side.BACK, calcAtlasPositionForId(getImageId(faces.back)))
        }
        return result;
    }

    
    private static class FacesInfo {
        public EnumMap<Side,Vector2f> sides = new EnumMap<Side, Vector2f>(Side.class);
        public Vector2f center;
    }


    private void registerBlock(String title, Block b) {
        b.withTitle(title);

        // Make up or load a dynamic ID and add the finished Block to our local block index (prep for BlockManager)
        // See if have an ID for this block already (we should if we loaded an existing manifest)
        if (_blockStringIndex.containsKey(b.getTitle())) {
            //log.info "Found an existing block ID value, assigning it to Block " + b.getTitle()
            b.withId((byte) _blockStringIndex.get(b.getTitle()))
        } else {
            // We have a single special case - the Air block (aka "empty") is ALWAYS id 0
            if (b.getTitle() == "Air") {
                //log.info "Hit the Air block - assigning this one the magic zero value a.k.a. 'empty'"
                b.withId((byte) 0)
                _blockStringIndex.put(b.getTitle(), (byte) 0)
            } else {
                //log.info "We don't have an existing ID for " + b.getTitle() + " so assigning _nextByte " + _nextByte
                b.withId(_nextByte)
                _blockStringIndex.put(b.getTitle(), _nextByte)
                _nextByte++
            }
        }

        _blockIndex.put(b.getId(), b)
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
        log.info "Saving merged Block texture file to " + _imageManifest.absolutePath
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

        log.info "Saving block IDs and image atlas (index) positions to " + _blockManifest.absolutePath
        _blockManifest.withWriter { writer ->
            writer << '// Warning: Editing this file may do crazy things to your saved world!\r\n'
            manifest.writeTo(writer)
        }
        log.info "The block index now looks like this: " + _blockIndex
    }

    /**
     * Loads an existing World Manifest from a save directory into instance variables
     */
    private loadManifest() {
        def manifest = new ConfigSlurper().parse(_blockManifest.toURL())

        Map<String, Integer> tempImageIndex = manifest.imageIndex

        _imageIndex = new HashMap<String, Integer>();
        tempImageIndex.each {key, value -> _imageIndex.put(key.toLowerCase(Locale.ENGLISH), value)}
        _blockStringIndex = manifest.blockIndex
        _nextByte = manifest.nextByte

        log.info "LOADED imageIndex from " + _imageManifest.getAbsolutePath() + " is: " + _imageIndex
        log.info "LOADED blockIndex from " + _blockManifest.getAbsolutePath() + " is: " + _blockStringIndex
        log.info "LOADED nextByte: " + _nextByte
    }

    // TODO: Utility method?
    private Vector2f calcAtlasPositionForId(int id) {
        return new Vector2f(((int) id % (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN), ((int) id / (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN))
    }
    
    private Integer getImageIdUnchecked(String name) {
        return _imageIndex.get(name.toLowerCase(Locale.ENGLISH))
    }

    private int getImageId(String name) {
        String lcName = name.toLowerCase(Locale.ENGLISH);
        if (_imageIndex.containsKey(lcName)) {
            return _imageIndex.get(lcName)
        }
        log.severe "Unknown texture: " + name;
        return 0;
    }

}