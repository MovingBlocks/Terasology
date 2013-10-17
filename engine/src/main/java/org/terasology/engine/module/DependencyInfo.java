/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.engine.module;

/**
 * @author Immortius
 */
public class DependencyInfo {

    private String id = "";
    private Version minVersion = new Version(0, 0, 0);
    private Version maxVersion;

    public String getId() {
        return id;
    }

    public Version getMinVersion() {
        return minVersion;
    }

    public Version getMaxVersion() {
        if (maxVersion == null) {
            return minVersion.getNextMajorVersion();
        }
        return maxVersion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMinVersion(Version value) {
        this.minVersion = value;
    }

    public void setMaxVersion(Version value) {
        this.maxVersion = value;
    }
}
