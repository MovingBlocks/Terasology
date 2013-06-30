/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering.gui.widgets;

import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.style.StyleShadow.EShadowDirection;
import org.terasology.rendering.gui.layout.RowLayout;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/**
 * A tab item which can be added to a tab folder.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see UITabFolder
 */
public class UITabItem extends UIDisplayContainerScrollable {

    //child elements
    private UITabFolder tabFolder;
    private final UITab tab;

    /**
     * The tab element which can be clicked on, to switch between the tabs.
     * All tabs are displayed in the tab bar over the actual tabs.
     * This element is directly bind to an UITabItem and can't be instanced alone.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public class UITab extends UIDisplayContainer {

        private final UITabItem tabItem;
        private Vector4f padding = new Vector4f(5f, 5f, 5f, 5f);
        private final float imageLabelSpace = 5f;

        //child elements
        private final UILabel label;
        private final UIImage image;

        private UITab(UITabItem tabItem) {
            this.tabItem = tabItem;
            select(false);

            addClickListener(new ClickListener() {
                @Override
                public void click(UIDisplayElement element, int button) {
                    UITabFolder folder = getTabItem().getTabFolder();
                    if (folder != null) {
                        folder.select(getTabItem());
                    }
                }
            });

            RowLayout layout = new RowLayout();
            layout.setSpacingHorizontal(5);

            label = new UILabel();
            label.setColor(Color.black);
            label.addChangedListener(new ChangedListener() {
                @Override
                public void changed(UIDisplayElement element) {
                    calcSizePosition();
                }
            });
            label.setVisible(true);

            image = new UIImage();
            image.setVisible(false);

            addDisplayElement(label);
            addDisplayElement(image);
        }

        private void calcSizePosition() {
            if (image.isVisible()) {
                label.setPosition(new Vector2f(padding.w + image.getSize().x + imageLabelSpace, padding.x));
                image.setPosition(new Vector2f(padding.w, padding.x + label.getSize().y / 2 - image.getSize().y / 2));
                setSize(new Vector2f(image.getSize().x + label.getSize().x + padding.y + padding.w + imageLabelSpace, Math.max(label.getSize().y + padding.x + padding.z, image.getSize().y + padding.x + padding.z)));
            } else {
                label.setPosition(new Vector2f(padding.w, padding.x));
                setSize(new Vector2f(label.getSize().x + padding.y + padding.w, label.getSize().y + padding.x + padding.z));
            }
        }

        /**
         * Get the UITabItem which is bind to this UITab.
         *
         * @return
         */
        private UITabItem getTabItem() {
            return tabItem;
        }

        /**
         * Get the text which will be displayed as the tabs name.
         *
         * @return Returns the text which will be displayed.
         */
        public String getText() {
            return label.getText();
        }

        /**
         * Set the text of the tab which will be displayed as the tabs name. Can only be set indirect through a UITabItem.
         *
         * @param text The text to set.
         */
        private void setText(String text) {
            this.label.setText(text);
        }

        /**
         * Get the padding of the text and image within the tab.
         *
         * @return Returns the padding.
         */
        public Vector4f getPadding() {
            return padding;
        }

        /**
         * /**
         * Set the padding of the text and image within the tab.
         *
         * @param padding The padding.
         */
        public void setPadding(Vector4f padding) {
            this.padding = padding;
            label.setPosition(new Vector2f(padding.w, padding.x));
        }

        /**
         * Set an image which can be displayed in the tab, left from the text. If the texture couldn't be loaded, the previous set image will be removed.
         *
         * @param texture       The texture. Can be an empty string to unload the previous set image.
         * @param textureOrigin The origin in the texture.
         * @param textureSize   The size of the texture.
         */
        public void setImage(String texture, Vector2f textureOrigin, Vector2f textureSize) {
            Texture tex = Assets.getTexture(texture);
            if (tex != null) {
                image.setVisible(true);
                image.setTexture(tex);
                image.setTextureOrigin(textureOrigin);
                image.setTextureSize(textureSize);
                if (image.getSize().x == 0 && image.getSize().y == 0) {
                    image.setSize(textureSize);
                }
            } else {
                image.setVisible(false);
            }

            calcSizePosition();
        }

        /**
         * Set the size of the image which can be displayed in the tab, left from the text.
         *
         * @param size The size.
         */
        public void setImageSize(Vector2f size) {
            image.setSize(size);

            calcSizePosition();
        }

        /**
         * Set whether the tab should be selected.
         *
         * @param select True to select the tab.
         */
        public void select(boolean select) {
            if (select) {
                setShadow(new Vector4f(4f, 3f, 0f, 3f), EShadowDirection.OUTSIDE, 0.7f);
            } else {
                setShadow(new Vector4f(0f, 3f, 4f, 3f), EShadowDirection.INSIDE, 1);
            }
        }
    }

    public UITabItem() {
        tab = new UITab(this);
        tab.setText("");
        tab.setVisible(true);
    }

    /**
     * Get the tab folder where the item was added.
     *
     * @return Returns the tab folder where the item was added.
     */
    public UITabFolder getTabFolder() {
        return tabFolder;
    }

    /**
     * Set the tab folder where the item belongs too. Will be set from the tab folder.
     * Shouldn't be used by anyone other.
     *
     * @param folder The tab folder.
     */
    public void setTabFolder(UITabFolder folder) {
        this.tabFolder = folder;
    }

    /**
     * Get the tab element which can be clicked on, to switch between the tabs.
     * All tabs are displayed in the tab bar over the actual tabs.
     *
     * @return Returns the tab.
     */
    public UITab getTab() {
        return tab;
    }

    /**
     * Get the text which will be displayed as the tabs name.
     *
     * @return Returns the text which will be displayed.
     */
    public String getText() {
        return tab.getText();
    }

    /**
     * Set the text of the tab which will be displayed as the tabs name. Can only be set indirect through a UITabItem.
     *
     * @param text The text to set.
     */
    public void setText(String text) {
        tab.setText(text);
    }
}
