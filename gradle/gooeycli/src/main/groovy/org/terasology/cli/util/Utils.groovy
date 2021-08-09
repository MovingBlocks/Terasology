// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.util

class Utils {
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
}
