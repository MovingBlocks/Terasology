/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.i18n;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.terasology.engine.SimpleUri;
import org.terasology.engine.Uri;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

/**
 * A URI to identify internationalization objects in Terasology.
 * These URIs are always in the form: {@literal <module-name>:<project-name>#<id>}.
 */
@API
public class TranslationUri implements Uri {

    /**
     * The separation character between project name and id
     */
    protected static final String PROJECT_SEPARATOR = "#";

    private static final Pattern URI_PATTERN = Pattern.compile("([^:]+):([^#]+)#(.+)?");

    private final Name moduleName;
    private final Name projectName;
    private final Name id;

    /**
     * Creates an empty, invalid instance.
     */
    public TranslationUri() {
        moduleName = Name.EMPTY;
        projectName = Name.EMPTY;
        id = Name.EMPTY;
    }

    /**
     * Creates an instance for the given <code>module:project#id</code> combo
     * @param moduleName
     * @param projectName
     * @param idName
     */
    public TranslationUri(String moduleName, String projectName, String idName) {
        this(new Name(moduleName), new Name(projectName), new Name(idName), null);
    }

    /**
     * Creates an instance for the given <code>module:project#id!locale</code> combo
     * @param moduleName
     * @param projectName
     * @param idName
     * @param langTag the language tag
     */
    public TranslationUri(String moduleName, String projectName, String idName, String langTag) {
        this(new Name(moduleName), new Name(projectName), new Name(idName), Locale.forLanguageTag(langTag));
    }

    /**
     * @param module
     * @param project
     * @param id
     * @param locale
     */
    public TranslationUri(Name module, Name project, Name id, Locale locale) {
        this.moduleName = module;
        this.projectName = project;
        this.id = id;
    }

    /**
     * Creates an instance from a string in the format <code>module:project#id!locale</code>.
     * If the string does not match this format, it will be marked invalid.
     * @param uri
     */
    public TranslationUri(String uri) {
        Matcher match = URI_PATTERN.matcher(uri);
        if (match.matches()) {
            moduleName = new Name(match.group(1));
            projectName = new Name(match.group(2));
            id = new Name(match.group(3));
        } else {
            moduleName = Name.EMPTY;
            projectName = Name.EMPTY;
            id = Name.EMPTY;
        }
    }

    @Override
    public Name getModuleName() {
        return moduleName;
    }

    public Name getProjectName() {
        return projectName;
    }

    public Uri getProjectUri() {
        return new SimpleUri(moduleName, projectName);
    }

    public Name getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        // locale can be unspecified
        return !moduleName.isEmpty() && !projectName.isEmpty() && !id.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("%s%s%s%s%s", moduleName, MODULE_SEPARATOR, projectName, PROJECT_SEPARATOR, id);
    }
}
