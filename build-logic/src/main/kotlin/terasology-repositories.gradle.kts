// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import java.net.URI

// We use both Maven Central and our own Artifactory instance, which contains module builds, extra libs, and so on
repositories {
    mavenCentral {
        content {
            // This is first choice for most java dependencies, but assume we'll need to check our
            // own repository for things from our own organization.
            // (This is an optimization so gradle doesn't try to find our hundreds of modules in 3rd party repos)
            excludeGroupByRegex("""org\.terasology(\..+)?""")
        }
    }

    google {
        // gestalt uses an annotation library from Google
        content {
            // (This is an optimization so gradle doesn't try to find our hundreds of modules here.)
            excludeGroupByRegex("""org\.terasology(\..+)?""")
        }
    }

    // JBoss Maven Repository requried to fetch `org.jpastebin` dependency for CrashReporter
    maven {
        name = "JBoss Public Maven Repository Group"
        url = URI("https://repository.jboss.org/nexus/content/repositories/public/")
        content {
            includeModule("org", "jpastebin")
        }
    }

    // MovingBlocks Artifactory instance for libs not readily available elsewhere plus our own libs
    maven {
        val repoViaEnv = System.getenv()["RESOLUTION_REPO"]
        if (rootProject.hasProperty("alternativeResolutionRepo")) {
            // If the user supplies an alternative repo via gradle.properties then use that
            name = "from alternativeResolutionRepo property"
            url =  URI(rootProject.properties["alternativeResolutionRepo"] as String)
        } else if (repoViaEnv != null && repoViaEnv != "") {
            name = "from \$RESOLUTION_REPO"
            url = URI(repoViaEnv)
        } else {
            // Our default is the main virtual repo containing everything except repos for testing Artifactory itself
            name = "Terasology Artifactory"
            url = URI("https://artifactory.terasology.io/artifactory/virtual-repo-live")
        }
    }

    // TODO MYSTERY: As of November 7th 2011 virtual-repo-live could no longer be relied on for latest snapshots - Pro feature?
    // We've been using it that way for *years* and nothing likewise changed in the area for years as well. This seems to work ....
    maven {
        name = "Terasology snapshot locals"
        url = URI("https://artifactory.terasology.io/artifactory/terasology-snapshot-local")
    }
}
