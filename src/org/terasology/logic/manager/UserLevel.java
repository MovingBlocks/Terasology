/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package org.terasology.logic.manager;
// This is a stub for user rights management. It serves mainly as a reminder 
// that there needs to be some sort of rights management                            
public class UserLevel {

    private static UserLevel _userLevel;

    public static UserLevel getInstance()
    {
        if(_userLevel == null) {
            _userLevel = new UserLevel();
        }
        return _userLevel;
    }

    private UserLevel(){}

    public boolean hasRights(){
        //should querry server / world if user has rights
        return true;
    }
}
