// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.helpers

import picocli.CommandLine

class KitchenSink {
    /**
     * Tests a URL via a HEAD request (no body) to see if it is valid
     * @param url the URL to test
     * @return boolean indicating whether the URL is valid (code 200) or not
     */
    static boolean isUrlValid(String url) {
        def code = new URL(url).openConnection().with {
            requestMethod = 'HEAD'
            connect()
            responseCode
        }
        return code.toString() == "200"
    }

    // Logging methods to produce colored text. Other styling is inconsistent, for instance underline on Windows sets gray background instead

    /**
     * Simply logs text in green
     * @param message the message to color
     */
    static void green(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|green $message|@"))
    }

    /**
     * Simply logs text in yellow
     * @param message the message to color
     */
    static void yellow(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|yellow $message|@"))
    }

    /**
     * Simply logs text in red
     * @param message the message to color
     */
    static void red(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string("@|red $message|@"))
    }


}
