// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.layers.mainMenu.savedGames;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple data class to represent numbered game names.
 * <p>
 * Takes care of parsing strings as numbered game names, e.g., to check for colliding names or extracting the next free
 * game number.
 *
 * @see GameProvider
 */
public class NumberedGameName {

    final String namePrefix;
    final Optional<Integer> number;

    public NumberedGameName(String namePrefix, Optional<Integer> number) {
        this.namePrefix = namePrefix.trim();
        this.number = number;
    }

    /**
     * Parse a given string as numbered game name according to the following regular expression:
     * <p>
     * <pre>
     *     "^(.*?)(\\d+)?$"
     * </pre>
     * These examples will be parsed as follows:
     * <pre>
     *      "Gooey's 1st Game"  → ("Gooey's 1st Game", None)
     *      "Gooey 42"          → ("Gooey", Optional(42))
     *      "42"                → ("42", None)
     * </pre>
     * <p>
     * The input string will be trimmed before matching.
     *
     * @param fullName the full game name
     * @return the parsed numbered game name, the number may be {@code Optional::empty}
     */
    static NumberedGameName fromString(String fullName) {
        final Pattern p = Pattern.compile("^(.*?)(\\d+)?$");
        Matcher matcher = p.matcher(fullName.trim());

        if (matcher.find()) {
            String prefix;
            Optional<Integer> number;

            if (matcher.start(2) > 0) {
                prefix = matcher.group(1);
                number = Optional.of(Integer.parseInt(matcher.group(2)));
            } else {
                prefix = matcher.group(0); // the whole string
                number = Optional.empty();
            }
            return new NumberedGameName(prefix, number);
        }
        throw new IllegalArgumentException("Unexpected error: Cannot parse '" + fullName + "' as numbered game name.");
    }

    /**
     * A textual representation of a numbered game version.
     * <p>
     * The number is appended with a single space as delimiter to the name prefix. If the number is not present, only
     * the name prefix will be returned.
     *
     * <pre>
     *      ("Gooey's 1st Game", None)  → "Gooey's 1st Game"
     *      ("Gooey", Optional(42))     → "Gooey 42"
     *      ("42", None)                → "42"
     * </pre>
     *
     * @return a textual representation of the numbered game version
     */
    @Override
    public String toString() {
        return namePrefix + number.map(i -> " " + i).orElse("");
    }
}
