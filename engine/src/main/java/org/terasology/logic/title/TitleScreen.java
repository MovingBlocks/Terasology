/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.title;

import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;

public class TitleScreen extends CoreScreenLayer {

    private String title = "";
    private String subtitle = "";
    private UILabel uiTitle;
    private UILabel uiSubtitle;

    @Override
    public void initialise() {
        uiTitle = find("title", UILabel.class);
        if (uiTitle != null) {
            uiTitle.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return title;
                }
            });
        }

        uiSubtitle = find("subtitle", UILabel.class);
        if (uiSubtitle != null) {
            uiSubtitle.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return subtitle;
                }
            });
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
