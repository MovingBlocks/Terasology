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
package org.terasology.input;



/**
 * Implementation of InputModified which allows for an Input(only key currently supported) to have a modifier
 * key attached to it (alt, control, shift or none) and act as you would expect in the input system.
 */
public class InputModifiedImpl implements InputModified{
    private Modifier modifier;
    private Input main;

    public InputModifiedImpl(Input main){
        this.main = main;
        this.modifier = Modifier.NONE;
    }

    public InputModifiedImpl(Input main, Modifier modifier){
        this.main = main;
        this.modifier = modifier;
    }

    @Override
    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public InputType getType() {
        return main.getType();
    }

    @Override
    public int getId() {
        return main.getId();
    }

    @Override
    public String getName() {
        return main.getName();
    }

    @Override
    public String getDisplayName() {
        switch(modifier){
            case ALT:
                return "Alt + " + main.getDisplayName();
            case CTRL:
                return "Ctrl + " + main.getDisplayName();
            case SHIFT:
                return "Shift + " + main.getDisplayName();
            case NONE:
            default:
                    return main.getDisplayName();
        }
    }

    @Override
    public void setModifier(Modifier modifier){
        this.modifier = modifier;
    }
}
