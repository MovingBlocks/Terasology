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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
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
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.WorldGenerator2DPreview;
import org.terasology.world.generator.WorldGeneratorInfo;
import org.terasology.world.generator.WorldGeneratorManager;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    private WorldGenerator2DPreview worldGenerator2DPreview;
    private WorldGenerator worldGenerator;

    private List<UIButton> layerButtons;
    private UIText inputSeed;
    private UIComposite detailPanel;
    private Vector2f defSize;
    private boolean isPreviewPossible;


    public UIDialogPreview(WorldGeneratorInfo info, String seed) {
        super(new Vector2f(640f, 480f));
        setTitle("Setup Map");

        // get the world generator
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        Module worldGeneratorModule = moduleManager.getLatestModuleVersion(info.getUri().getModuleName());
        try {
            moduleManager.enableModule(worldGeneratorModule);

            worldGenerator = CoreRegistry.get(WorldGeneratorManager.class).createGenerator(info.getUri());

            if (worldGenerator instanceof WorldGenerator2DPreview) {
                worldGenerator.setWorldSeed(seed);
                worldGenerator2DPreview = (WorldGenerator2DPreview) worldGenerator;
                isPreviewPossible = true;
            } else {
                logger.info(info.getUri().toString() + " does not support a 2d preview");
            }
        } catch (UnresolvedWorldGeneratorException e) {
            // if errors happen, dont enable this feature
            logger.error("Unable to load world generator: " + info.getUri().toString() + " for a 2d preview");
        } finally {
            moduleManager.disableModule(worldGeneratorModule);
        }


        // create the UI
        UILabel inputSeedLabel = new UILabel("Seed");
        inputSeedLabel.setColor(org.newdawn.slick.Color.darkGray);

        inputSeed = new UIText();
        inputSeed.setText(seed);
        inputSeed.setSize(new Vector2f(120, 32));
        inputSeed.addChangedListener(new ChangedListener() {

            @Override
            public void changed(UIDisplayElement element) {
                worldGenerator.setWorldSeed(inputSeed.getText());
                updatePreview();
            }
        });

        detailPanel.addDisplayElement(inputSeedLabel);
        detailPanel.addDisplayElement(inputSeed);


        layerButtons = new ArrayList<UIButton>();
        for (String layerName : worldGenerator2DPreview.getLayers()) {

            UIButton layerButton = new UIButton(defSize, ButtonType.TOGGLE);
            layerButton.getLabel().setText(layerName);
            layerButton.setUserData(layerName);
            layerButtons.add(layerButton);
        }

        if (layerButtons.size() > 0) {
            layerButtons.get(0).setToggleState(true);
        }

        ClickListener btnClick = new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIButton clickedBtn = (UIButton) element;
                for (UIButton btn : layerButtons) {
                    if (clickedBtn != btn) {
                        btn.setToggleState(false);
                    } else {
                        btn.setToggleState(true);
                    }
                }

                updatePreview();
            }
        };

        for (UIButton btn : layerButtons) {
            btn.addClickListener(btnClick);
            detailPanel.addDisplayElement(btn);
        }

        detailPanel.layout();
        updatePreview();
    }

    @Override
    protected void createDialogArea(final UIDisplayContainer parent) {
        defSize = new Vector2f(80, 32);
        setModal(true);

        detailPanel = new UIComposite();
        ColumnLayout layout = new ColumnLayout();
        layout.setSpacingVertical(8f);
        detailPanel.setLayout(layout);

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
        for (UIButton btn : layerButtons) {
            if (btn.getToggleState()) {
                selected = btn;
                break;
            }
        }

        int scale = zoomSlider.getValue();

        if (selected != null) {
            String selectedLayerName = (String) selected.getUserData();
            imagePreview.setTexture(createTexture(imageSize, imageSize, scale, selectedLayerName));
        }
    }

    private Texture createTexture(int width, int height, int scale, String layerName) {

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
                Color c = worldGenerator2DPreview.get(layerName, px, py);

                array[(y * width + x) * 4 + 0] = (byte) c.r();
                array[(y * width + x) * 4 + 1] = (byte) c.g();
                array[(y * width + x) * 4 + 2] = (byte) c.b();
                array[(y * width + x) * 4 + 3] = (byte) c.a();
            }
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        buf.put(array);
        buf.flip();

        ByteBuffer[] data = new ByteBuffer[]{buf};

        AssetUri uri = new AssetUri(AssetType.TEXTURE, "engine:terrainpreview");
        TextureData texdata = new TextureData(width, height, data, WrapMode.Clamp, FilterMode.Nearest);

        return new OpenGLTexture(uri, texdata);
    }

    public boolean isPreviewPossible() {
        return isPreviewPossible;
    }

}
