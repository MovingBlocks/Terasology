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
package org.terasology.rendering.nui.widgets;

import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * A widget for creating hyperlinks based on a specified URL and displayed
 * using a UILabel.
 */
public class UILink extends UILabel {

    @LayoutConfig
    private Binding<String> link = new DefaultBinding<>("");

    public UILink() {
    }

    /**
     * Parameterized constructor- takes in a String to create and display a
     * hyperlink using specified String. Default color of hyperlink is blue.
     * @param url The URL to link the text to
     */
    public UILink(String url) {
        super(url);
        createLink(url);
        setColor(Color.BLUE);
    }

    /**
     * Parameterized constructor- takes in two Strings to create a hyperlink
     * using first String and display the hyperlink using second String.
     * Default color of hyperlink is blue.
     * @param url The URL to link the text to
     * @param text The text used to display the hyperlink
     */
    public UILink(String url, String text) {
        super(text);
        createLink(url);
        setColor(Color.BLUE);
    }

    /**
     * Parameterized constructor- takes in two Strings and a Color to create a
     * hyperlink using first String and display the hyperlink using second
     * String and specified Color.
     * @param url The URL to link the text to
     * @param text The text used to display the hyperlink
     * @param linkColor The Color to display the hyperlink
     */
    public UILink(String url, String text, Color linkColor) {
        super(text);
        createLink(url);
        setColor(linkColor);
    }

    /**
     * Method to link text (inherited from UILabel) to specified URL
     * @param url The URL to link the text to
     */
    public void createLink(String url) {
        // TODO: Create a hyperlink using url and display link with text field from UILabel

        // HTML does not seem to work
        this.link.set("<a href=" + url + ">" + getText() + "</a>");
    }

    /**
     * Getter for link.
     * @return The URL of the link
     */
    public String getLink() {
        if (this.link == null) {
            return "";
        }
        return this.link.get();
    }

    /**
     * Method to set the color of the text used to display hyperlink
     * @param textColor The Color to display the text
     */
    public void setColor(Color textColor) {
        setText(FontColor.getColored(getText(), textColor));
    }
}
