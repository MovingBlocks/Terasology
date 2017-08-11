/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.config;

public class SelectModulesConfig {
    private boolean isChecked;
    private boolean isLibraryChecked;
    private boolean isAssetChecked;
    private boolean isWorldChecked;
    private boolean isGameplayChecked;
    private boolean isAugmentationChecked;
    private boolean isSpecialChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean value) {
        this.isChecked = value;
    }

    public boolean isLibraryChecked() {
        return isLibraryChecked;
    }

    public void setIsLibraryChecked(boolean value) {
        this.isLibraryChecked =value;
    }

    public boolean isAssetChecked() {
        return isAssetChecked;
    }

    public void setIsAssetChecked(boolean value) {
        this.isAssetChecked =value;
    }

    public boolean isGameplayChecked() {
        return isGameplayChecked;
    }

    public void setIsGameplayChecked(boolean value) {
        this.isGameplayChecked =value;
    }

    public boolean isAugmentationChecked() {
        return isAugmentationChecked;
    }

    public void setIsAugmentationChecked(boolean value) {
        this.isAugmentationChecked =value;
    }

    public boolean isSpecialChecked() {
        return isSpecialChecked;
    }

    public void setIsSpecialChecked(boolean value) {
        this.isSpecialChecked =value;
    }

    public boolean isWorldChecked() {
        return isWorldChecked;
    }

    public void setIsWorldChecked(boolean value) {
        this.isWorldChecked =value;
    }

}
