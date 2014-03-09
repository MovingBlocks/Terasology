/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import java.nio.ByteBuffer;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * @author Martin Steiger
 */
public class PlayerSettingsScreen extends CoreScreenLayer {

    @In
    private Config config;

    private UISlider sliderBlue;
    private UISlider sliderGreen;
    private UISlider sliderRed;
    private UIImage img;
    
    @Override
    public void initialise() {
        UIText nametext = find("playername", UIText.class);
        if (nametext != null) {
            nametext.bindText(BindHelper.bindBeanProperty("name", config.getPlayer(), String.class));
        }

        Color color = config.getPlayer().getColor();
        
        img = find("image", UIImage.class);
        img.setImage(createTexture(10, 10, color));

        sliderRed = find("red", UISlider.class);
        sliderGreen = find("green", UISlider.class);
        sliderBlue = find("blue", UISlider.class);

        sliderRed.bindValue(new NotifyingBinding(color.rf()));
        sliderGreen.bindValue(new NotifyingBinding(color.gf()));
        sliderBlue.bindValue(new NotifyingBinding(color.bf()));
        
        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
    }
    
    /**
     * 
     */
    public void update() {
        float red = sliderRed.getValue();
        float green = sliderGreen.getValue();
        float blue = sliderBlue.getValue();

        Color c = new Color(red, green, blue);

        config.getPlayer().setColor(c);
        
        img.setImage(createTexture(10, 10, c));
    }

    private Texture createTexture(int width, int height, Color color) {
        int size = 4 * width * height;

        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                color.addToBuffer(buf);
            }
        }
        buf.flip();

        ByteBuffer[] data = new ByteBuffer[]{buf};
        AssetUri uri = new AssetUri(AssetType.TEXTURE, "engine:colorPreview");
        TextureData texData = new TextureData(width, height, data, Texture.WrapMode.CLAMP, Texture.FilterMode.LINEAR);

        return Assets.generateAsset(uri, texData, Texture.class);
    }
    
    /**
     * Calls update() in parent class when the slider value changes
     */
    private final class NotifyingBinding extends DefaultBinding<Float> {

        private NotifyingBinding(Float value) {
            super(value);
        }

        @Override
        public void set(Float v) {
            super.set(v);
            
            update();
        }
    }
}
