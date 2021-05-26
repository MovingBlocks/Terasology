// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

public class EnterTextPopup extends CoreScreenLayer {
    private Binding<String> inputBinding;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "ok", button -> {
            if (inputBinding != null) {
                inputBinding.set(find("text", UIText.class).getText());
            }
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    public void bindInput(Binding<String> binding) {
        this.inputBinding = binding;
    }

}
