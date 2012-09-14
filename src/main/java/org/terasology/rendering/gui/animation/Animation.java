/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.animation;

import org.terasology.rendering.gui.framework.UIDisplayElement;

import java.util.ArrayList;

public abstract class Animation {
    private boolean started = false;
    private boolean repeat  = false;
    protected UIDisplayElement target;
    private final ArrayList<AnimationStartNotify> notifyStartElements = new ArrayList<AnimationStartNotify>();
    private final ArrayList<AnimationStopNotify> notifyStopElements = new ArrayList<AnimationStopNotify>();

    public void start(){
        started = true;
        notifyStartListners();
    };

    public void stop(){
        started = false;
        notifyStopListners();
    };

    public boolean isStarted(){
        return started;
    }

    public void setTarget(UIDisplayElement target){
        this.target = target;
    };

    public void setRepeat(boolean repeat){
        this.repeat = repeat;
    }

    public boolean isRepeat(){
        return repeat;
    }

    public void addNotifyListeners(AnimationNotify notify){
        if(notify instanceof AnimationStartNotify){
            notifyStartElements.add((AnimationStartNotify) notify);
        }else if(notify instanceof AnimationStopNotify){
            notifyStopElements.add((AnimationStopNotify) notify);
        }
    }

    private void notifyStartListners(){
        for(AnimationStartNotify notify:notifyStartElements){
            notify.action(target);
        }
    }

    private void notifyStopListners(){
        for(AnimationStopNotify notify:notifyStopElements){
            notify.action(target);
        }
    }

    public void renderBegin(){};
    public void renderEnd(){};
    public void update(){};

}
