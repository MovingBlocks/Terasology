package org.terasology.utilities

import groovy.util.logging.Log
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.imageio.ImageIO
import org.newdawn.slick.util.ResourceLoader

/**
 * Helper class for accessing resources from the classpath, whether it is
 * in a Jar file or exploded folders.
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@Log
public class ClasspathResourceLoader {
    // TODO: Usage of this is fairly brute force, maybe there's a more efficient way, with sorting or so? Probably doesn't matter much tho
    private JarFile _jar
    private String _rootPath

    /**
     * @param packagePath The package to load resources from. This should be specific enough that it won't match package
     * fragments from other jars.
     */
    public ClasspathResourceLoader(String packagePath) {
        _rootPath = packagePath
        scanForJar(packagePath)
    }

    /**
     * This method figures out whether we're running from inside a jar file, in case we need to load stuff differently
     * If we are then set an instance variable JarFile we can keep handy for later loading from
     *
     * @param path any path to something that exists inside the jar file (better be unique!)
     */
    private scanForJar(String path) {
        URL u = getClass().getClassLoader().getResource(path)
        log.fine("URL made from our dummy jar path is: " + u)
        if (u.getProtocol().equals("jar")) {
            // Found and adapted a nifty technique from http://www.uofr.net/~greg/java/get-resource-listing.html
            log.fine("We're running from inside a jar file, so we're going to store references to everything inside")
            String jarPath = u.getPath().substring(5, u.getPath().indexOf("!")) //strip out only the JAR file
            log.fine("jarPath is: " + jarPath)
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
            log.fine "Successfully loaded a jar file reference, returning it"
            _jar = jar
        }
        else {
            log.info "We're not running inside a jar file, so we don't need any references to jar resources"
        }
    }

    public String getPackagePath() {
        return _rootPath
    }

    public boolean isJar() {
        return _jar != null
    }

    /**
     * Retrieves all classes in a path within the loader
     * This relies on the directory only containing desired classes, closure stubs, and sub dirs
     * TODO: Need a separate loader for external addons - it needs override priority for user content
     * @param subPath path under the root path of the jar we care about
     * @return instanced Groovy classes we'll later load into Java classes for performance reasons
     */
    public getClassesAt(String subPath) {
        def allClasses = []
        def path = ""
        if (subPath.isEmpty()) {
            path = _rootPath
        } else {
            path = _rootPath + "/" + subPath
        }
        log.info "Getting classes from " + path

        // Check to see if we're loading from within a jar file not not
        if (_jar != null) {
            Enumeration<JarEntry> entries = _jar.entries()
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName()
                if (name.startsWith(path)) { // We only care about stuff under the desired path
                    String entry = name.substring(path.length())
                    log.fine("Class entry under desired path: " + entry)
                    if (entry[-1] == '/') {
                        log.fine("This one is a dir, ignoring it")
                    } else {
                        log.fine("This is not a dir, going to check if it is a suitable class")
                        // We only care about class files that are not inner classes ($) nor deeper than desired path (exactly one /)
                        if (!entry.contains('$') && entry.endsWith(".class") && entry.count('/') == 1) {
                            def className = entry[0..-7]
                            log.fine("Useful class: " + className)
                            allClasses << getClass().getClassLoader().loadClass((path + className).replace('/', '.'))
                        }
                    }
                }
            }
        } else {
            // Load from file system instead (running from source)
            URL u = getClass().getClassLoader().getResource(path)
            path = path.replace('/', '.')

            new File(u.toURI()).list().each { i ->
                // Ignore directories and compiled inner classes (closures)
                if (!i.contains('$') && i.endsWith(".class")) {
                    def className = i[0..-7]

                    log.fine("Found: " + className)
                    allClasses << getClass().getClassLoader().loadClass(path + "." + className)
                }
            }
        }

        return allClasses
    }

    /**
     * Retrieves all images from the given subpath of the package path.
     * Assume there is nothing but images in the subpath
     * TODO: Refactor further - build around a getResources function
     * TODO: Exclude non-images
     * @param subPath of where to start looking under the root path
     * @return the images we loaded
     */
    public getImages(String subPath) {
        def path
        if (subPath.isEmpty()) {
            path = _rootPath
        } else {
            path = _rootPath + "/" + subPath
        }
        log.info "Getting images from under " + path

        if (isJar()) {
            return getInternalImagesFromJar(path)
        } else {
            return getInternalImages(path)
        }
    }

    /**
     * Looks for Block image files recursively starting from a given path and adds them to a map
     * @param path the path to start looking from
     * @return a map containing loaded BufferedImages tied to their filename minus .png
     */
    private getInternalImages(String path) {
        def images = [:]

        // TODO: Check if either of these work, is resource loader better?
        // URL u = ResourceLoader.getResource(path)
        URL u = getClass().getClassLoader().getResource(path)
        log.info "*** Going to look for images under non-jar classpath: " + path

        new File(u.toURI()).list().each { i ->
            log.fine "Checking filename/dir: " + i
            // Expecting either png images or subdirs with more png images (and potentially more subdirs)
            // TODO: We might need some error handling here (hopefully solid convention is enough)
            if (i.endsWith(".png")) {
                log.fine "Useful image: " + i
                // Load a BufferedImage and put it in the map tied to its name short the ".png"
                images.put(i[0..-5], ImageIO.read(ResourceLoader.getResource(path + "/" + i).openStream()))
            }
            else {
                // Recursively go through subdirs and add all we find there
                images.putAll(getInternalImages(path + '/' + i))
            }
        }
        // Return the final map
        return images
    }

    /**
     * Looks for Block image files inside the jar file we're running from all at once and adds them to a map
     * @param path path within the jar file we care about
     * @return a map containing loaded BufferedImages tied to their filename minus .png
     */
    private getInternalImagesFromJar(String path) {
        log.info "*** Going to look for images under jar classpath: " + path
        def images = [:]
        Set<String> result = new HashSet<String>() // Detect dupes
        Enumeration<JarEntry> entries = _jar.entries()
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName()
            //println "Got a name: " + name
            if (name.startsWith(path)) { // We only care about stuff under the desired path
                String entry = name.substring(path.length())
                log.fine "Entry under desired path: " + entry
                if (entry[-1] == '/') {
                    log.fine "This one is a dir, ignoring it"
                } else {
                    // We check to see if any item adds return false, meaning the item already existed (bad)
                    if (!result.add(entry)) {
                        log.warning "Hit a dupe image - this may not be bad but killing everything anyway just in case!"
                        throw new RuntimeException("Loaded a duplicate image from a jar file! Something might be wrong")
                    }
                }
            }
        }

        // print what we got and load as images, then return a nice mapping of the two
        result.each {
            log.info it + " is being loaded as an image and mapped to short name " + it[(it.lastIndexOf('/') + 1)..-5]
            // Load a BufferedImage and put it in the map tied to its name short the ".png"
            images.put(it[(it.lastIndexOf('/') + 1)..-5], ImageIO.read(ResourceLoader.getResource(path + it).openStream()))
        }

        return images

    }
}
