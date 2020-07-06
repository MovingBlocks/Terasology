// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
/**
 * Convenience class for pulling properties out of a 'gradle.properties' if present, for various overrides
 * // TODO: YAML variant that can be added for more complex use cases?
 */
class PropHelper {

    static String getGlobalProp(String key) {
        Properties extraProps = new Properties()
        File gradlePropsFile = new File("gradle.properties")
        if (gradlePropsFile.exists()) {
            gradlePropsFile.withInputStream {
                extraProps.load(it)
            }
            //println "Found a 'gradle.properties' file, loaded in global overrides: " + extraProps

            if (extraProps.containsKey(key)) {
                println "Returning found global prop for $key"
                return extraProps.get(key)
            }
            println "Didn't find a global prop for key $key"

        } else {
            println "No 'gradle.properties' file found, not supplying global overrides"
        }
        return null
    }
}
