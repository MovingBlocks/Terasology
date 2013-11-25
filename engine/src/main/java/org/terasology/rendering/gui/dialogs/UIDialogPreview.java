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

package org.terasology.rendering.gui.dialogs;

import java.nio.ByteBuffer;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.Texture.FilterMode;
import org.terasology.rendering.assets.texture.Texture.WrapMode;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.layout.ColumnLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIButton.ButtonType;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.opengl.OpenGLTexture;
import org.terasology.world.WorldBiomeProvider.Biome;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGeneratorInfo;
import org.terasology.world.internal.WorldBiomeProviderImpl;

import com.google.common.collect.Lists;

/*
 * Dialog for map setup
 *
 * @author msteiger
 * @author MPratt "map gen setup" <marsmod@hotmail.com>
 */

public class UIDialogPreview extends UIDialog {

    private static final Logger logger = LoggerFactory.getLogger(UIDialogPreview.class);

    private UIButton buttonOk;
    private UIButton buttonCancel;
    
    private UISlider zoomSlider;

    private UIImage imagePreview;
    private final int imageSize = 128;
    
    private WorldGenerator worldGenerator;

    private WorldBiomeProviderImpl biomeProvider;

    private List<UIButton> radioGroup;
    private UIText inputSeed;
    
    public UIDialogPreview(WorldGeneratorInfo info, String seed) {

        super(new Vector2f(640f, 480f));
        setTitle("Setup Map");

        inputSeed.setText(seed);
        biomeProvider = new WorldBiomeProviderImpl(seed);

//        try {
//            BlockManagerImpl blockManager = new BlockManagerImpl(new WorldAtlas(4096), new DefaultBlockFamilyFactoryRegistry());
//            CoreRegistry.put(BlockManager.class, blockManager);
//
//            worldGenerator = CoreRegistry.get(WorldGeneratorManager.class).createGenerator(info.getUri());
//            worldGenerator.setWorldSeed(seed);
//            worldGenerator.setWorldBiomeProvider(biomeProvider);
//        } catch (UnresolvedWorldGeneratorException e) {
//            logger.error("Unable to load world generator", e);
//        }

        updatePreview();
    }

    @Override
    protected void createDialogArea(final UIDisplayContainer parent) {
        setModal(true);

        UIComposite detailPanel = new UIComposite();
        ColumnLayout layout = new ColumnLayout();
        layout.setSpacingVertical(8f);
        detailPanel.setLayout(layout);

        UILabel inputSeedLabel = new UILabel("Seed");
        inputSeedLabel.setColor(org.newdawn.slick.Color.darkGray);

        inputSeed = new UIText();
        inputSeed.setSize(new Vector2f(120, 32));
        inputSeed.addChangedListener(new ChangedListener() {

            @Override
            public void changed(UIDisplayElement element) {
                biomeProvider = new WorldBiomeProviderImpl(inputSeed.getText());
                updatePreview();
            }
        });
        
        detailPanel.addDisplayElement(inputSeedLabel);
        detailPanel.addDisplayElement(inputSeed);
 
        Vector2f defSize = new Vector2f(80, 32);
        UIButton buttonShowBiomes = new UIButton(defSize, ButtonType.TOGGLE);
        buttonShowBiomes.getLabel().setText("Biomes");
        buttonShowBiomes.setToggleState(true);
        
        ColorFunction biomeColor = new ColorFunction() {
            @Override
            public Color get(int x, int z) {
                Biome biome = biomeProvider.getBiomeAt(x, z);
                
                switch (biome) {
                case DESERT:
                    return Color.YELLOW;
                case FOREST:
                    return Color.GREEN;
                case MOUNTAINS:
                    return new Color(240, 120, 120);
                case PLAINS:
                    return new Color(220, 220, 60);
                case SNOW:
                    return Color.WHITE;
                default:
                    return Color.GREY;
                }
            }            
        };
        buttonShowBiomes.setUserData(biomeColor);
        
        UIButton buttonShowHum = new UIButton(defSize, ButtonType.TOGGLE);
        buttonShowHum.getLabel().setText("Humidity");

        ColorFunction humColor = new ColorFunction() {
            @Override
            public Color get(int x, int z) {
                float hum = biomeProvider.getHumidityAt(x, z);
                return new Color(hum * 0.2f, hum * 0.2f, hum);
            }
        };
        
        buttonShowHum.setUserData(humColor);
        
        UIButton buttonShowTemp = new UIButton(defSize, ButtonType.TOGGLE);
        buttonShowTemp.getLabel().setText("Temperature");

        ColorFunction tempColor = new ColorFunction() {
            @Override
            public Color get(int x, int z) {
                float temp = biomeProvider.getTemperatureAt(x, z);
                return new Color(temp, temp * 0.2f, temp * 0.2f);
            }
        };

        buttonShowTemp.setUserData(tempColor);

        radioGroup = Lists.newArrayList(buttonShowBiomes, buttonShowHum, buttonShowTemp);
        
        ClickListener btnClick = new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIButton clickedBtn = (UIButton) element;
                for (UIButton btn : radioGroup) {
                    if (clickedBtn != btn) {
                        btn.setToggleState(false); 
                    } else {
                        btn.setToggleState(true); 
                    }
                }
                
                updatePreview();
            }
        };
        
        for (UIButton btn : radioGroup) {
            btn.addClickListener(btnClick);
            detailPanel.addDisplayElement(btn);
        }

        zoomSlider = new UISlider(defSize, 0, 100);
        zoomSlider.setValue(64);
        zoomSlider.setText("64x");
        zoomSlider.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                slider.setText(slider.getValue() + "x");
                updatePreview();
            }
        });

        detailPanel.addDisplayElement(zoomSlider);
        
        detailPanel.layout();
        
        int imageScreenSize = 384;
        detailPanel.setPosition(new Vector2f(40 + imageScreenSize, 40));
        
        imagePreview = new UIImage();
        imagePreview.setSize(new Vector2f(imageScreenSize, imageScreenSize));
        imagePreview.setPosition(new Vector2f(20, 40));
        imagePreview.setBorderSolid(new Vector4f(1f, 1f, 1f, 1f), org.newdawn.slick.Color.black);
        
        parent.addDisplayElement(imagePreview);
        parent.addDisplayElement(detailPanel);
        
        parent.layout();
    }
    
    @Override
    protected void createButtons(UIDisplayContainer parent) {

        buttonOk = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        buttonOk.getLabel().setText("OK");
        buttonOk.setVisible(true);
        buttonOk.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

                closeDialog(EReturnCode.OK, inputSeed.getText());
            }
        });

        buttonCancel = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        buttonCancel.getLabel().setText("Cancel");
        buttonCancel.setVisible(true);
        buttonCancel.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

                closeDialog(EReturnCode.CANCEL, null);
            }
        });

        parent.addDisplayElement(buttonCancel);
        parent.addDisplayElement(buttonOk);
        
        parent.layout();
    }

    
    private void updatePreview() {
        
        UIButton selected = null;
        for (UIButton btn : radioGroup) {
            if (btn.getToggleState()) {
                selected = btn;
                break;
            }
        }
        
        int scale = zoomSlider.getValue();
        
        if (selected != null) {
            ColorFunction colorFunc = (ColorFunction) selected.getUserData();
            imagePreview.setTexture(createTexture(imageSize, imageSize, scale, colorFunc));
        }
    }
    
    private Texture createTexture(int width, int height, int scale, ColorFunction colorFunc) {

        int size = 4 * width * height;
        byte[] array = new byte[size];
        
        final int offX = -width / 2;
        final int offY = -height / 2;
        final int scaleX = scale;
        final int scaleY = scale;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = (x + offX) * scaleX;
                int py = (y + offY) * scaleY;
                Color c = colorFunc.get(px, py);
                
                array[(y * width + x) * 4 + 0] = (byte) c.r();
                array[(y * width + x) * 4 + 1] = (byte) c.g();
                array[(y * width + x) * 4 + 2] = (byte) c.b();
                array[(y * width + x) * 4 + 3] = (byte) c.a();
            }
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        buf.put(array);
        buf.flip();

        ByteBuffer[] data = new ByteBuffer[] {buf};

        AssetUri uri = new AssetUri(AssetType.TEXTURE, "engine:terrainpreview");
        TextureData texdata = new TextureData(width, height, data, WrapMode.Clamp, FilterMode.Nearest);

        return new OpenGLTexture(uri, texdata);
    }
    
    private interface ColorFunction<T> {
        /**
         * @param x the x world coordinate
         * @param z the z world coordinate
         * @return never <code>null</code>
         */
        Color get(int x, int z);
    }
}
